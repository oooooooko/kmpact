package com.okko.kmpact.presentation.tools

import com.okko.kmpact.domain.model.ToolCommand
import com.okko.kmpact.presentation.base.BaseIntent

/**
 * 工具界面的用户意图
 */
sealed interface ToolsIntent : BaseIntent {
    
    /**
     * 选择命令
     */
    data class SelectCommand(val command: ToolCommand) : ToolsIntent
    
    /**
     * 更新参数
     */
    data class UpdateParameter(val key: String, val value: String) : ToolsIntent
    
    /**
     * 执行命令
     */
    data object ExecuteCommand : ToolsIntent
    
    /**
     * 取消执行
     */
    data object CancelExecution : ToolsIntent
    
    /**
     * 更新输入框内容
     */
    data class UpdateInput(val input: String) : ToolsIntent
    
    /**
     * 发送输入到进程
     */
    data object SendInput : ToolsIntent
    
    /**
     * 清除日志
     */
    data object ClearLogs : ToolsIntent
}
