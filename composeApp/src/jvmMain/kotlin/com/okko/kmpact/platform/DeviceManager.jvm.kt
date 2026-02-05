package com.okko.kmpact.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * JVM 平台的设备管理器实现
 */
actual class DeviceManager {
    
    /**
     * 查找 adb 命令的路径
     */
    private fun findAdbPath(): String {
        // 1. 尝试直接使用 adb（如果在 PATH 中）
        try {
            val process = ProcessBuilder("which", "adb").start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val path = reader.readLine()
            process.waitFor()
            if (path != null && path.isNotBlank()) {
                return path.trim()
            }
        } catch (e: Exception) {
            // 忽略
        }
        
        // 2. 检查常见的 Android SDK 路径
        val possiblePaths = mutableListOf<String>()
        
        // 用户主目录下的 Android SDK
        val userHome = System.getProperty("user.home")
        possiblePaths.add("$userHome/Library/Android/sdk/platform-tools/adb")  // macOS
        possiblePaths.add("$userHome/Android/Sdk/platform-tools/adb")  // Linux
        possiblePaths.add("$userHome/AppData/Local/Android/Sdk/platform-tools/adb.exe")  // Windows
        
        // ANDROID_HOME 环境变量
        val androidHome = System.getenv("ANDROID_HOME")
        if (androidHome != null) {
            possiblePaths.add("$androidHome/platform-tools/adb")
            possiblePaths.add("$androidHome/platform-tools/adb.exe")
        }
        
        // ANDROID_SDK_ROOT 环境变量
        val androidSdkRoot = System.getenv("ANDROID_SDK_ROOT")
        if (androidSdkRoot != null) {
            possiblePaths.add("$androidSdkRoot/platform-tools/adb")
            possiblePaths.add("$androidSdkRoot/platform-tools/adb.exe")
        }
        
        // 查找第一个存在的路径
        for (path in possiblePaths) {
            val file = java.io.File(path)
            if (file.exists() && file.canExecute()) {
                return path
            }
        }
        
        // 如果都找不到，返回 "adb"，让系统尝试从 PATH 中查找
        return "adb"
    }
    
    /**
     * 获取已连接的设备列表
     */
    actual suspend fun getConnectedDevices(): List<DeviceInfo> = withContext(Dispatchers.IO) {
        try {
            val adbPath = findAdbPath()
            
            val process = ProcessBuilder(adbPath, "devices", "-l")
                .redirectErrorStream(true)
                .start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val devices = mutableListOf<DeviceInfo>()
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let { l ->
                    // 跳过标题行和空行
                    if (l.isNotBlank() && !l.startsWith("List of devices")) {
                        val parts = l.trim().split("\\s+".toRegex())
                        if (parts.size >= 2 && parts[1] == "device") {
                            val serialNumber = parts[0]
                            // 提取 model 信息
                            val model = parts.find { it.startsWith("model:") }
                                ?.substringAfter("model:") ?: "Unknown"
                            
                            devices.add(
                                DeviceInfo(
                                    serialNumber = serialNumber,
                                    model = model,
                                    status = "已连接"
                                )
                            )
                        }
                    }
                }
            }
            
            process.waitFor()
            devices
        } catch (e: Exception) {
            emptyList()
        }
    }
}
