package com.okko.kmpact.utils

import androidx.compose.ui.graphics.ImageBitmap
import com.okko.kmpact.ui.components.devtools.ErrorCorrectionLevel
import com.okko.kmpact.data.settings.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * JVM平台的二维码服务实现
 */
actual object QrCodeService {
    
    actual suspend fun generateQrCode(
        content: String,
        size: Int,
        errorCorrectionLevel: ErrorCorrectionLevel
    ): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val bufferedImage = QrCodeUtils.generateQrCode(
                    content = content,
                    size = size,
                    errorCorrectionLevel = errorCorrectionLevel.toZxingLevel()
                )
                ImageUtils.bufferedImageToImageBitmap(bufferedImage)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    actual suspend fun selectAndDecodeQrCode(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = FileUtils.selectImageFile()
                if (file != null && FileUtils.isSupportedImageFile(file)) {
                    QrCodeUtils.decodeQrCodeFromFile(file)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    actual suspend fun decodeQrCodeFromPath(filePath: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (filePath.isBlank()) return@withContext null
                
                val file = java.io.File(filePath)
                if (file.exists() && FileUtils.isSupportedImageFile(file)) {
                    QrCodeUtils.decodeQrCodeFromFile(file)
                } else {
                    "文件不存在或格式不支持"
                }
            } catch (e: Exception) {
                "解码失败: ${e.message}"
            }
        }
    }
    
    actual suspend fun downloadPng(
        content: String,
        size: Int,
        errorCorrectionLevel: ErrorCorrectionLevel
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val bufferedImage = QrCodeUtils.generateQrCode(
                    content = content,
                    size = size,
                    errorCorrectionLevel = errorCorrectionLevel.toZxingLevel()
                )
                
                // 使用设置中的默认下载路径
                val downloadPath = SettingsManager.getCurrentSettings().downloadPath
                val file = FileUtils.createTimestampFile(downloadPath, "png")
                
                QrCodeUtils.saveQrCodeToFile(bufferedImage, file, "PNG")
                "PNG 已保存到: ${file.absolutePath}"
            } catch (e: Exception) {
                "下载失败: ${e.message}"
            }
        }
    }
    
    actual suspend fun downloadSvg(
        content: String,
        size: Int,
        errorCorrectionLevel: ErrorCorrectionLevel
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val svgContent = QrCodeUtils.generateQrCodeSvg(
                    content = content,
                    size = size,
                    errorCorrectionLevel = errorCorrectionLevel.toZxingLevel()
                )
                
                // 使用设置中的默认下载路径
                val downloadPath = SettingsManager.getCurrentSettings().downloadPath
                val file = FileUtils.createTimestampFile(downloadPath, "svg")
                
                file.writeText(svgContent)
                "SVG 已保存到: ${file.absolutePath}"
            } catch (e: Exception) {
                "下载失败: ${e.message}"
            }
        }
    }
}