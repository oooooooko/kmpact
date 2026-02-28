package com.okko.kmpact.utils

import androidx.compose.ui.graphics.ImageBitmap
import com.okko.kmpact.ui.components.devtools.ErrorCorrectionLevel

/**
 * 二维码服务 - 跨平台抽象
 */
expect object QrCodeService {
    /**
     * 生成二维码
     */
    suspend fun generateQrCode(
        content: String,
        size: Int,
        errorCorrectionLevel: ErrorCorrectionLevel
    ): ImageBitmap?
    
    /**
     * 选择并解码二维码图片
     */
    suspend fun selectAndDecodeQrCode(): String?
    
    /**
     * 从文件路径解码二维码
     */
    suspend fun decodeQrCodeFromPath(filePath: String): String?
    
    /**
     * 下载PNG格式二维码
     */
    suspend fun downloadPng(
        content: String,
        size: Int,
        errorCorrectionLevel: ErrorCorrectionLevel
    ): String // 返回状态信息
    
    /**
     * 下载SVG格式二维码
     */
    suspend fun downloadSvg(
        content: String,
        size: Int,
        errorCorrectionLevel: ErrorCorrectionLevel
    ): String // 返回状态信息
}