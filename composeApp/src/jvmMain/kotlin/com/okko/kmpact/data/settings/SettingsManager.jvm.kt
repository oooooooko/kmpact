package com.okko.kmpact.data.settings

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 设置存储数据类（用于序列化）
 */
@Serializable
private data class SettingsData(
    val downloadPath: String = ""
)

/**
 * 设置文件路径
 */
private val settingsFile: File by lazy {
    val userHome = System.getProperty("user.home")
    val configDir = File(userHome, ".androidcmdtools")
    if (!configDir.exists()) {
        configDir.mkdirs()
    }
    File(configDir, "settings.json")
}

/**
 * JSON 序列化器
 */
private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

/**
 * 从存储加载设置（JVM 实现）
 */
actual fun loadSettingsFromStorage(): AppSettings? {
    return try {
        if (!settingsFile.exists()) {
            return null
        }
        
        val jsonString = settingsFile.readText()
        val data = json.decodeFromString<SettingsData>(jsonString)
        
        AppSettings(
            downloadPath = data.downloadPath.ifEmpty { getDefaultDownloadPath() }
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 保存设置到存储（JVM 实现）
 */
actual fun saveSettingsToStorage(settings: AppSettings) {
    try {
        val data = SettingsData(
            downloadPath = settings.downloadPath
        )
        
        val jsonString = json.encodeToString(data)
        settingsFile.writeText(jsonString)
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}
