package com.okko.kmpact.platform

import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * JVM 平台的文件选择器实现
 */
actual class FileChooser {
    
    /**
     * 选择单个文件
     */
    actual fun chooseFile(
        title: String,
        allowedExtensions: List<String>?
    ): String? {
        return try {
            // 使用 AWT FileDialog（更原生的体验）
            val fileDialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
            
            // 设置文件过滤器
            if (allowedExtensions != null && allowedExtensions.isNotEmpty()) {
                fileDialog.filenameFilter = FilenameFilter { _, name ->
                    allowedExtensions.any { ext ->
                        name.lowercase().endsWith(".$ext")
                    }
                }
            }
            
            fileDialog.isVisible = true
            
            val directory = fileDialog.directory
            val file = fileDialog.file
            
            if (directory != null && file != null) {
                "$directory$file"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 选择文件夹
     */
    actual fun chooseDirectory(
        title: String
    ): String? {
        return try {
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = title
            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            fileChooser.isAcceptAllFileFilterUsed = false
            
            val result = fileChooser.showOpenDialog(null)
            
            if (result == JFileChooser.APPROVE_OPTION) {
                fileChooser.selectedFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
