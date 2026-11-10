# ==============================================================================
# Aura 18.0 ProGuard / R8 混淆与代码缩减加固规则
# ==============================================================================

# =========================================================
# 1. 修复 App Startup 与 WorkManager 反射初始化（根治冷启动崩溃）
# =========================================================
-keep class * extends androidx.startup.Initializer {
    public <init>();
}
-keep class androidx.startup.** { *; }

-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# =========================================================
# 2. 保护项目真实包名 (com.example.wxxx)
# =========================================================
-keep class com.example.wxxx.** { *; }
-keep class com.example.wxxx.BuildConfig { *; }

# =========================================================
# 3. 数据持久化、图片加载与系统组件
# =========================================================
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**
-keep class coil.** { *; }
-dontwarn coil.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.biometric.** { *; }

# =========================================================
# 4. 抑制三方依赖告警
# =========================================================
-dontwarn androidx.work.**
-dontwarn androidx.startup.**
