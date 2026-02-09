package com.okko.kmpact.platform

/**
 * 设备信息
 */
data class DeviceInfo(
    val serialNumber: String,
    val model: String,
    val status: String,
    val name: String? = null,
    val brand: String? = null,
    val androidVersion: String? = null,
    val processor: String? = null,
    val wifi: String? = null,
    val ipAddress: String? = null,
    val macAddress: String? = null,
    val storageUsage: String? = null,
    val physicalResolution: String? = null,
    val resolution: String? = null,
    val memory: String? = null,
    val kernelVersion: String? = null
)

/**
 * 设备管理器接口
 */
expect class DeviceManager() {
    
    /**
     * 获取已连接的设备列表
     */
    suspend fun getConnectedDevices(): List<DeviceInfo>
    
    /**
     * 获取设备详细信息
     */
    suspend fun getDeviceDetails(serialNumber: String): DeviceInfo?
}
