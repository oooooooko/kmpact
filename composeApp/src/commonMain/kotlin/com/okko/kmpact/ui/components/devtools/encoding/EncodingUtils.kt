package com.okko.kmpact.ui.components.devtools.encoding

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * 编码转换工具类
 */
object EncodingUtils {

    /**
     * 执行编码转换
     */
    fun convert(input: String, type: EncodingType): Result<String> {
        return try {
            val result = when (type) {
                // 加密
                is EncodingType.UnicodeEncode -> unicodeEncode(input)
                is EncodingType.UrlEncode -> urlEncode(input)
                is EncodingType.Base64Encode -> base64Encode(input)
                is EncodingType.Md5Encode -> md5Hash(input)
                is EncodingType.HexEncode -> hexEncode(input)
                is EncodingType.Sha1Encode -> sha1Hash(input)
                is EncodingType.Sha256Encode -> sha256Hash(input)

                // 解密
                is EncodingType.UnicodeDecode -> unicodeDecode(input)
                is EncodingType.UrlDecode -> urlDecode(input)
                is EncodingType.Base64Decode -> base64Decode(input)
                is EncodingType.HexDecode -> hexDecode(input)
                is EncodingType.CookieFormat -> cookieFormat(input)
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== 加密方法 ====================

    private fun unicodeEncode(input: String): String {
        return input.map { char ->
            "\\u${char.code.toString(16).padStart(4, '0')}"
        }.joinToString("")
    }

    private fun urlEncode(input: String): String {
        return input.encodeToByteArray().joinToString("") { byte ->
            val unsigned = byte.toInt() and 0xFF
            when (unsigned.toChar()) {
                in 'A'..'Z', in 'a'..'z', in '0'..'9', '-', '_', '.', '~' ->
                    unsigned.toChar().toString()

                else ->
                    "%${unsigned.toString(16).uppercase().padStart(2, '0')}"
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun base64Encode(input: String): String {
        return Base64.encode(input.encodeToByteArray())
    }

    private fun md5Hash(input: String): String {
        return calculateMD5(input.encodeToByteArray())
    }

    private fun hexEncode(input: String): String {
        return input.encodeToByteArray().joinToString("") { byte ->
            (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
        }
    }

    private fun sha1Hash(input: String): String {
        return calculateSHA1(input.encodeToByteArray())
    }

    private fun sha256Hash(input: String): String {
        return calculateSHA256(input.encodeToByteArray())
    }

    // ==================== 解密方法 ====================

    private fun unicodeDecode(input: String): String {
        val regex = """\\u([0-9a-fA-F]{4})""".toRegex()
        return regex.replace(input) { matchResult ->
            val code = matchResult.groupValues[1].toInt(16)
            code.toChar().toString()
        }
    }

    private fun urlDecode(input: String): String {
        val result = StringBuilder()
        var i = 0
        while (i < input.length) {
            when {
                input[i] == '%' && i + 2 < input.length -> {
                    val hex = input.substring(i + 1, i + 3)
                    result.append(hex.toInt(16).toChar())
                    i += 3
                }

                input[i] == '+' -> {
                    result.append(' ')
                    i++
                }

                else -> {
                    result.append(input[i])
                    i++
                }
            }
        }
        return result.toString()
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun base64Decode(input: String): String {
        return Base64.decode(input).decodeToString()
    }

    private fun hexDecode(input: String): String {
        val cleaned = input.replace(" ", "").replace("0x", "")
        return cleaned.chunked(2).map { hex ->
            hex.toInt(16).toChar()
        }.joinToString("")
    }

    private fun cookieFormat(input: String): String {
        val cookies = input.trim().split(';')

        return buildString {
            appendLine("Cookie格式化结果：")
            appendLine()
            cookies.forEachIndexed { index, cookie ->
                val trimmed = cookie.trim()
                if (trimmed.isNotEmpty()) {
                    val parts = trimmed.split('=', limit = 2)
                    val key = parts[0]
                    val value = if (parts.size > 1) parts[1] else ""
                    appendLine("${index + 1}. $key = $value")
                }
            }
        }
    }

    // ==================== MD5 实现 ====================

    private fun calculateMD5(input: ByteArray): String {
        // MD5 常量
        val s = intArrayOf(
            7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
            5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
            4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
            6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21
        )

        val k = IntArray(64) { i ->
            ((1L shl 32) * kotlin.math.abs(kotlin.math.sin((i + 1).toDouble()))).toInt()
        }

        // 初始化变量
        var a0 = 0x67452301
        var b0 = 0xEFCDAB89.toInt()
        var c0 = 0x98BADCFE.toInt()
        var d0 = 0x10325476

        // 预处理
        val msgLen = input.size
        val bitLen = msgLen * 8L
        val paddingLen = (56 - (msgLen + 1) % 64 + 64) % 64
        val totalLen = msgLen + 1 + paddingLen + 8

        val padded = ByteArray(totalLen)
        input.copyInto(padded)
        padded[msgLen] = 0x80.toByte()

        // 添加长度（小端序）
        for (i in 0..7) {
            padded[totalLen - 8 + i] = ((bitLen ushr (i * 8)) and 0xFFL).toByte()
        }

        // 处理每个512位块
        for (offset in 0 until totalLen step 64) {
            val m = IntArray(16)
            for (i in 0..15) {
                m[i] = (padded[offset + i * 4].toInt() and 0xFF) or
                        ((padded[offset + i * 4 + 1].toInt() and 0xFF) shl 8) or
                        ((padded[offset + i * 4 + 2].toInt() and 0xFF) shl 16) or
                        ((padded[offset + i * 4 + 3].toInt() and 0xFF) shl 24)
            }

            var a = a0
            var b = b0
            var c = c0
            var d = d0

            for (i in 0..63) {
                val f: Int
                val g: Int

                when (i) {
                    in 0..15 -> {
                        f = (b and c) or (b.inv() and d)
                        g = i
                    }

                    in 16..31 -> {
                        f = (d and b) or (d.inv() and c)
                        g = (5 * i + 1) % 16
                    }

                    in 32..47 -> {
                        f = b xor c xor d
                        g = (3 * i + 5) % 16
                    }

                    else -> {
                        f = c xor (b or d.inv())
                        g = (7 * i) % 16
                    }
                }

                val temp = d
                d = c
                c = b
                b = b + leftRotate(a + f + k[i] + m[g], s[i])
                a = temp
            }

            a0 += a
            b0 += b
            c0 += c
            d0 += d
        }

        // 生成最终哈希值
        return buildString {
            append(toHexString(a0))
            append(toHexString(b0))
            append(toHexString(c0))
            append(toHexString(d0))
        }
    }

    private fun leftRotate(value: Int, shift: Int): Int {
        return (value shl shift) or (value ushr (32 - shift))
    }

    private fun toHexString(value: Int): String {
        return buildString {
            for (i in 0..3) {
                val byte = (value ushr (i * 8)) and 0xFF
                append(byte.toString(16).padStart(2, '0'))
            }
        }
    }

    // ==================== SHA1 实现 ====================

    private fun calculateSHA1(input: ByteArray): String {
        // 初始化哈希值
        var h0 = 0x67452301
        var h1 = 0xEFCDAB89.toInt()
        var h2 = 0x98BADCFE.toInt()
        var h3 = 0x10325476
        var h4 = 0xC3D2E1F0.toInt()

        // 预处理
        val msgLen = input.size
        val bitLen = msgLen * 8L
        val paddingLen = (56 - (msgLen + 1) % 64 + 64) % 64
        val totalLen = msgLen + 1 + paddingLen + 8

        val padded = ByteArray(totalLen)
        input.copyInto(padded)
        padded[msgLen] = 0x80.toByte()

        // 添加长度（大端序）
        for (i in 0..7) {
            padded[totalLen - 1 - i] = ((bitLen ushr (i * 8)) and 0xFFL).toByte()
        }

        // 处理每个512位块
        for (offset in 0 until totalLen step 64) {
            val w = IntArray(80)

            // 前16个字（大端序）
            for (i in 0..15) {
                w[i] = ((padded[offset + i * 4].toInt() and 0xFF) shl 24) or
                        ((padded[offset + i * 4 + 1].toInt() and 0xFF) shl 16) or
                        ((padded[offset + i * 4 + 2].toInt() and 0xFF) shl 8) or
                        (padded[offset + i * 4 + 3].toInt() and 0xFF)
            }

            // 扩展到80个字
            for (i in 16..79) {
                w[i] = leftRotate(w[i - 3] xor w[i - 8] xor w[i - 14] xor w[i - 16], 1)
            }

            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4

            for (i in 0..79) {
                val f: Int
                val k: Int

                when (i) {
                    in 0..19 -> {
                        f = (b and c) or (b.inv() and d)
                        k = 0x5A827999
                    }

                    in 20..39 -> {
                        f = b xor c xor d
                        k = 0x6ED9EBA1
                    }

                    in 40..59 -> {
                        f = (b and c) or (b and d) or (c and d)
                        k = 0x8F1BBCDC.toInt()
                    }

                    else -> {
                        f = b xor c xor d
                        k = 0xCA62C1D6.toInt()
                    }
                }

                val temp = leftRotate(a, 5) + f + e + k + w[i]
                e = d
                d = c
                c = leftRotate(b, 30)
                b = a
                a = temp
            }

            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
        }

        // 生成最终哈希值
        return buildString {
            append(toHexStringBigEndian(h0))
            append(toHexStringBigEndian(h1))
            append(toHexStringBigEndian(h2))
            append(toHexStringBigEndian(h3))
            append(toHexStringBigEndian(h4))
        }
    }

    private fun toHexStringBigEndian(value: Int): String {
        return buildString {
            for (i in 3 downTo 0) {
                val byte = (value ushr (i * 8)) and 0xFF
                append(byte.toString(16).padStart(2, '0'))
            }
        }
    }

    // ==================== SHA256 实现 ====================

    private fun calculateSHA256(input: ByteArray): String {
        // SHA-256 常量（前64个质数的立方根的小数部分的前32位）
        val k = intArrayOf(
            0x428a2f98.toInt(),
            0x71374491.toInt(),
            0xb5c0fbcf.toInt(),
            0xe9b5dba5.toInt(),
            0x3956c25b.toInt(),
            0x59f111f1.toInt(),
            0x923f82a4.toInt(),
            0xab1c5ed5.toInt(),
            0xd807aa98.toInt(),
            0x12835b01.toInt(),
            0x243185be.toInt(),
            0x550c7dc3.toInt(),
            0x72be5d74.toInt(),
            0x80deb1fe.toInt(),
            0x9bdc06a7.toInt(),
            0xc19bf174.toInt(),
            0xe49b69c1.toInt(),
            0xefbe4786.toInt(),
            0x0fc19dc6.toInt(),
            0x240ca1cc.toInt(),
            0x2de92c6f.toInt(),
            0x4a7484aa.toInt(),
            0x5cb0a9dc.toInt(),
            0x76f988da.toInt(),
            0x983e5152.toInt(),
            0xa831c66d.toInt(),
            0xb00327c8.toInt(),
            0xbf597fc7.toInt(),
            0xc6e00bf3.toInt(),
            0xd5a79147.toInt(),
            0x06ca6351.toInt(),
            0x14292967.toInt(),
            0x27b70a85.toInt(),
            0x2e1b2138.toInt(),
            0x4d2c6dfc.toInt(),
            0x53380d13.toInt(),
            0x650a7354.toInt(),
            0x766a0abb.toInt(),
            0x81c2c92e.toInt(),
            0x92722c85.toInt(),
            0xa2bfe8a1.toInt(),
            0xa81a664b.toInt(),
            0xc24b8b70.toInt(),
            0xc76c51a3.toInt(),
            0xd192e819.toInt(),
            0xd6990624.toInt(),
            0xf40e3585.toInt(),
            0x106aa070.toInt(),
            0x19a4c116.toInt(),
            0x1e376c08.toInt(),
            0x2748774c.toInt(),
            0x34b0bcb5.toInt(),
            0x391c0cb3.toInt(),
            0x4ed8aa4a.toInt(),
            0x5b9cca4f.toInt(),
            0x682e6ff3.toInt(),
            0x748f82ee.toInt(),
            0x78a5636f.toInt(),
            0x84c87814.toInt(),
            0x8cc70208.toInt(),
            0x90befffa.toInt(),
            0xa4506ceb.toInt(),
            0xbef9a3f7.toInt(),
            0xc67178f2.toInt()
        )

        // 初始化哈希值（前8个质数的平方根的小数部分的前32位）
        var h0 = 0x6a09e667
        var h1 = 0xbb67ae85.toInt()
        var h2 = 0x3c6ef372
        var h3 = 0xa54ff53a.toInt()
        var h4 = 0x510e527f
        var h5 = 0x9b05688c.toInt()
        var h6 = 0x1f83d9ab
        var h7 = 0x5be0cd19

        // 预处理
        val msgLen = input.size
        val bitLen = msgLen * 8L
        val paddingLen = (56 - (msgLen + 1) % 64 + 64) % 64
        val totalLen = msgLen + 1 + paddingLen + 8

        val padded = ByteArray(totalLen)
        input.copyInto(padded)
        padded[msgLen] = 0x80.toByte()

        // 添加长度（大端序）
        for (i in 0..7) {
            padded[totalLen - 1 - i] = ((bitLen ushr (i * 8)) and 0xFFL).toByte()
        }

        // 处理每个512位块
        for (offset in 0 until totalLen step 64) {
            val w = IntArray(64)

            // 前16个字（大端序）
            for (i in 0..15) {
                w[i] = ((padded[offset + i * 4].toInt() and 0xFF) shl 24) or
                        ((padded[offset + i * 4 + 1].toInt() and 0xFF) shl 16) or
                        ((padded[offset + i * 4 + 2].toInt() and 0xFF) shl 8) or
                        (padded[offset + i * 4 + 3].toInt() and 0xFF)
            }

            // 扩展到64个字
            for (i in 16..63) {
                val s0 = rightRotate(w[i - 15], 7) xor rightRotate(w[i - 15], 18) xor (w[i - 15] ushr 3)
                val s1 = rightRotate(w[i - 2], 17) xor rightRotate(w[i - 2], 19) xor (w[i - 2] ushr 10)
                w[i] = w[i - 16] + s0 + w[i - 7] + s1
            }

            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4
            var f = h5
            var g = h6
            var h = h7

            for (i in 0..63) {
                val s1 = rightRotate(e, 6) xor rightRotate(e, 11) xor rightRotate(e, 25)
                val ch = (e and f) xor (e.inv() and g)
                val temp1 = h + s1 + ch + k[i] + w[i]
                val s0 = rightRotate(a, 2) xor rightRotate(a, 13) xor rightRotate(a, 22)
                val maj = (a and b) xor (a and c) xor (b and c)
                val temp2 = s0 + maj

                h = g
                g = f
                f = e
                e = d + temp1
                d = c
                c = b
                b = a
                a = temp1 + temp2
            }

            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
            h5 += f
            h6 += g
            h7 += h
        }

        // 生成最终哈希值
        return buildString {
            append(toHexStringBigEndian(h0))
            append(toHexStringBigEndian(h1))
            append(toHexStringBigEndian(h2))
            append(toHexStringBigEndian(h3))
            append(toHexStringBigEndian(h4))
            append(toHexStringBigEndian(h5))
            append(toHexStringBigEndian(h6))
            append(toHexStringBigEndian(h7))
        }
    }

    private fun rightRotate(value: Int, shift: Int): Int {
        return (value ushr shift) or (value shl (32 - shift))
    }
}
