package com.okko.kmpact.platform

/**
 * JVM平台的系统路径工具
 */
actual object SystemPaths {
    actual fun getUserHome(): String {
        return System.getProperty("user.home") ?: ""
    }
    
    actual fun getDownloadsPath(): String {
        return getUserHome() + "/Downloads"
    }
    
    actual fun getDesktopPath(): String {
        return getUserHome() + "/Desktop"
    }
}
