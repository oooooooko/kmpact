package com.okko.kmpact.data.settings

/**
 * 应用设置数据类
 */
data class AppSettings(
    val downloadPath: String = getDefaultDownloadPath()
)

/**
 * 获取默认下载路径
 */
expect fun getDefaultDownloadPath(): String

/**
 * 验证路径是否存在
 */
expect fun validatePath(path: String): Boolean

/**
 * 选择文件夹
 */
expect fun selectFolder(): String?
