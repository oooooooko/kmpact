package com.okko.kmpact.domain.repository

import com.okko.kmpact.domain.model.ToolCommand

/**
 * 最近使用工具的仓库接口
 */
interface RecentToolsRepository {
    /**
     * 添加工具到最近使用列表
     */
    fun addRecentTool(toolCommand: ToolCommand)
    
    /**
     * 获取最近使用的工具列表
     * @param limit 返回的最大数量
     */
    fun getRecentTools(limit: Int = 10): List<ToolCommand>
    
    /**
     * 清空最近使用列表
     */
    fun clearRecentTools()
}
