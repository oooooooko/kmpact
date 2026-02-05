package com.okko.kmpact.platform

/**
 * 系统路径工具（跨平台）
 */
expect object SystemPaths {
    /**
     * 获取用户主目录
     */
    fun getUserHome(): String
    
    /**
     * 获取下载目录
     */
    fun getDownloadsPath(): String
    
    /**
     * 获取桌面目录
     */
    fun getDesktopPath(): String
}
