package com.example.wxxx

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Aura 应用内更新管理引擎
 * 负责 GitHub Releases 最新版本检测、版本号比对、DownloadManager 后台下载、IO 流式安全兜底及 FileProvider 拉起系统安装
 */
object UpdateManager {

    private const val GITHUB_API_URL = "https://api.github.com/repos/Jeesemmy/Aura/releases/latest"

    /**
     * 版本更新信息数据载体
     */
    data class ReleaseInfo(
        val rawTag: String,          // 原始 tag 标识（如 "Aura" 或 "v14.1"）
        val versionName: String,     // 提取出的干净版本号（如 "11.0" 或 "14.1"）
        val title: String,           // Release 标题名称（如 "Aura 11.0"）
        val changelog: String,       // 更新日志内容（body）
        val downloadUrl: String,     // APK 文件直链（browser_download_url）
        val apkSize: Long,           // APK 文件字节大小
        val hasNewVersion: Boolean   // 相对当前客户端是否有新版本
    )

    /**
     * 下载进度状态载体
     */
    data class DownloadProgress(
        val isDownloading: Boolean = false,
        val progress: Int = 0,               // 0 .. 100 百分比
        val bytesDownloaded: Long = 0L,
        val totalBytes: Long = 0L,
        val isCompleted: Boolean = false,
        val isFailed: Boolean = false,
        val downloadedFile: File? = null
    )

    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()

    // 追踪后台轮询协程与注册的系统广播，杜绝泄漏与并发冲突
    private var pollingJob: Job? = null
    private var currentDownloadReceiver: BroadcastReceiver? = null
    private var receiverAppContext: Context? = null

    /**
     * 重置/关闭下载进度并回收后台轮询与广播资源
     */
    fun resetDownloadProgress() {
        pollingJob?.cancel()
        pollingJob = null
        cleanupReceiver()
        _downloadProgress.value = null
    }

    /**
     * 安全注销广播接收器并释放上下文引用
     */
    private fun cleanupReceiver() {
        currentDownloadReceiver?.let { receiver ->
            try {
                receiverAppContext?.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
        currentDownloadReceiver = null
        receiverAppContext = null
    }

    /**
     * 第一步/第二步：请求 GitHub Releases API 并对比版本号
     * 具备完善的超时配置、网络异常/无网络兜底和 JSON 防御性解析
     * @param currentVersion 当前运行的应用版本号（例如 "15.0"）
     */
    suspend fun checkUpdate(currentVersion: String = "15.0"): Result<ReleaseInfo> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(GITHUB_API_URL)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    // ⚠️ 注意：GitHub API 强制要求携带 User-Agent 请求头，否则会返回 403 Forbidden
                    setRequestProperty("User-Agent", "Aura-App")
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    instanceFollowRedirects = true
                }

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorMsg = when (responseCode) {
                        HttpURLConnection.HTTP_FORBIDDEN -> "GitHub API 请求过于频繁或受限 (HTTP 403)，请稍后再试"
                        HttpURLConnection.HTTP_NOT_FOUND -> "未找到相关 Release 版本 (HTTP 404)"
                        in 500..599 -> "GitHub 服务器维护或响应异常 (HTTP $responseCode)，请稍后重试"
                        else -> "服务器响应异常 (HTTP $responseCode)"
                    }
                    return@withContext Result.failure(IOException(errorMsg))
                }

                // 读取 JSON 响应流，使用 use 确保输入流及时安全关闭
                val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                if (responseText.isBlank()) {
                    return@withContext Result.failure(IOException("服务器返回内容为空"))
                }

                val json = try {
                    JSONObject(responseText)
                } catch (e: Exception) {
                    return@withContext Result.failure(IOException("版本数据解析失败，返回内容格式异常", e))
                }

                val rawTag = json.optString("tag_name", "")
                val releaseTitle = json.optString("name", "").ifEmpty { rawTag }
                val changelog = json.optString("body", "暂无更新日志")

                // 提取版本号数字：优先从 tag_name 提取（如 "v14.1" -> "14.1"）
                // 如果 tag_name 只是 "Aura" 无数字，则从 title 中提取（如 "Aura 11.0" -> "11.0"）
                val versionInTag = Regex("""\d+(\.\d+)*""").find(rawTag)?.value
                val versionInTitle = Regex("""\d+(\.\d+)*""").find(releaseTitle)?.value
                val cleanRemoteVersion = versionInTag ?: versionInTitle ?: rawTag

