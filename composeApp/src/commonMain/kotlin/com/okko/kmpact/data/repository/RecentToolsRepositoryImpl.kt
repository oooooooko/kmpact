package com.okko.kmpact.data.repository

import com.okko.kmpact.data.storage.PreferencesStorageImpl
import com.okko.kmpact.domain.model.ToolCommand
import com.okko.kmpact.domain.model.ToolCommands
import com.okko.kmpact.domain.repository.RecentToolsRepository

/**
 * 最近使用工具的仓库实现（持久化存储）
 */
class RecentToolsRepositoryImpl private constructor() : RecentToolsRepository {
    private val storage = PreferencesStorageImpl()
    private val recentToolIds = mutableListOf<String>()
    
    companion object {
        private const val KEY_RECENT_TOOLS = "recent_tools"
        private const val MAX_RECENT_TOOLS = 10
        
        @Volatile
        private var instance: RecentToolsRepositoryImpl? = null
        
        fun getInstance(): RecentToolsRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: RecentToolsRepositoryImpl().also { instance = it }
            }
        }
    }
    
    init {
        // 从存储中加载最近使用的工具
        loadFromStorage()
    }
    
    private fun loadFromStorage() {
        val savedIds = storage.getStringList(KEY_RECENT_TOOLS)
        recentToolIds.clear()
        recentToolIds.addAll(savedIds)
    }
    
    private fun saveToStorage() {
        storage.saveStringList(KEY_RECENT_TOOLS, recentToolIds)
    }
    
    override fun addRecentTool(toolCommand: ToolCommand) {
        // 移除已存在的相同工具
        recentToolIds.remove(toolCommand.id)
        
        // 添加到列表开头
        recentToolIds.add(0, toolCommand.id)
        
        // 保持最多10个
        if (recentToolIds.size > MAX_RECENT_TOOLS) {
            recentToolIds.removeAt(recentToolIds.size - 1)
        }
        
        // 保存到持久化存储
        saveToStorage()
    }
    
    override fun getRecentTools(limit: Int): List<ToolCommand> {
        return recentToolIds
            .take(limit)
            .mapNotNull { id -> ToolCommands.getCommandById(id) }
    }
    
    override fun clearRecentTools() {
        recentToolIds.clear()
        saveToStorage()
    }
}
