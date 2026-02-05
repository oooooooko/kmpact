package com.okko.kmpact

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import javax.imageio.ImageIO

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KMP-AndroidCmdTools",
        icon = BitmapPainter(loadAppIcon()),
        state = WindowState(
            width = 1400.dp,
            height = 900.dp
        )
    ) {
        App()
    }
}

/**
 * 加载应用图标
 */
private fun loadAppIcon(): androidx.compose.ui.graphics.ImageBitmap {
    return try {
        // 从resources加载图标
        val iconStream = object {}.javaClass.getResourceAsStream("/ic_launcher.png")
        if (iconStream != null) {
            val bufferedImage = ImageIO.read(iconStream)
            bufferedImage.toComposeImageBitmap()
        } else {
            // 如果找不到图标，创建默认图标
            println("警告: 未找到ic_launcher.png资源文件，使用默认图标")
            createDefaultIcon()
        }
    } catch (e: Exception) {
        println("加载图标失败: ${e.message}")
        createDefaultIcon()
    }
}

/**
 * 创建默认图标（备用）
 */
private fun createDefaultIcon(): androidx.compose.ui.graphics.ImageBitmap {
    val size = 256
    val image = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
    val g2d = image.createGraphics()
    
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.color = java.awt.Color(0x19, 0x76, 0xD2)
    g2d.fillRoundRect(0, 0, size, size, 40, 40)
    
    g2d.dispose()
    return image.toComposeImageBitmap()
}