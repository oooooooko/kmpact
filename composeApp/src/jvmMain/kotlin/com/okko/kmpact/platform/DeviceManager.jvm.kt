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
     * 获取已连接的设备列表
     */
    actual suspend fun getConnectedDevices(): List<DeviceInfo> = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("adb", "devices", "-l").start()
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
