package com.okko.kmpact.ui.components.devtools.icongen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Android 图标尺寸配置
 */
enum class IconDensity(val displayName: String, val size: Int, val folderName: String) {
    LDPI("LDPI", 36, "mipmap-ldpi"),
    MDPI("MDPI", 48, "mipmap-mdpi"),
    HDPI("HDPI", 72, "mipmap-hdpi"),
    XHDPI("XHDPI", 96, "mipmap-xhdpi"),
    XXHDPI("XXHDPI", 144, "mipmap-xxhdpi"),
    XXXHDPI("XXXHDPI", 192, "mipmap-xxxhdpi")
}

/**
 * 内边距比例预设
 */
enum class PaddingPreset(val displayName: String, val percentage: Float) {
    NONE("0%", 0f),
    SMALL("5%", 0.05f),
    MEDIUM("10%", 0.10f),
    LARGE("15%", 0.15f),
    XLARGE("20%", 0.20f)
}

/**
 * 图标配置
 */
data class IconConfig(
    val backgroundColor: Color = Color.Transparent,
    val paddingPercentage: Float = 0f,
    val hasRoundCorner: Boolean = false,
    val cornerRadiusPercentage: Float = 17.54f
) {
    /**
     * 计算实际内边距
     */
    fun calculatePadding(size: Int): Int {
        return (size * paddingPercentage).toInt()
    }
    
    /**
     * 计算实际圆角半径
     */
    fun calculateCornerRadius(size: Int): Float {
        return if (hasRoundCorner) {
            size * cornerRadiusPercentage / 100f
        } else {
            0f
        }
    }
}

/**
 * 图标生成结果
 */
data class IconGenerationResult(
    val success: Boolean,
    val outputPath: String = "",
    val errorMessage: String = "",
    val generatedFiles: List<String> = emptyList()
)

/**
 * 图标生成器（平台特定实现）
 */
expect object IconGenerator {
    /**
     * 选择图片文件
     */
    fun selectImageFile(): String?
    
    /**
     * 加载图片
     */
    fun loadImage(path: String): ImageBitmap?
    
    /**
     * 生成预览图标
     */
    fun generatePreviewIcon(
        sourceImage: ImageBitmap,
        config: IconConfig,
        size: Int = 192
    ): ImageBitmap?
    
    /**
     * 生成所有尺寸的图标
     */
    fun generateIcons(
        sourcePath: String,
        config: IconConfig
    ): IconGenerationResult
}
