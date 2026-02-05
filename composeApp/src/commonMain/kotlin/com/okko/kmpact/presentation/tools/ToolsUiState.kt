package com.okko.kmpact.presentation.tools

import com.okko.kmpact.domain.model.ToolCommand
import com.okko.kmpact.presentation.base.BaseUiState
import com.okko.kmpact.ui.components.LogEntry

/**
 * 工具界面的UI状态
 */
data class ToolsUiState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    
    /**
     * 当前选中的命令
     */
    val selectedCommand: ToolCommand? = null,
    
    /**
     * 命令参数
     */
    val parameters: Map<String, String> = emptyMap(),
    
    /**
     * 是否正在执行
     */
    val isExecuting: Boolean = false,
    
    /**
     * 终端日志
     */
    val logs: List<LogEntry> = emptyList(),
    
    /**
     * 是否需要用户输入
     */
    val needsInput: Boolean = false,
    
    /**
     * 当前输入框的值
     */
    val currentInput: String = ""
) : BaseUiState
