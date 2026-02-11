package com.okko.kmpact.data.settings

import java.io.File
import javax.swing.JFileChooser

/**
 * 获取默认下载路径（JVM 实现）
 */
actual fun getDefaultDownloadPath(): String {
    return System.getProperty("user.home") + File.separator + "Downloads"
}

/**
 * 验证路径是否存在（JVM 实现）
 */
actual fun validatePath(path: String): Boolean {
    return try {
        val file = File(path)
        file.exists() && file.isDirectory
    } catch (e: Exception) {
        false
    }
}

/**
 * 选择文件夹（JVM 实现）
 */
actual fun selectFolder(): String? {
    val fileChooser = JFileChooser()
    fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    fileChooser.dialogTitle = "选择下载目录"
    
    return if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFile.absolutePath
    } else {
        null
    }
}
