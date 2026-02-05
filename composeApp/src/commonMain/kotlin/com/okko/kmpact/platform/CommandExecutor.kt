package com.okko.kmpact.platform

import com.okko.kmpact.domain.model.CommandResult
import com.okko.kmpact.domain.model.ToolCommand

/**
 * 命令执行器（跨平台）
 * 
 * 使用expect/actual机制实现平台特定的命令执行
 */
expect class CommandExecutor() {
    
    /**
     * 执行命令
     * 
     * @param command 要执行的命令
     * @param parameters 命令参数
     * @param workingDir 工作目录（可选）
     * @param onOutput 输出回调（实时输出）
     * @param onNeedInput 需要输入时的回调
     * @return 命令执行结果
     */
    suspend fun execute(
        command: ToolCommand,
        parameters: Map<String, String> = emptyMap(),
        workingDir: String? = null,
        onOutput: ((String) -> Unit)? = null,
        onNeedInput: (() -> Unit)? = null
    ): CommandResult
    
    /**
     * 发送输入到正在执行的进程
     * 
     * @param input 要发送的输入内容
     */
    fun sendInput(input: String)
    
    /**
     * 取消当前正在执行的命令
     */
    fun cancelExecution()
    
    /**
     * 检查脚本是否存在
     */
    fun checkScriptExists(scriptPath: String): Boolean
    
    /**
     * 获取脚本绝对路径
     */
    fun getScriptAbsolutePath(scriptPath: String): String
}
