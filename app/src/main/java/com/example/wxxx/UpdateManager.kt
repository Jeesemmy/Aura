package com.example.wxxx

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
 * ⚙️ Aura 19.0 应用内更新管理引擎
 * 负责 GitHub Releases 最新版本检测、版本号比对、IO 协程流式下载及 FileProvider 拉起系统安装
 */
object UpdateManager {

    private const val GITHUB_API_URL = "https://api.github.com/repos/Jeesemmy/Aura/releases/latest"
    val AURA_CURRENT_VERSION: String = runCatching {
        BuildConfig.VERSION_NAME
    }.getOrDefault("19.0")

    /**
     * 版本更新信息数据载体
     */
    data class ReleaseInfo(
        val rawTag: String,          // 原始 tag 标识（如 "Aura" 或 "v18.0"）
        val versionName: String,     // 提取出的干净版本号（如 "18.0"）
        val title: String,           // Release 标题名称（如 "Aura 18.0"）
        val changelog: String,       // 更新日志内容（body）
        val downloadUrl: String,     // APK 文件直链（browser_download_url）
        val apkSize: Long,           // APK 文件字节大小
        val hasNewVersion: Boolean   // 相对当前客户端是否有新版本
    )

    // 🌟 全局响应式版本更新状态：是否发现新版本 (StateFlow 跨页面共享)
    private val _hasNewVersion = MutableStateFlow(false)
    val hasNewVersion: StateFlow<Boolean> = _hasNewVersion.asStateFlow()

    // 🌟 全局最新 Release 数据缓存
    private val _latestReleaseInfo = MutableStateFlow<ReleaseInfo?>(null)
    val latestReleaseInfo: StateFlow<ReleaseInfo?> = _latestReleaseInfo.asStateFlow()

    /**
     * 允许外部手动同步或重置新版本状态（如用户在更新详情页确认后或远程推送触发）
     */
    fun setHasNewVersion(hasNew: Boolean) {
        _hasNewVersion.value = hasNew
    }

    /**
     * 🌟 [阶段二 OTA 真实网络流] 真实网络流式下载 APK，实时通过 Flow 向外不断 emit 进度 (0.0f - 1.0f)
     * - 支持 GitHub 301/302 重定向安全解析
     * - 协程取消响应 (ensureActive) 与未完成残缺文件自动清理
     * - 运行在 Dispatchers.IO，绝不阻塞主线程
     * @param url 远程 APK 链接
     * @param targetFile 保存的目标 File (通常为 context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)/update.apk)
     */
    fun downloadApk(url: String, targetFile: File): Flow<Float> = flow {
        targetFile.parentFile?.mkdirs()
        if (targetFile.exists()) {
            targetFile.delete()
        }

        var connection: HttpURLConnection? = null
        try {
            var currentUrl = url
            var redirects = 0
            while (redirects < 5) {
                val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 15000
                    instanceFollowRedirects = false
                    setRequestProperty("User-Agent", "Aura-App")
                    setRequestProperty("Accept", "*/*")
                }
                val code = conn.responseCode
                if (code in 300..399) {
                    val location = conn.getHeaderField("Location") ?: break
                    conn.disconnect()
                    currentUrl = location
                    redirects++
                } else {
                    connection = conn
                    break
                }
            }

            val conn = connection ?: throw IOException("无法连接至下载源")
            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                throw IOException("下载请求失败 (HTTP $responseCode)")
            }

            val totalBytes = conn.contentLength.toLong()
            var downloadedBytes = 0L

            emit(0.0f)

            conn.inputStream.use { input ->
                targetFile.outputStream().use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var bytesRead: Int
                    var lastEmittedProgress = 0f

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        currentCoroutineContext().ensureActive()
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            val progress = (downloadedBytes.toFloat() / totalBytes).coerceIn(0.0f, 1.0f)
                            if (progress - lastEmittedProgress >= 0.005f || progress >= 1.0f) {
                                lastEmittedProgress = progress
                                emit(progress)
                            }
                        }
                    }
                    output.flush()
                }
            }

            emit(1.0f)
        } catch (e: Exception) {
            try {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
            } catch (_: Exception) {}
            throw e
        } finally {
            connection?.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 请求 GitHub Releases API 并对比版本号
     * 具备完善的超时配置、网络异常/无网络兜底和 JSON 防御性解析
     * @param currentVersion 当前运行的应用版本号（默认为 AURA_CURRENT_VERSION "19.0"）
     */
    suspend fun checkUpdate(currentVersion: String = AURA_CURRENT_VERSION): Result<ReleaseInfo> {
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

                // 提取版本号数字：优先从 tag_name 提取（如 "v18.0" -> "18.0"）
                // 如果 tag_name 只是 "Aura" 无数字，则从 title 中提取（如 "Aura 18.0" -> "18.0"）
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

                val releaseInfo = ReleaseInfo(
                    rawTag = rawTag,
                    versionName = cleanRemoteVersion,
                    title = releaseTitle,
                    changelog = changelog,
                    downloadUrl = downloadUrl,
                    apkSize = apkSize,
                    hasNewVersion = hasNew
                )

                // 🌟 同步至全局可观察状态流，驱动 UI (如设置中心灯牌等) 实时响应
                _latestReleaseInfo.value = releaseInfo
                _hasNewVersion.value = hasNew

                Result.success(releaseInfo)
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
     * 版本号比对逻辑：支持如 "17.0" 对比 "18.0"、"18.0.1" 等
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
