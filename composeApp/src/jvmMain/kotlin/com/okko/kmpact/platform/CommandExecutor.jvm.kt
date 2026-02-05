package com.okko.kmpact.platform

import com.okko.kmpact.domain.model.CommandResult
import com.okko.kmpact.domain.model.ToolCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * JVM平台的命令执行器
 * 
 * 负责在JVM平台上执行shell脚本
 */
actual class CommandExecutor {
    
    /**
     * 当前正在执行的进程
     */
    @Volatile
    private var currentProcess: Process? = null
    
    /**
     * 当前进程的输出流写入器
     */
    @Volatile
    private var processWriter: java.io.BufferedWriter? = null
    
    /**
     * 执行命令
     * 
     * @param command 要执行的命令
     * @param parameters 命令参数
     * @param workingDir 工作目录
     * @param onOutput 输出回调
     * @param onNeedInput 需要输入时的回调
     * @return 命令执行结果
     */
    actual suspend fun execute(
        command: ToolCommand,
        parameters: Map<String, String>,
        workingDir: String?,
        onOutput: ((String) -> Unit)?,
        onNeedInput: (() -> Unit)?
    ): CommandResult = withContext(Dispatchers.IO) {
        try {
            // 获取项目根目录
            var projectRoot = System.getProperty("user.dir")
            
            // 如果当前目录是 composeApp，则向上一级
            if (projectRoot.endsWith("composeApp")) {
                projectRoot = File(projectRoot).parent ?: projectRoot
            }
            
            // 构建脚本路径
            // scriptPath 格式: "shell/device-tools/InstallApk.sh"
            // 需要替换 shell 为 androidcmdtools-shell
            val actualScriptPath = command.scriptPath.replace("shell/", "androidcmdtools-shell/")
            val scriptPath = File(projectRoot, actualScriptPath)
            
            if (!scriptPath.exists()) {
                return@withContext CommandResult(
                    success = false,
                    output = "",
                    error = "脚本文件不存在: ${scriptPath.absolutePath}",
                    exitCode = -1
                )
            }
            
            // 确保脚本有执行权限
            scriptPath.setExecutable(true)
            
            // 构建命令
            val processBuilder = ProcessBuilder()
            
            // 根据操作系统选择shell
            val shellCommand = when {
                System.getProperty("os.name").lowercase().contains("win") -> {
                    // Windows使用Git Bash或WSL
                    listOf("bash", scriptPath.absolutePath)
                }
                else -> {
                    // macOS/Linux使用bash
                    // 对于需要TTY的脚本，我们需要特殊处理
                    listOf("bash", scriptPath.absolutePath)
                }
            }
            
            processBuilder.command(shellCommand)
            
            // 设置环境变量，让脚本知道它在非交互模式下运行
            val env = processBuilder.environment()
            env["TERM"] = "dumb"  // 设置TERM避免警告
            env["INTERACTIVE_MODE"] = "false"  // 自定义变量，脚本可以检测
            
            // 设置工作目录
            if (workingDir != null) {
                processBuilder.directory(File(workingDir))
            } else {
                processBuilder.directory(scriptPath.parentFile)
            }
            
            // 设置其他环境变量
            parameters.forEach { (key, value) ->
                env[key] = value
            }
            
            // 合并标准输出和错误输出
            processBuilder.redirectErrorStream(true)
            
            // 启动进程
            val process = processBuilder.start()
            currentProcess = process // 保存进程引用
            
            try {
                // 保存输出流写入器，用于后续发送输入
                processWriter = process.outputStream.bufferedWriter()
                
                // 如果有参数，通过标准输入传递
                if (parameters.isNotEmpty()) {
                    parameters.forEach { (_, value) ->
                        processWriter?.write(value)
                        processWriter?.newLine()
                        processWriter?.flush()
                    }
                }
                
                // 读取输出
                val output = StringBuilder()
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                
                var line: String?
                var lastOutputTime = System.currentTimeMillis()
                var waitingForEnter = false
                
                while (reader.readLine().also { line = it } != null) {
                    line?.let {
                        output.appendLine(it)
                        onOutput?.invoke(it)
                        lastOutputTime = System.currentTimeMillis()
                        
                        // 检测/dev/tty错误，这通常意味着脚本在等待回车
                        if (it.contains("/dev/tty: Device not configured")) {
                            waitingForEnter = true
                            onNeedInput?.invoke()
                        }
                        
                        // 检测是否需要用户输入（通过关键词判断）
                        val lowerLine = it.lowercase()
                        if (lowerLine.contains("请选择") || 
                            lowerLine.contains("请输入") ||
                            lowerLine.contains("按回车") ||
                            lowerLine.contains("是否") ||
                            lowerLine.contains("(y/n)") ||
                            lowerLine.contains("y/n") ||
                            lowerLine.contains("press enter") ||
                            lowerLine.contains("please enter") ||
                            lowerLine.contains("select") ||
                            lowerLine.endsWith("：") ||
                            lowerLine.endsWith(":") ||
                            (lowerLine.matches(Regex(".*\\d+\\..*")) && it.contains("."))) {
                            // 通知需要输入
                            onNeedInput?.invoke()
                        }
                    }
                }
                
                // 等待进程结束
                val exitCode = process.waitFor()
                
                CommandResult(
                    success = exitCode == 0,
                    output = output.toString(),
                    error = if (exitCode != 0) "命令执行失败，退出码: $exitCode" else null,
                    exitCode = exitCode
                )
            } finally {
                processWriter?.close()
                processWriter = null
                currentProcess = null // 清除进程引用
            }
        } catch (e: Exception) {
            processWriter?.close()
            processWriter = null
            currentProcess = null // 清除进程引用
            onOutput?.invoke("❌ 执行异常: ${e.message}")
            CommandResult(
                success = false,
                output = "",
                error = e.message ?: "未知错误",
                exitCode = -1
            )
        }
    }
    
    /**
     * 发送输入到正在执行的进程
     */
    actual fun sendInput(input: String) {
        try {
            processWriter?.let { writer ->
                writer.write(input)
                writer.newLine()
                writer.flush()
                println("✓ 已发送输入: $input")
            } ?: run {
                println("✗ 没有正在执行的进程")
            }
        } catch (e: Exception) {
            println("✗ 发送输入失败: ${e.message}")
        }
    }
    
    /**
     * 取消当前正在执行的命令
     */
    actual fun cancelExecution() {
        currentProcess?.let { process ->
            try {
                // 销毁进程
                process.destroy()
                
                // 如果进程没有立即终止，强制终止
                if (process.isAlive) {
                    Thread.sleep(1000) // 等待1秒
                    if (process.isAlive) {
                        process.destroyForcibly()
                    }
                }
                
                println("✓ 命令已取消")
            } catch (e: Exception) {
                println("✗ 取消命令失败: ${e.message}")
            }
        }
    }
    
    /**
     * 检查脚本是否存在
     */
    actual fun checkScriptExists(scriptPath: String): Boolean {
        var projectRoot = System.getProperty("user.dir")
        
        // 如果当前目录是 composeApp，则向上一级
        if (projectRoot.endsWith("composeApp")) {
            projectRoot = File(projectRoot).parent ?: projectRoot
        }
        
        val actualScriptPath = scriptPath.replace("shell/", "androidcmdtools-shell/")
        val file = File(projectRoot, actualScriptPath)
        
        // 调试信息
        println("=== 脚本路径调试 ===")
        println("原始工作目录: ${System.getProperty("user.dir")}")
        println("调整后根目录: $projectRoot")
        println("原始路径: $scriptPath")
        println("转换路径: $actualScriptPath")
        println("完整路径: ${file.absolutePath}")
        println("文件存在: ${file.exists()}")
        if (!file.exists()) {
            println("父目录存在: ${file.parentFile?.exists()}")
            println("父目录内容: ${file.parentFile?.list()?.joinToString(", ")}")
        }
        println("==================")
        
        return file.exists()
    }
    
    /**
     * 获取脚本绝对路径
     */
    actual fun getScriptAbsolutePath(scriptPath: String): String {
        var projectRoot = System.getProperty("user.dir")
        
        // 如果当前目录是 composeApp，则向上一级
        if (projectRoot.endsWith("composeApp")) {
            projectRoot = File(projectRoot).parent ?: projectRoot
        }
        
        val actualScriptPath = scriptPath.replace("shell/", "androidcmdtools-shell/")
        return File(projectRoot, actualScriptPath).absolutePath
    }
}
