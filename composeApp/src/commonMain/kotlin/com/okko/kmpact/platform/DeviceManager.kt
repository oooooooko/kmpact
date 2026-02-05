package com.okko.kmpact.platform

/**
 * 设备信息
 */
data class DeviceInfo(
    val serialNumber: String,
    val model: String,
    val status: String
)

/**
 * 设备管理器接口
 */
expect class DeviceManager() {
    
    /**
     * 获取已连接的设备列表
     */
    suspend fun getConnectedDevices(): List<DeviceInfo>
}
