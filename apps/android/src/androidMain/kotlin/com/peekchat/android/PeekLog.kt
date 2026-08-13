package com.peekchat.android

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 内置日志机制 — 把关键步骤写到文件，方便用户分享给团队分析。
 *
 * 日志文件位于 app 外部存储的 Documents/peek-chat/logs/ 目录，
 * Andy 可以直接用文件管理器找到，或通过 App 内的导出入口分享。
 */
object PeekLog {

    private const val TAG = "PeekLog"
    private var logDir: File? = null
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    /** 初始化日志目录。在 Application / MainActivity.onCreate 调用。 */
    fun init(context: Context) {
        if (logDir != null) return
        val dir = context.getExternalFilesDir(null)?.let { base ->
            File(base, "logs")
        } ?: File(context.filesDir, "logs")
        dir.mkdirs()
        logDir = dir
        Log.i(TAG, "Log dir: ${dir.absolutePath}")
    }

    /** 记录一条关键事件。同步写文件，保证顺序。 */
    fun log(tag: String, message: String) {
        val line = "${formatter.format(Date())} [$tag] $message"
        Log.i(tag, message)
        synchronized(this) {
            try {
                val dir = logDir ?: return
                val file = File(dir, "peekchat.log")
                file.appendText(line + "\n")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write log", e)
            }
        }
    }

    /** 记录异常。 */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        val t = throwable?.let { "\n    ${it::class.simpleName}: ${it.message}" } ?: ""
        log(tag, "ERROR: $message$t")
    }

    /** 获取日志文件路径，供导出/分享。 */
    fun getLogFile(): File? {
        return logDir?.let { File(it, "peekchat.log") }
    }
}
