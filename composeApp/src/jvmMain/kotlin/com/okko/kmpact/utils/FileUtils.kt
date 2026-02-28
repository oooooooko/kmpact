package com.okko.kmpact.utils

import java.awt.image.BufferedImage
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * 文件操作工具类
 */
object FileUtils {
    
    /**
     * 打开文件选择器选择图片文件
     * @return File? 选择的文件，如果取消选择返回null
     */
    fun selectImageFile(): File? {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "选择二维码图片"
            fileSelectionMode = JFileChooser.FILES_ONLY
            isMultiSelectionEnabled = false
            
            // 添加图片文件过滤器
            val imageFilter = FileNameExtensionFilter(
                "图片文件 (*.png, *.jpg, *.jpeg, *.bmp, *.gif)",
                "png", "jpg", "jpeg", "bmp", "gif"
            )
            fileFilter = imageFilter
        }
        
        return if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile
        } else {
            null
        }
    }
    
    /**
     * 打开文件选择器保存文件
     * @param defaultName 默认文件名
     * @param extension 文件扩展名
     * @return File? 选择的保存位置，如果取消返回null
     */
    fun selectSaveFile(defaultName: String, extension: String): File? {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "保存二维码"
            fileSelectionMode = JFileChooser.FILES_ONLY
            selectedFile = File("$defaultName.$extension")
            
            // 添加文件过滤器
            val filter = when (extension.lowercase()) {
                "png" -> FileNameExtensionFilter("PNG 图片 (*.png)", "png")
                "jpg", "jpeg" -> FileNameExtensionFilter("JPEG 图片 (*.jpg)", "jpg", "jpeg")
                "svg" -> FileNameExtensionFilter("SVG 矢量图 (*.svg)", "svg")
                else -> FileNameExtensionFilter("所有文件", "*")
            }
            fileFilter = filter
        }
        
        return if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            // 确保文件有正确的扩展名
            if (!selectedFile.name.endsWith(".$extension", ignoreCase = true)) {
                File(selectedFile.parentFile, "${selectedFile.nameWithoutExtension}.$extension")
            } else {
                selectedFile
            }
        } else {
            null
        }
    }
    
    /**
     * 在指定目录创建带时间戳的文件
     * @param directory 目录路径
     * @param extension 文件扩展名
     * @return File 创建的文件对象
     */
    fun createTimestampFile(directory: String, extension: String): File {
        val dir = File(directory)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName = "${timestamp}_qrcode.$extension"
        return File(dir, fileName)
    }
    
    /**
     * 读取图片文件
     * @param file 图片文件
     * @return BufferedImage? 图片对象，如果读取失败返回null
     */
    fun readImage(file: File): BufferedImage? {
        return try {
            ImageIO.read(file)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 检查文件是否为支持的图片格式
     * @param file 文件
     * @return Boolean 是否为支持的图片格式
     */
    fun isSupportedImageFile(file: File): Boolean {
        val supportedExtensions = listOf("png", "jpg", "jpeg", "bmp", "gif")
        val extension = file.extension.lowercase()
        return supportedExtensions.contains(extension)
    }
    
    /**
     * 获取文件大小的可读字符串
     * @param file 文件
     * @return String 文件大小字符串
     */
    fun getFileSizeString(file: File): String {
        val bytes = file.length()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}