package com.okko.kmpact.data.storage

/**
 * 偏好设置存储接口
 */
interface PreferencesStorage {
    /**
     * 保存字符串列表
     */
    fun saveStringList(key: String, value: List<String>)
    
    /**
     * 获取字符串列表
     */
    fun getStringList(key: String): List<String>
    
    /**
     * 删除指定键
     */
    fun remove(key: String)
    
    /**
     * 清空所有数据
     */
    fun clear()
}

/**
 * 平台特定的实现
 */
expect class PreferencesStorageImpl() : PreferencesStorage
