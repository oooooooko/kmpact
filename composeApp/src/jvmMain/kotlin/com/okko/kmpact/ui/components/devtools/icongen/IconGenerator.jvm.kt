package com.okko.kmpact.ui.components.devtools.icongen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * JVM 平台图标生成器实现
 */
actual object IconGenerator {
    
    /**
     * 选择图片文件
     */
    actual fun selectImageFile(): String? {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "选择图片文件"
        fileChooser.fileFilter = FileNameExtensionFilter(
            "图片文件 (*.png, *.jpg, *.jpeg)",
            "png", "jpg", "jpeg"
        )
        
        return if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile.absolutePath
        } else {
            null
        }
    }
    
    /**
     * 加载图片
     */
    actual fun loadImage(path: String): ImageBitmap? {
        return try {
            val file = File(path)
            if (!file.exists()) return null
            
            val bufferedImage = ImageIO.read(file)
            bufferedImage?.toComposeImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 生成预览图标
     */
    actual fun generatePreviewIcon(
        sourceImage: ImageBitmap,
        config: IconConfig,
        size: Int
    ): ImageBitmap? {
        return try {
            val bufferedImage = processIcon(
                sourceImage.toAwtImage(),
                config,
                size
            )
            bufferedImage.toComposeImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 生成所有尺寸的图标
     */
    actual fun generateIcons(
        sourcePath: String,
        config: IconConfig
    ): IconGenerationResult {
        return try {
            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) {
                return IconGenerationResult(
                    success = false,
                    errorMessage = "源文件不存在"
                )
            }
            
            val sourceImage = ImageIO.read(sourceFile)
            if (sourceImage == null) {
                return IconGenerationResult(
                    success = false,
                    errorMessage = "无法读取图片文件"
                )
            }
            
            // 获取设置的下载路径
            val downloadPath = try {
                com.okko.kmpact.data.settings.SettingsManager.getCurrentSettings().downloadPath
            } catch (e: Exception) {
                System.getProperty("user.home") + File.separator + "Downloads"
            }
            
            // 创建输出目录
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val outputDir = File(downloadPath, "$timestamp-icons")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            val generatedFiles = mutableListOf<String>()
            
            // 生成各个尺寸的图标
            IconDensity.entries.forEach { density ->
                val densityDir = File(outputDir, density.folderName)
                densityDir.mkdirs()
                
                val processedImage = processIcon(sourceImage, config, density.size)
                val outputFile = File(densityDir, "ic_launcher.png")
                
                ImageIO.write(processedImage, "PNG", outputFile)
                generatedFiles.add(outputFile.absolutePath)
            }
            
            IconGenerationResult(
                success = true,
                outputPath = outputDir.absolutePath,
                generatedFiles = generatedFiles
            )
        } catch (e: Exception) {
            e.printStackTrace()
            IconGenerationResult(
                success = false,
                errorMessage = "生成失败: ${e.message}"
            )
        }
    }
    
    /**
     * 处理单个图标
     */
    private fun processIcon(
        sourceImage: BufferedImage,
        config: IconConfig,
        targetSize: Int
    ): BufferedImage {
        // 创建目标图片
        val result = BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB)
        val g2d = result.createGraphics()
        
        // 设置渲染质量
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        
        // 绘制背景色
        if (config.backgroundColor != Color.Transparent) {
            g2d.color = config.backgroundColor.toAwtColor()
            if (config.hasRoundCorner) {
                val cornerRadius = config.calculateCornerRadius(targetSize)
                g2d.fill(RoundRectangle2D.Float(
                    0f, 0f,
                    targetSize.toFloat(), targetSize.toFloat(),
                    cornerRadius * 2, cornerRadius * 2
                ))
            } else {
                g2d.fillRect(0, 0, targetSize, targetSize)
            }
        }
        
        // 计算内边距
        val padding = config.calculatePadding(targetSize)
        val contentSize = targetSize - padding * 2
        
        // 设置裁剪区域（如果有圆角）
        if (config.hasRoundCorner) {
            val cornerRadius = config.calculateCornerRadius(targetSize)
            g2d.clip = RoundRectangle2D.Float(
                0f, 0f,
                targetSize.toFloat(), targetSize.toFloat(),
                cornerRadius * 2, cornerRadius * 2
            )
        }
        
        // 缩放并绘制源图片
        val scaledImage = scaleImage(sourceImage, contentSize, contentSize)
        val x = padding + (contentSize - scaledImage.width) / 2
        val y = padding + (contentSize - scaledImage.height) / 2
        g2d.drawImage(scaledImage, x, y, null)
        
        g2d.dispose()
        return result
    }
    
    /**
     * 缩放图片
     */
    private fun scaleImage(source: BufferedImage, maxWidth: Int, maxHeight: Int): BufferedImage {
        val sourceWidth = source.width
        val sourceHeight = source.height
        
        // 计算缩放比例
        val scale = minOf(
            maxWidth.toFloat() / sourceWidth,
            maxHeight.toFloat() / sourceHeight
        )
        
        val targetWidth = (sourceWidth * scale).toInt()
        val targetHeight = (sourceHeight * scale).toInt()
        
        val result = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
        val g2d = result.createGraphics()
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g2d.drawImage(source, 0, 0, targetWidth, targetHeight, null)
        g2d.dispose()
        
        return result
    }
    
    /**
     * Compose Color 转 AWT Color
     */
    private fun Color.toAwtColor(): java.awt.Color {
        return java.awt.Color(
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt(),
            (alpha * 255).toInt()
        )
    }
    
    /**
     * BufferedImage 转 ImageBitmap
     */
    private fun BufferedImage.toComposeImageBitmap(): ImageBitmap {
        val bytes = java.io.ByteArrayOutputStream().use { baos ->
            ImageIO.write(this, "PNG", baos)
            baos.toByteArray()
        }
        return Image.makeFromEncoded(bytes).toComposeImageBitmap()
    }
}