                // 遍历 assets 获取 APK 安装包链接（防范数组中异常条目导致解析崩溃）
                val assets = json.optJSONArray("assets")
                var downloadUrl = ""
                var apkSize = 0L

                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i) ?: continue
                        val name = asset.optString("name", "")
                        val contentType = asset.optString("content_type", "")
                        if (name.endsWith(".apk", ignoreCase = true) ||
                            contentType.equals("application/vnd.android.package-archive", ignoreCase = true)
                        ) {
                            val candidateUrl = asset.optString("browser_download_url", "")
                            if (candidateUrl.isNotBlank()) {
                                downloadUrl = candidateUrl
                                apkSize = asset.optLong("size", 0L)
                                break
                            }
                        }
                    }
                }

                if (downloadUrl.isEmpty()) {
                    return@withContext Result.failure(
                        IllegalStateException("最新 Release 中未找到可用的 APK 安装包")
                    )
                }

                val hasNew = isNewerVersion(currentVersion, cleanRemoteVersion)

                Result.success(
                    ReleaseInfo(
                        rawTag = rawTag,
                        versionName = cleanRemoteVersion,
                        title = releaseTitle,
                        changelog = changelog,
                        downloadUrl = downloadUrl,
                        apkSize = apkSize,
                        hasNewVersion = hasNew
                    )
                )
            } catch (e: UnknownHostException) {
                Result.failure(IOException("无法连接网络，请检查手机网络设置后重试", e))
            } catch (e: SocketTimeoutException) {
                Result.failure(IOException("连接 GitHub 超时，请检查网络后重试", e))
            } catch (e: ConnectException) {
                Result.failure(IOException("无法连接到 GitHub 服务器，请检查网络", e))
            } catch (e: SSLException) {
                Result.failure(IOException("安全连接失败 (SSL异常)，请稍后重试", e))
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    /**
     * 版本号比对逻辑：支持如 "14.0" 对比 "14.1"、"14.0.1"、"15.0" 等
     * @return 当 remoteVersion > currentVersion 时返回 true
     */
    fun isNewerVersion(currentVersion: String, remoteVersion: String): Boolean {
        val curParts = currentVersion.split(".").mapNotNull { it.filter(Char::isDigit).toIntOrNull() }
        val remParts = remoteVersion.split(".").mapNotNull { it.filter(Char::isDigit).toIntOrNull() }
        if (curParts.isEmpty() || remParts.isEmpty()) return false

        val maxLen = maxOf(curParts.size, remParts.size)
        for (i in 0 until maxLen) {
            val cur = curParts.getOrElse(i) { 0 }
            val rem = remParts.getOrElse(i) { 0 }
            if (rem > cur) return true
            if (rem < cur) return false
        }
        return false
    }

    /**
     * 第三步：使用 DownloadManager 进行后台下载，并在完成时通过 FileProvider 调起系统安装
     * 具备 Cursor 严格关闭、广播生命周期管理及 DownloadManager 不可用时的流式下载兜底
     * @param context Context 实例
     * @param downloadUrl APK 下载链接
     * @param versionName 版本名称（用于拼接下载文件名）
     */
    fun downloadAndInstall(
        context: Context,
        downloadUrl: String,
        versionName: String
    ) {
        val appContext = context.applicationContext ?: context

        // 停止先前可能残留的轮询与旧广播，防范多实例竞争与泄漏
        pollingJob?.cancel()
        cleanupReceiver()

        val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager == null) {
            // 系统服务不可用时无缝降级走协程流式下载
            startDirectDownload(appContext, downloadUrl, versionName)
            return
        }

        val fileName = "Aura_$versionName.apk"
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetFile = File(downloadDir, fileName)

        // 若已存在同名旧文件，先清理
        if (targetFile.exists()) {
            try {
                targetFile.delete()
            } catch (_: Exception) {}
        }

        // 初始化进度状态
        _downloadProgress.value = DownloadProgress(
            isDownloading = true,
            progress = 0
        )

        // 构建系统下载请求
        val downloadId: Long = try {
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Aura 正在下载更新")
                setDescription("版本：$versionName")
                setMimeType("application/vnd.android.package-archive")
                // 下载中和下载完成后均在通知栏显示提示
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                // 保存至手机公共 Download 目录
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            // 定制 ROM 禁用 DownloadManager 或由于权限抛出异常时降级至直接流式下载
            startDirectDownload(appContext, downloadUrl, versionName)
            return
        }

        // 启动后台轮询协程，实时读取 DownloadManager 进度并推送至 StateFlow
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            var isPolling = true
            var notFoundCount = 0
            while (isActive && isPolling) {
                try {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    // 使用 use 扩展函数确保 cursor 必定被正确关闭，杜绝 CursorWindow 跨进程句柄泄漏
                    val hasFound = downloadManager.query(query)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            val downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                            val status = if (statusIndex != -1) cursor.getInt(statusIndex) else -1
                            val downloadedBytes = if (downloadedIndex != -1) cursor.getLong(downloadedIndex) else 0L
                            val totalBytes = if (totalIndex != -1) cursor.getLong(totalIndex) else 0L

                            val percent = if (totalBytes > 0) {
                                ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                            } else {
                                0
                            }

                            when (status) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    isPolling = false
                                    val apkFile = resolveDownloadedFile(appContext, downloadManager, downloadId, targetFile)
                                    _downloadProgress.value = DownloadProgress(
                                        isDownloading = false,
                                        progress = 100,
                                        bytesDownloaded = if (totalBytes > 0) totalBytes else downloadedBytes,
                                        totalBytes = totalBytes,
                                        isCompleted = true,
                                        downloadedFile = apkFile
                                    )
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    isPolling = false
                                    _downloadProgress.value = DownloadProgress(
                                        isDownloading = false,
                                        progress = percent,
                                        isFailed = true
                                    )
                                }
                                else -> {
                                    _downloadProgress.value = DownloadProgress(
                                        isDownloading = true,
                                        progress = percent,
                                        bytesDownloaded = downloadedBytes,
                                        totalBytes = totalBytes
                                    )
                                }
                            }
                            true
                        } else {
                            false
                        }
                    } ?: false

                    if (!hasFound) {
                        notFoundCount++
                        if (notFoundCount > 15) { // 连续 3 秒查询不到该任务，判定已取消
                            isPolling = false
                            _downloadProgress.value = DownloadProgress(
                                isDownloading = false,
                                isFailed = true
                            )
                        }
                    } else {
                        notFoundCount = 0
                    }
                } catch (_: Exception) {}

                if (isPolling && isActive) {
                    delay(200)
                }
            }
        }

        // 注册广播接收器监听下载完成事件（核心原生链路保留）
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(recvContext: Context, intent: Intent) {
                if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (id == downloadId) {
                        try {
                            val query = DownloadManager.Query().setFilterById(downloadId)
                            downloadManager.query(query)?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                                    if (statusIndex != -1 && cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                                        val apkFile = resolveDownloadedFile(appContext, downloadManager, downloadId, targetFile)

                                        _downloadProgress.value = DownloadProgress(
                                            isDownloading = false,
                                            progress = 100,
                                            isCompleted = true,
                                            downloadedFile = apkFile
                                        )

                                        if (apkFile != null) {
                                            installApk(appContext, apkFile)
                                        } else {
                                            showToast(appContext, "下载完成，但未找到安装包文件")
                                        }
                                    } else {
                                        _downloadProgress.value = DownloadProgress(
                                            isDownloading = false,
                                            isFailed = true
                                        )
                                        showToast(appContext, "更新包下载失败，请检查网络")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            _downloadProgress.value = DownloadProgress(
                                isDownloading = false,
                                isFailed = true
                            )
                        } finally {
                            // 处理完毕，注销广播，避免内存泄露
                            cleanupReceiver()
                        }
                    }
                }
            }
        }

        currentDownloadReceiver = receiver
        receiverAppContext = appContext

        try {
            // 兼容 Android 14+ 必须显式声明 RECEIVER_EXPORTED (因为属于系统 DownloadManager 跨进程通知)
            ContextCompat.registerReceiver(
                appContext,
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (_: Exception) {
            // 若广播注册受限，后台轮询协程仍可保证 UI 进度与完成事件完整运行
        }
    }

    /**
     * 解析 DownloadManager 下载成功的 APK 目标文件
     * 兼容 Android 7.0 到 15+ 的公共目录与应用分区存储
     * 使用 use 语法安全关闭数据流，防止文件句柄及内存泄漏
     */
    private fun resolveDownloadedFile(
        context: Context,
        downloadManager: DownloadManager,
        downloadId: Long,
        defaultTargetFile: File
    ): File? {
        // 1. 如果公共目录下的目标文件存在且具有内容，优先返回
        if (defaultTargetFile.exists() && defaultTargetFile.length() > 0L) {
            return defaultTargetFile
        }

        // 2. 尝试从 DownloadManager COLUMN_LOCAL_URI 检索真实本地 file:// 路径
        try {
            val query = DownloadManager.Query().setFilterById(downloadId)
            downloadManager.query(query)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    if (localUriIndex != -1) {
                        val localUriStr = cursor.getString(localUriIndex)
                        if (!localUriStr.isNullOrEmpty()) {
                            val uri = Uri.parse(localUriStr)
                            if (uri.scheme == "file") {
                                val file = File(uri.path ?: "")
                                if (file.exists() && file.length() > 0L) {
                                    return file
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // 3. Android 10+ 分区存储可能将文件保护在 DownloadManager 专属沙盒下
        // 使用 contentResolver.openInputStream 或 openDownloadedFile 流式转存至应用专属目录，确保 FileProvider 具备合法访问权限
        try {
            val downloadedUri = downloadManager.getUriForDownloadedFile(downloadId)
            val inputStream = if (downloadedUri != null) {
                context.contentResolver.openInputStream(downloadedUri)
            } else {
                downloadManager.openDownloadedFile(downloadId)?.let { pfd ->
                    ParcelFileDescriptor.AutoCloseInputStream(pfd)
                }
            }

            if (inputStream != null) {
                val safeDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
                val safeApkFile = File(safeDir, defaultTargetFile.name)
                // 确保输入流与输出流在使用后严格关闭
                inputStream.use { input ->
                    safeApkFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                if (safeApkFile.exists() && safeApkFile.length() > 0L) {
                    return safeApkFile
                }
            }
        } catch (_: Exception) {}

        return if (defaultTargetFile.exists()) defaultTargetFile else null
    }

    /**
     * DownloadManager 不可用时的直接流式下载备选方案
     * 规范使用输入输出流并在 finally 中安全释放，规避内存泄漏
     */
    private fun startDirectDownload(
        context: Context,
        downloadUrl: String,
        versionName: String
    ) {
        val safeDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        val targetFile = File(safeDir, "Aura_$versionName.apk")
        if (targetFile.exists()) {
            try {
                targetFile.delete()
            } catch (_: Exception) {}
        }

        pollingJob?.cancel()
        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            var connection: HttpURLConnection? = null
            try {
                _downloadProgress.value = DownloadProgress(isDownloading = true, progress = 0)
                val url = URL(downloadUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 15000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Aura-App")
                }

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw IOException("下载请求失败 (HTTP $responseCode)")
                }

                val totalBytes = connection.contentLength.toLong()
                var downloadedBytes = 0L

                // 规范关闭输入流和文件输出流，杜绝资源泄漏
                connection.inputStream.use { input ->
                    targetFile.outputStream().use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var lastPercent = 0
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            if (!isActive) {
                                throw IOException("下载任务已取消")
                            }
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            val percent = if (totalBytes > 0) {
                                ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                            } else {
                                0
                            }
                            if (percent != lastPercent) {
                                lastPercent = percent
                                _downloadProgress.value = DownloadProgress(
                                    isDownloading = true,
                                    progress = percent,
                                    bytesDownloaded = downloadedBytes,
                                    totalBytes = totalBytes
                                )
                            }
                        }
                        output.flush()
                    }
                }

                _downloadProgress.value = DownloadProgress(
                    isDownloading = false,
                    progress = 100,
                    bytesDownloaded = if (totalBytes > 0) totalBytes else downloadedBytes,
                    totalBytes = totalBytes,
                    isCompleted = true,
                    downloadedFile = targetFile
                )

                withContext(Dispatchers.Main) {
                    installApk(context, targetFile)
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _downloadProgress.value = DownloadProgress(
                        isDownloading = false,
                        progress = 0,
                        isFailed = true
                    )
                    showToast(context, "更新包下载失败: ${e.localizedMessage ?: "网络异常"}")
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    /**
     * 调起系统 APK 安装程序
     * 涵盖 Android 8.0 未知应用权限、FileProvider 安全 URI 授权、多进程 Intent 权限穿透与异常拦截
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            showToast(context, "未找到下载的安装包，请重新下载")
            return
        }

        // Android 8.0 (API 26) 及以上未知应用来源权限检查与多重异常兜底引导
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canInstall = try {
                context.packageManager.canRequestPackageInstalls()
            } catch (_: Exception) {
                true
            }
            if (!canInstall) {
                showToast(context, "请先允许 Aura 安装未知应用")
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(settingsIntent)
                } catch (_: Exception) {
                    try {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    } catch (e2: Exception) {
                        showToast(context, "无法打开未知应用安装设置界面，请在系统设置中手动开启权限")
                    }
                }
                return
            }
        }

        try {
            // 使用 FileProvider 生成安全 content:// 协议 URI
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // 针对部分定制系统（MIUI/EMUI 等）显式向所有可能的目标安装器组件预先授权 URI 读取权限
            try {
                val resInfoList = context.packageManager.queryIntentActivities(installIntent, 0)
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (_: Exception) {}

            context.startActivity(installIntent)
        } catch (e: Exception) {
            showToast(context, "拉起安装失败: ${e.localizedMessage ?: "未知错误"}")
        }
    }

    /**
     * 线程安全的 Toast 显示工具方法，确保在主线程 Looper 上执行
     */
    private fun showToast(context: Context, message: String) {
        val appContext = context.applicationContext ?: context
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        } else {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
