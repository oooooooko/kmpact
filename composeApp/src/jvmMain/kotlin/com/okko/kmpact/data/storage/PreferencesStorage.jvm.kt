package com.okko.kmpact.data.storage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.prefs.Preferences

/**
 * 存储实现
 */
actual class PreferencesStorageImpl : PreferencesStorage {
    private val prefs = Preferences.userNodeForPackage(PreferencesStorageImpl::class.java)
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    override fun saveStringList(key: String, value: List<String>) {
        val jsonString = json.encodeToString(value)
        prefs.put(key, jsonString)
        prefs.flush()
    }
    
    override fun getStringList(key: String): List<String> {
        val jsonString = prefs.get(key, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<String>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override fun remove(key: String) {
        prefs.remove(key)
        prefs.flush()
    }
    
    override fun clear() {
        prefs.clear()
        prefs.flush()
    }
}
