package com.okko.kmpact.data.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 设置管理器
 * 负责应用设置的读取、保存和管理
 */
object SettingsManager {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    /**
     * 初始化设置
     */
    fun init() {
        loadSettings()
    }
    
    /**
     * 加载设置
     */
    private fun loadSettings() {
        val loaded = loadSettingsFromStorage()
        _settings.value = loaded ?: AppSettings()
    }
    
    /**
     * 保存设置
     */
    fun saveSettings(newSettings: AppSettings): Boolean {
        return try {
            saveSettingsToStorage(newSettings)
            _settings.value = newSettings
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 获取当前设置
     */
    fun getCurrentSettings(): AppSettings {
        return _settings.value
    }
    
    /**
     * 更新下载路径
     */
    fun updateDownloadPath(path: String) {
        val newSettings = _settings.value.copy(downloadPath = path)
        saveSettings(newSettings)
    }
}

/**
 * 从存储加载设置
 */
expect fun loadSettingsFromStorage(): AppSettings?

/**
 * 保存设置到存储
 */
expect fun saveSettingsToStorage(settings: AppSettings)
