package com.okko.kmpact.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * 图片转换工具类
 */
object ImageUtils {
    
    /**
     * 将BufferedImage转换为Compose ImageBitmap
     * @param bufferedImage AWT BufferedImage
     * @return ImageBitmap Compose ImageBitmap
     */
    fun bufferedImageToImageBitmap(bufferedImage: BufferedImage): ImageBitmap {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "PNG", outputStream)
        val byteArray = outputStream.toByteArray()
        return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
    }
    
    /**
     * 创建一个空白的ImageBitmap
     * @param width 宽度
     * @param height 高度
     * @return ImageBitmap 空白图片
     */
    fun createBlankImageBitmap(width: Int, height: Int): ImageBitmap {
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = bufferedImage.createGraphics()
        graphics.color = java.awt.Color.WHITE
        graphics.fillRect(0, 0, width, height)
        graphics.dispose()
        return bufferedImageToImageBitmap(bufferedImage)
    }
}