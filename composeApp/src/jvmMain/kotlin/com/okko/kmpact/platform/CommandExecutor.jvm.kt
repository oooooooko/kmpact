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
            // 获取脚本路径
            val actualScriptPath = command.scriptPath.replace("shell/", "androidcmdtools-shell/")
            val scriptPath = findScriptPath(actualScriptPath)
            
            if (scriptPath == null || !scriptPath.exists()) {
                val possiblePaths = findAllPossiblePaths(actualScriptPath)
                val errorMsg = "脚本文件不存在: $actualScriptPath\n" +
                        "查找路径: ${scriptPath?.absolutePath ?: "未找到"}\n" +
                        "当前工作目录: ${System.getProperty("user.dir")}\n" +
                        "应用路径: ${getApplicationPath()}\n" +
                        "JAR路径: ${getJarPath()}\n" +
                        "尝试的所有路径:\n${possiblePaths.joinToString("\n") { "  - ${it.absolutePath} (存在: ${it.exists()})" }}"
                onOutput?.invoke(errorMsg)
                return@withContext CommandResult(
                    success = false,
                    output = "",
                    error = errorMsg,
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
            
            // 设置 PATH 环境变量，包含 Android SDK 工具路径
            val currentPath = env["PATH"] ?: ""
            val additionalPaths = mutableListOf<String>()
            
            // 添加常见的 Android SDK 路径
            val userHome = System.getProperty("user.home")
            additionalPaths.add("$userHome/Library/Android/sdk/platform-tools")  // macOS
            additionalPaths.add("$userHome/Android/Sdk/platform-tools")  // Linux
            additionalPaths.add("$userHome/AppData/Local/Android/Sdk/platform-tools")  // Windows
            
            // 从环境变量获取 Android SDK 路径
            System.getenv("ANDROID_HOME")?.let { androidHome ->
                additionalPaths.add("$androidHome/platform-tools")
            }
            System.getenv("ANDROID_SDK_ROOT")?.let { sdkRoot ->
                additionalPaths.add("$sdkRoot/platform-tools")
            }
            
            // 合并 PATH
            val newPath = (additionalPaths + currentPath.split(":")).joinToString(":")
            env["PATH"] = newPath
            
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
        val actualScriptPath = scriptPath.replace("shell/", "androidcmdtools-shell/")
        val file = findScriptPath(actualScriptPath)
        return file?.exists() ?: false
    }
    
    /**
     * 获取脚本绝对路径
     */
    actual fun getScriptAbsolutePath(scriptPath: String): String {
        val actualScriptPath = scriptPath.replace("shell/", "androidcmdtools-shell/")
        val file = findScriptPath(actualScriptPath)
        return file?.absolutePath ?: "未找到"
    }
    
    /**
     * 获取JAR文件路径
     */
    private fun getJarPath(): String {
        return try {
            CommandExecutor::class.java.protectionDomain.codeSource.location.toURI().path
        } catch (e: Exception) {
            "无法获取"
        }
    }
    
    /**
     * 获取所有可能的路径（用于调试）
     */
    private fun findAllPossiblePaths(scriptPath: String): List<File> {
        val possiblePaths = mutableListOf<File>()
        
        // 1. JAR文件所在目录
        try {
            val jarPath = CommandExecutor::class.java.protectionDomain.codeSource.location.toURI().path
            val jarFile = File(jarPath)
            
            if (jarFile.path.contains(".app/Contents/")) {
                val appDir = jarFile.parentFile
                possiblePaths.add(File(appDir, scriptPath))
                
                val contentsDir = appDir.parentFile
                possiblePaths.add(File(contentsDir, scriptPath))
                
                var current = appDir
                while (current != null && !current.name.endsWith(".app")) {
                    current = current.parentFile
                }
                if (current != null) {
                    possiblePaths.add(File(current, scriptPath))
                    possiblePaths.add(File(current.parentFile, scriptPath))
                }
            } else {
                possiblePaths.add(File(jarFile.parentFile, scriptPath))
            }
        } catch (e: Exception) {
            // 忽略
        }
        
        // 2. 当前工作目录
        val workingDir = System.getProperty("user.dir")
        possiblePaths.add(File(workingDir, scriptPath))
        
        // 3. 如果在composeApp目录，向上一级
        if (workingDir.endsWith("composeApp")) {
            val parent = File(workingDir).parent
            if (parent != null) {
                possiblePaths.add(File(parent, scriptPath))
            }
        }
        
        // 4. 用户主目录
        val userHome = System.getProperty("user.home")
        possiblePaths.add(File(userHome, "Library/Application Support/AndroidCmdTools/$scriptPath"))
        
        return possiblePaths
    }
    
    /**
     * 获取应用程序路径
     * 支持开发环境和打包后的环境
     */
    private fun getApplicationPath(): String {
        // 尝试获取JAR文件路径
        val jarPath = CommandExecutor::class.java.protectionDomain.codeSource.location.toURI().path
        val jarFile = File(jarPath)
        
        return when {
            // 打包后的macOS应用: /Applications/AndroidCmdTools.app/Contents/app/
            jarFile.path.contains(".app/Contents/") -> {
                // 向上查找到.app目录
                var current = jarFile.parentFile
                while (current != null && !current.name.endsWith(".app")) {
                    current = current.parentFile
                }
                current?.parent ?: jarFile.parent
            }
            // 开发环境
            else -> {
                var projectRoot = System.getProperty("user.dir")
                if (projectRoot.endsWith("composeApp")) {
                    File(projectRoot).parent ?: projectRoot
                } else {
                    projectRoot
                }
            }
        }
    }
    
    /**
     * 查找脚本文件路径
     * 按优先级查找：
     * 1. 打包后的资源目录（macOS: .app/Contents/app/）
     * 2. 开发环境的项目根目录
     */
    private fun findScriptPath(scriptPath: String): File? {
        val possiblePaths = mutableListOf<File>()
        try {
            val jarPath = CommandExecutor::class.java.protectionDomain.codeSource.location.toURI().path
            val jarFile = File(jarPath)

            if (jarFile.path.contains(".app/Contents/")) {
                val appDir = jarFile.parentFile
                val scriptInApp = File(appDir, scriptPath)
                possiblePaths.add(scriptInApp)
                
                // 如果在app目录找到了，直接返回
                if (scriptInApp.exists()) {
                    return scriptInApp
                }
                
                // 在Contents目录下查找
                val contentsDir = appDir.parentFile
                possiblePaths.add(File(contentsDir, scriptPath))
                
                // 在.app目录下查找
                var current = appDir
                while (current != null && !current.name.endsWith(".app")) {
                    current = current.parentFile
                }
                if (current != null) {
                    possiblePaths.add(File(current, scriptPath))
                    possiblePaths.add(File(current.parentFile, scriptPath))
                }
            } else {
                // 开发环境或其他打包方式
                val scriptInJarDir = File(jarFile.parentFile, scriptPath)
                possiblePaths.add(scriptInJarDir)
            }
        } catch (e: Exception) {
        }
        
        // 2. 当前工作目录（仅在开发环境有效）
        val workingDir = System.getProperty("user.dir")
        if (workingDir != "/" && workingDir != "\\") {  // 排除根目录
            possiblePaths.add(File(workingDir, scriptPath))
            
            // 3. 如果在composeApp目录，向上一级
            if (workingDir.endsWith("composeApp")) {
                val parent = File(workingDir).parent
                if (parent != null) {
                    possiblePaths.add(File(parent, scriptPath))
                }
            }
        }
        
        // 4. 用户主目录下的应用支持目录（备用方案）
        val userHome = System.getProperty("user.home")
        possiblePaths.add(File(userHome, "Library/Application Support/AndroidCmdTools/$scriptPath"))
        
        // 查找第一个存在的路径
        return possiblePaths.firstOrNull { it.exists() }
    }

}
