package com.okko.kmpact.ui.components.devtools.color

/**
 * 颜色格式类型
 */
enum class ColorFormat(val displayName: String, val example: String) {
    HEX("HEX", "#FF5733"),
    RGB("RGB", "rgb(255, 87, 51)"),
    RGBA("RGBA", "rgba(255, 87, 51, 1.0)"),
    HSL("HSL", "hsl(9, 100%, 60%)"),
    HSLA("HSLA", "hsla(9, 100%, 60%, 1.0)"),
    HSV("HSV", "hsv(9, 80%, 100%)"),
    CMYK("CMYK", "cmyk(0%, 66%, 80%, 0%)")
}

/**
 * 颜色数据类
 */
data class ColorData(
    val red: Int,      // 0-255
    val green: Int,    // 0-255
    val blue: Int,     // 0-255
    val alpha: Float = 1.0f  // 0.0-1.0
) {
    /**
     * 转换为 HEX 格式
     */
    fun toHex(includeAlpha: Boolean = false): String {
        return if (includeAlpha && alpha < 1.0f) {
            val alphaHex = (alpha * 255).toInt().toString(16).padStart(2, '0').uppercase()
            "#${red.toString(16).padStart(2, '0').uppercase()}" +
            "${green.toString(16).padStart(2, '0').uppercase()}" +
            "${blue.toString(16).padStart(2, '0').uppercase()}$alphaHex"
        } else {
            "#${red.toString(16).padStart(2, '0').uppercase()}" +
            "${green.toString(16).padStart(2, '0').uppercase()}" +
            "${blue.toString(16).padStart(2, '0').uppercase()}"
        }
    }
    
    /**
     * 转换为 RGB 格式
     */
    fun toRgb(): String {
        return "rgb($red, $green, $blue)"
    }
    
    /**
     * 转换为 RGBA 格式
     */
    fun toRgba(): String {
        val alphaStr = (alpha * 100).toInt() / 100.0
        return "rgba($red, $green, $blue, $alphaStr)"
    }
    
    /**
     * 转换为 HSL 格式
     */
    fun toHsl(): String {
        val hsl = rgbToHsl(red, green, blue)
        return "hsl(${hsl.first.toInt()}, ${(hsl.second * 100).toInt()}%, ${(hsl.third * 100).toInt()}%)"
    }
    
    /**
     * 转换为 HSLA 格式
     */
    fun toHsla(): String {
        val hsl = rgbToHsl(red, green, blue)
        val alphaStr = (alpha * 100).toInt() / 100.0
        return "hsla(${hsl.first.toInt()}, ${(hsl.second * 100).toInt()}%, ${(hsl.third * 100).toInt()}%, $alphaStr)"
    }
    
    /**
     * 转换为 HSV 格式
     */
    fun toHsv(): String {
        val hsv = rgbToHsv(red, green, blue)
        return "hsv(${hsv.first.toInt()}, ${(hsv.second * 100).toInt()}%, ${(hsv.third * 100).toInt()}%)"
    }
    
    /**
     * 转换为 CMYK 格式
     */
    fun toCmyk(): String {
        val cmyk = rgbToCmyk(red, green, blue)
        return "cmyk(${(cmyk.first * 100).toInt()}%, ${(cmyk.second * 100).toInt()}%, ${(cmyk.third * 100).toInt()}%, ${(cmyk.fourth * 100).toInt()}%)"
    }
    
    /**
     * 转换为 Compose Color
     */
    fun toComposeColor(): androidx.compose.ui.graphics.Color {
        return androidx.compose.ui.graphics.Color(
            red = red / 255f,
            green = green / 255f,
            blue = blue / 255f,
            alpha = alpha
        )
    }
    
    companion object {
        /**
         * 从 HEX 解析
         */
        fun fromHex(hex: String): ColorData? {
            val cleanHex = hex.trim().removePrefix("#")
            
            return try {
                when (cleanHex.length) {
                    6 -> {
                        val r = cleanHex.substring(0, 2).toInt(16)
                        val g = cleanHex.substring(2, 4).toInt(16)
                        val b = cleanHex.substring(4, 6).toInt(16)
                        ColorData(r, g, b)
                    }
                    8 -> {
                        val r = cleanHex.substring(0, 2).toInt(16)
                        val g = cleanHex.substring(2, 4).toInt(16)
                        val b = cleanHex.substring(4, 6).toInt(16)
                        val a = cleanHex.substring(6, 8).toInt(16) / 255f
                        ColorData(r, g, b, a)
                    }
                    3 -> {
                        val r = cleanHex.substring(0, 1).repeat(2).toInt(16)
                        val g = cleanHex.substring(1, 2).repeat(2).toInt(16)
                        val b = cleanHex.substring(2, 3).repeat(2).toInt(16)
                        ColorData(r, g, b)
                    }
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * 从 RGB 解析
         */
        fun fromRgb(rgb: String): ColorData? {
            val regex = """rgb\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)""".toRegex()
            val match = regex.find(rgb.trim()) ?: return null
            
            return try {
                val r = match.groupValues[1].toInt()
                val g = match.groupValues[2].toInt()
                val b = match.groupValues[3].toInt()
                
                if (r in 0..255 && g in 0..255 && b in 0..255) {
                    ColorData(r, g, b)
                } else null
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * 从 RGBA 解析
         */
        fun fromRgba(rgba: String): ColorData? {
            val regex = """rgba\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*([\d.]+)\s*\)""".toRegex()
            val match = regex.find(rgba.trim()) ?: return null
            
            return try {
                val r = match.groupValues[1].toInt()
                val g = match.groupValues[2].toInt()
                val b = match.groupValues[3].toInt()
                val a = match.groupValues[4].toFloat()
                
                if (r in 0..255 && g in 0..255 && b in 0..255 && a in 0f..1f) {
                    ColorData(r, g, b, a)
                } else null
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * 从 HSL 解析
         */
        fun fromHsl(hsl: String): ColorData? {
            val regex = """hsl\s*\(\s*(\d+)\s*,\s*(\d+)%\s*,\s*(\d+)%\s*\)""".toRegex()
            val match = regex.find(hsl.trim()) ?: return null
            
            return try {
                val h = match.groupValues[1].toFloat()
                val s = match.groupValues[2].toFloat() / 100f
                val l = match.groupValues[3].toFloat() / 100f
                
                val rgb = hslToRgb(h, s, l)
                ColorData(rgb.first, rgb.second, rgb.third)
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * RGB 转 HSL
         */
        private fun rgbToHsl(r: Int, g: Int, b: Int): Triple<Float, Float, Float> {
            val rf = r / 255f
            val gf = g / 255f
            val bf = b / 255f
            
            val max = maxOf(rf, gf, bf)
            val min = minOf(rf, gf, bf)
            val delta = max - min
            
            var h = 0f
            val s: Float
            val l = (max + min) / 2f
            
            if (delta != 0f) {
                s = if (l < 0.5f) delta / (max + min) else delta / (2f - max - min)
                
                h = when (max) {
                    rf -> ((gf - bf) / delta + (if (gf < bf) 6f else 0f)) * 60f
                    gf -> ((bf - rf) / delta + 2f) * 60f
                    bf -> ((rf - gf) / delta + 4f) * 60f
                    else -> 0f
                }
            } else {
                s = 0f
            }
            
            return Triple(h, s, l)
        }
        
        /**
         * HSL 转 RGB
         */
        private fun hslToRgb(h: Float, s: Float, l: Float): Triple<Int, Int, Int> {
            val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
            val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
            val m = l - c / 2f
            
            val (r, g, b) = when {
                h < 60f -> Triple(c, x, 0f)
                h < 120f -> Triple(x, c, 0f)
                h < 180f -> Triple(0f, c, x)
                h < 240f -> Triple(0f, x, c)
                h < 300f -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }
            
            return Triple(
                ((r + m) * 255).toInt(),
                ((g + m) * 255).toInt(),
                ((b + m) * 255).toInt()
            )
        }
        
        /**
         * RGB 转 HSV
         */
        private fun rgbToHsv(r: Int, g: Int, b: Int): Triple<Float, Float, Float> {
            val rf = r / 255f
            val gf = g / 255f
            val bf = b / 255f
            
            val max = maxOf(rf, gf, bf)
            val min = minOf(rf, gf, bf)
            val delta = max - min
            
            var h = 0f
            val s = if (max != 0f) delta / max else 0f
            val v = max
            
            if (delta != 0f) {
                h = when (max) {
                    rf -> ((gf - bf) / delta + (if (gf < bf) 6f else 0f)) * 60f
                    gf -> ((bf - rf) / delta + 2f) * 60f
                    bf -> ((rf - gf) / delta + 4f) * 60f
                    else -> 0f
                }
            }
            
            return Triple(h, s, v)
        }
        
        /**
         * RGB 转 CMYK
         */
        private fun rgbToCmyk(r: Int, g: Int, b: Int): Quadruple<Float, Float, Float, Float> {
            val rf = r / 255f
            val gf = g / 255f
            val bf = b / 255f
            
            val k = 1f - maxOf(rf, gf, bf)
            
            if (k == 1f) {
                return Quadruple(0f, 0f, 0f, 1f)
            }
            
            val c = (1f - rf - k) / (1f - k)
            val m = (1f - gf - k) / (1f - k)
            val y = (1f - bf - k) / (1f - k)
            
            return Quadruple(c, m, y, k)
        }
    }
}

/**
 * 四元组数据类
 */
data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
