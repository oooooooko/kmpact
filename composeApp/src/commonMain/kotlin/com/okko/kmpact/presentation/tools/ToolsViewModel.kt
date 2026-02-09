package com.okko.kmpact.presentation.tools

import androidx.lifecycle.viewModelScope
import com.okko.kmpact.data.repository.RecentToolsRepositoryImpl
import com.okko.kmpact.domain.model.ToolCommand
import com.okko.kmpact.domain.repository.RecentToolsRepository
import com.okko.kmpact.domain.usecase.ExecuteCommandUseCaseImpl
import com.okko.kmpact.presentation.base.BaseViewModel
import com.okko.kmpact.ui.components.LogEntry
import com.okko.kmpact.ui.components.LogLevel
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 工具ViewModel
 * 
 * 管理所有AndroidCmdTools工具的执行
 */
class ToolsViewModel : BaseViewModel<ToolsUiState, ToolsIntent, ToolsEffect>(
    initialState = ToolsUiState()
) {
    
    private val executeCommandUseCase = ExecuteCommandUseCaseImpl()
    private val recentToolsRepository: RecentToolsRepository = RecentToolsRepositoryImpl.getInstance()
    
    init {
        addLog(LogLevel.INFO, "工具界面已就绪")
    }
    
    override fun handleIntent(intent: ToolsIntent) {
        when (intent) {
            is ToolsIntent.SelectCommand -> handleSelectCommand(intent.command)
            is ToolsIntent.UpdateParameter -> handleUpdateParameter(intent.key, intent.value)
            is ToolsIntent.ExecuteCommand -> handleExecuteCommand()
            is ToolsIntent.CancelExecution -> handleCancelExecution()
            is ToolsIntent.UpdateInput -> handleUpdateInput(intent.input)
            is ToolsIntent.SendInput -> handleSendInput()
            is ToolsIntent.ClearLogs -> handleClearLogs()
        }
    }
    
    private fun handleSelectCommand(command: ToolCommand) {
        updateState { 
            copy(
                selectedCommand = command,
                parameters = emptyMap() // 清空之前的参数
            ) 
        }
        addLog(LogLevel.INFO, "已选择工具: ${command.name}")
    }
    
    private fun handleUpdateParameter(key: String, value: String) {
        updateState {
            copy(parameters = parameters + (key to value))
        }
    }
    
    private fun handleExecuteCommand() {
        val command = currentState.selectedCommand
        if (command == null) {
            addLog(LogLevel.ERROR, "请先选择一个工具")
            return
        }
        
        // 添加到最近使用列表
        recentToolsRepository.addRecentTool(command)
        
        viewModelScope.launch {
            addLog(LogLevel.COMMAND, "执行命令: ${command.name}")
            updateState { copy(isExecuting = true, needsInput = false) }
            
            val result = executeCommandUseCase.execute(
                command = command,
                parameters = currentState.parameters,
                onOutput = { output ->
                    addLog(LogLevel.OUTPUT, output)
                },
                onNeedInput = {
                    // 检测到需要输入
                    updateState { copy(needsInput = true) }
                    addLog(LogLevel.INFO, "💡 等待用户输入...")
                }
            )
            
            result.fold(
                onSuccess = { commandResult ->
                    if (commandResult.success) {
                        addLog(LogLevel.SUCCESS, "命令执行成功")
                        sendEffect(ToolsEffect.ShowSuccess("命令执行成功"))
                    } else {
                        addLog(LogLevel.ERROR, "命令执行失败: ${commandResult.error}")
                        sendEffect(ToolsEffect.ShowError("命令执行失败", commandResult.error ?: "未知错误"))
                    }
                },
                onFailure = { error ->
                    addLog(LogLevel.ERROR, "执行异常: ${error.message}")
                    sendEffect(ToolsEffect.ShowError("执行异常", error.message ?: "未知错误"))
                }
            )
            
            updateState { copy(isExecuting = false, needsInput = false) }
        }
    }
    
    private fun handleUpdateInput(input: String) {
        updateState { copy(currentInput = input) }
    }
    
    private fun handleSendInput() {
        val input = currentState.currentInput
        
        // 允许发送空行（用户只按回车）
        addLog(LogLevel.INFO, "→ ${if (input.isEmpty()) "(回车)" else input}")
        executeCommandUseCase.sendInput(input)
        // 不要立即禁用输入框，保持启用状态以便继续输入
        // needsInput 会在命令执行完成后自动设置为 false
        updateState { copy(currentInput = "") }
    }
    
    private fun handleCancelExecution() {
        if (currentState.isExecuting) {
            addLog(LogLevel.WARNING, "正在取消命令执行...")
            executeCommandUseCase.cancelExecution()
            updateState { copy(isExecuting = false) }
            addLog(LogLevel.INFO, "命令已取消")
        }
    }
    
    private fun handleClearLogs() {
        updateState { copy(logs = emptyList()) }
    }
    
    /**
     * 添加日志
     */
    private fun addLog(level: LogLevel, message: String) {
        val timestamp = getCurrentTimestamp()
        val logEntry = LogEntry(
            timestamp = timestamp,
            level = level,
            message = message
        )
        updateState { 
            copy(logs = logs + logEntry) 
        }
    }
    
    /**
     * 获取当前时间戳
     */
    private fun getCurrentTimestamp(): String {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        val second = localDateTime.second.toString().padStart(2, '0')
        return "$hour:$minute:$second"
    }
    
    /**
     * 获取最近使用的工具列表
     */
    fun getRecentTools(): List<ToolCommand> {
        return recentToolsRepository.getRecentTools(10)
    }
}
