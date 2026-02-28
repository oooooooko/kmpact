package com.okko.kmpact.utils

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

/**
 * 二维码工具类，封装ZXing库的功能
 */
object QrCodeUtils {
    
    /**
     * 生成二维码
     * @param content 二维码内容
     * @param size 二维码尺寸
     * @param errorCorrectionLevel 容错率
     * @return BufferedImage 二维码图片
     */
    fun generateQrCode(
        content: String,
        size: Int = 400,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M
    ): BufferedImage {
        val writer = QRCodeWriter()
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to errorCorrectionLevel,
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1
        )
        
        val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        return MatrixToImageWriter.toBufferedImage(bitMatrix)
    }
    
    /**
     * 将BufferedImage转换为字节数组
     * @param image 图片
     * @param format 格式 (PNG, JPG等)
     * @return ByteArray 图片字节数组
     */
    fun imageToByteArray(image: BufferedImage, format: String = "PNG"): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, format, outputStream)
        return outputStream.toByteArray()
    }
    
    /**
     * 保存二维码到文件
     * @param image 二维码图片
     * @param file 目标文件
     * @param format 图片格式
     */
    fun saveQrCodeToFile(image: BufferedImage, file: File, format: String = "PNG") {
        ImageIO.write(image, format, file)
    }
    
    /**
     * 解码二维码
     * @param image 包含二维码的图片
     * @return String? 解码结果，如果解码失败返回null
     */
    fun decodeQrCode(image: BufferedImage): String? {
        return try {
            val source = BufferedImageLuminanceSource(image)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            
            val hints = mapOf(
                DecodeHintType.CHARACTER_SET to "UTF-8",
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
            )
            
            val result = reader.decode(bitmap, hints)
            result.text
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 从文件解码二维码
     * @param file 图片文件
     * @return String? 解码结果
     */
    fun decodeQrCodeFromFile(file: File): String? {
        return try {
            val image = ImageIO.read(file)
            decodeQrCode(image)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 生成SVG格式的二维码
     * @param content 二维码内容
     * @param size 二维码尺寸
     * @param errorCorrectionLevel 容错率
     * @return String SVG内容
     */
    fun generateQrCodeSvg(
        content: String,
        size: Int = 400,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M
    ): String {
        val writer = QRCodeWriter()
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to errorCorrectionLevel,
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1
        )
        
        val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        
        val svgBuilder = StringBuilder()
        svgBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        svgBuilder.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
        svgBuilder.append("width=\"$size\" height=\"$size\" viewBox=\"0 0 $size $size\">\n")
        svgBuilder.append("<rect width=\"$size\" height=\"$size\" fill=\"white\"/>\n")
        
        val moduleSize = size.toFloat() / bitMatrix.width
        
        for (y in 0 until bitMatrix.height) {
            for (x in 0 until bitMatrix.width) {
                if (bitMatrix[x, y]) {
                    val rectX = x * moduleSize
                    val rectY = y * moduleSize
                    svgBuilder.append("<rect x=\"$rectX\" y=\"$rectY\" ")
                    svgBuilder.append("width=\"$moduleSize\" height=\"$moduleSize\" fill=\"black\"/>\n")
                }
            }
        }
        
        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }
}

/**
 * ZXing错误纠正级别到应用错误纠正级别的转换
 */
fun com.okko.kmpact.ui.components.devtools.ErrorCorrectionLevel.toZxingLevel(): ErrorCorrectionLevel {
    return when (this) {
        com.okko.kmpact.ui.components.devtools.ErrorCorrectionLevel.L -> ErrorCorrectionLevel.L
        com.okko.kmpact.ui.components.devtools.ErrorCorrectionLevel.M -> ErrorCorrectionLevel.M
        com.okko.kmpact.ui.components.devtools.ErrorCorrectionLevel.Q -> ErrorCorrectionLevel.Q
        com.okko.kmpact.ui.components.devtools.ErrorCorrectionLevel.H -> ErrorCorrectionLevel.H
    }
}