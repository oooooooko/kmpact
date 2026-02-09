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
    
    /**
     * 获取设备详细信息
     */
    actual suspend fun getDeviceDetails(serialNumber: String): DeviceInfo? = withContext(Dispatchers.IO) {
        try {
            val adbPath = findAdbPath()
            
            // 获取各种属性
            val name = getProperty(adbPath, serialNumber, "ro.product.name")
            val brand = getProperty(adbPath, serialNumber, "ro.product.brand")
            val model = getProperty(adbPath, serialNumber, "ro.product.model")
            val androidVersion = getProperty(adbPath, serialNumber, "ro.build.version.release")
            val processor = getProperty(adbPath, serialNumber, "ro.product.cpu.abi")
            val kernelVersion = getProperty(adbPath, serialNumber, "ro.build.version.kernel")
            
            // 获取 WiFi 状态
            val wifiEnabled = getShellCommand(adbPath, serialNumber, "settings get global wifi_on")
            val wifi = if (wifiEnabled == "1") "已开启" else "已关闭"
            
            // 获取 IP 地址
            val ipAddress = getShellCommand(adbPath, serialNumber, "ip addr show wlan0")
                .lines()
                .find { it.contains("inet ") }
                ?.trim()
                ?.split(" ")
                ?.getOrNull(1)
                ?.substringBefore("/")
            
            // 获取存储使用情况（转换为GB）
            val storageInfo = getShellCommand(adbPath, serialNumber, "df /data")
                .lines()
                .lastOrNull { it.contains("/data") }
            val storageUsage = storageInfo?.let {
                val parts = it.trim().split("\\s+".toRegex())
                if (parts.size >= 5) {
                    val usedKB = parts[2].toLongOrNull()
                    val totalKB = parts[1].toLongOrNull()
                    if (usedKB != null && totalKB != null) {
                        val usedGB = String.format("%.2f", usedKB / 1024.0 / 1024.0)
                        val totalGB = String.format("%.2f", totalKB / 1024.0 / 1024.0)
                        "${usedGB}GB / ${totalGB}GB"
                    } else null
                } else null
            }
            
            // 获取物理分辨率
            val physicalSize = getShellCommand(adbPath, serialNumber, "wm size")
                .lines()
                .find { it.contains("Physical size:") }
                ?.substringAfter("Physical size:")
                ?.trim()
            
            // 获取当前分辨率
            val overrideSize = getShellCommand(adbPath, serialNumber, "wm size")
                .lines()
                .find { it.contains("Override size:") }
                ?.substringAfter("Override size:")
                ?.trim()
            val resolution = overrideSize ?: physicalSize
            
            // 获取内存信息
            val memInfo = getShellCommand(adbPath, serialNumber, "cat /proc/meminfo")
                .lines()
                .find { it.startsWith("MemTotal:") }
                ?.substringAfter("MemTotal:")
                ?.trim()
                ?.substringBefore(" ")
            val memory = memInfo?.let {
                val memMB = it.toLongOrNull()?.div(1024)
                val memGB = memMB?.div(1024)
                if (memGB != null && memGB > 0) {
                    "${memGB}GB"
                } else if (memMB != null) {
                    "${memMB}MB"
                } else null
            }
            
            DeviceInfo(
                serialNumber = serialNumber,
                model = model ?: "Unknown",
                status = "已连接",
                name = name,
                brand = brand,
                androidVersion = androidVersion,
                processor = processor,
                wifi = wifi,
                ipAddress = ipAddress,
                macAddress = null,  // 移除MAC地址（权限问题）
                storageUsage = storageUsage,
                physicalResolution = physicalSize,
                resolution = resolution,
                memory = memory,
                kernelVersion = kernelVersion
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取设备属性
     */
    private fun getProperty(adbPath: String, serialNumber: String, property: String): String? {
        return try {
            val process = ProcessBuilder(adbPath, "-s", serialNumber, "shell", "getprop", property)
                .redirectErrorStream(true)
                .start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()?.trim()
            process.waitFor()
            
            if (result.isNullOrBlank()) null else result
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 执行 shell 命令
     */
    private fun getShellCommand(adbPath: String, serialNumber: String, command: String): String {
        return try {
            val process = ProcessBuilder(adbPath, "-s", serialNumber, "shell", command)
                .redirectErrorStream(true)
                .start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            
            output
        } catch (e: Exception) {
            ""
        }
    }
}
