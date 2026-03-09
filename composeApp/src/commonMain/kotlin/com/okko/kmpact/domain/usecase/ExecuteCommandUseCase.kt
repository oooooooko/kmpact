package com.okko.kmpact.domain.usecase

import com.okko.kmpact.domain.model.CommandResult
import com.okko.kmpact.domain.model.ToolCommand
import com.okko.kmpact.platform.CommandExecutor

/**
 * 执行命令UseCase
 * 
 * 职责：
 * - 执行AndroidCmdTools脚本
 * - 处理命令输出
 * - 返回执行结果
 */
interface ExecuteCommandUseCase {
    
    /**
     * 执行命令
     * 
     * @param command 要执行的命令
     * @param parameters 命令参数（可选）
     * @param onOutput 输出回调（实时输出）
     * @param onNeedInput 需要输入时的回调
     * @return 命令执行结果
     */
    suspend fun execute(
        command: ToolCommand,
        parameters: Map<String, String> = emptyMap(),
        onOutput: ((String) -> Unit)? = null,
        onNeedInput: (() -> Unit)? = null
    ): Result<CommandResult>
    
    /**
     * 发送输入到正在执行的命令
     */
    fun sendInput(input: String)
    
    /**
     * 取消当前正在执行的命令
     */
    fun cancelExecution()
}

/**
 * 执行命令UseCase实现
 * 
 * 使用平台特定的CommandExecutor执行脚本
 */
class ExecuteCommandUseCaseImpl(
    private val commandExecutor: CommandExecutor = CommandExecutor()
) : ExecuteCommandUseCase {
    
    override suspend fun execute(
        command: ToolCommand,
        parameters: Map<String, String>,
        onOutput: ((String) -> Unit)?,
        onNeedInput: (() -> Unit)?
    ): Result<CommandResult> {
        return try {
            // 脚本路径已经是正确的，不需要转换
            val actualScriptPath = command.scriptPath
            
            // 输出命令信息
            onOutput?.invoke("⏳ 正在执行命令: ${command.name}")
            onOutput?.invoke("📂 脚本路径: $actualScriptPath")
            
            // 检查脚本是否存在
            command.scriptPath?.let {
                if (!commandExecutor.checkScriptExists(it)) {
                    val error = "脚本文件不存在: $actualScriptPath"
                    onOutput?.invoke("❌ $error")
                    return Result.failure(Exception(error))
                }
            }
            
            onOutput?.invoke("✅ 脚本文件存在")
            
            // 输出参数信息
            if (parameters.isNotEmpty()) {
                onOutput?.invoke("📋 参数:")
                parameters.forEach { (key, value) ->
                    // 隐藏敏感信息（密码）
                    val displayValue = if (key.lowercase().contains("password")) {
                        "********"
                    } else {
                        value
                    }
                    onOutput?.invoke("  - $key: $displayValue")
                }
            }
            
            // 执行命令
            val result = commandExecutor.execute(
                command = command,
                parameters = parameters,
                onOutput = onOutput,
                onNeedInput = onNeedInput
            )
            
            // 输出结果
            if (result.success) {
                onOutput?.invoke("✅ 命令执行成功")
            } else {
                onOutput?.invoke("❌ 命令执行失败: ${result.error}")
            }
            
            Result.success(result)
        } catch (e: Exception) {
            onOutput?.invoke("❌ 命令执行异常: ${e.message}")
            Result.failure(e)
        }
    }
    
    override fun sendInput(input: String) {
        commandExecutor.sendInput(input)
    }
    
    override fun cancelExecution() {
        commandExecutor.cancelExecution()
    }
}
