package com.okko.kmpact.ui.components.devtools.encoding

/**
 * 编码类型分类
 */
enum class EncodingCategory(val displayName: String) {
    ENCODE("加密"),
    DECODE("解密")
}

/**
 * 编码类型
 */
sealed class EncodingType(
    val name: String,
    val description: String,
    val category: EncodingCategory
) {
    // 加密类型
    object UnicodeEncode : EncodingType("Unicode编码", "转换为\\u开头的Unicode编码", EncodingCategory.ENCODE)
    object UrlEncode : EncodingType("URL编码", "转换为%开头的URL编码", EncodingCategory.ENCODE)
    object Base64Encode : EncodingType("Base64编码", "转换为Base64编码", EncodingCategory.ENCODE)
    object Md5Encode : EncodingType("MD5", "生成MD5哈希值", EncodingCategory.ENCODE)
    object HexEncode : EncodingType("十六进制编码", "转换为十六进制编码", EncodingCategory.ENCODE)
    object Sha1Encode : EncodingType("SHA1加密", "生成SHA1哈希值", EncodingCategory.ENCODE)
    object Sha256Encode : EncodingType("SHA256加密", "生成SHA256哈希值", EncodingCategory.ENCODE)
    
    // 解密类型
    object UnicodeDecode : EncodingType("Unicode解码", "解码\\u开头的Unicode编码", EncodingCategory.DECODE)
    object UrlDecode : EncodingType("URL解码", "解码%开头的URL编码", EncodingCategory.DECODE)
    object Base64Decode : EncodingType("Base64解码", "解码Base64编码", EncodingCategory.DECODE)
    object HexDecode : EncodingType("十六进制解码", "解码十六进制编码", EncodingCategory.DECODE)
    object CookieFormat : EncodingType("Cookie格式化", "格式化Cookie字符串", EncodingCategory.DECODE)
    
    companion object {
        fun getAllTypes(): List<EncodingType> = listOf(
            // 加密
            UnicodeEncode, UrlEncode, Base64Encode, Md5Encode,
            HexEncode, Sha1Encode, Sha256Encode,
            // 解密
            UnicodeDecode, UrlDecode, Base64Decode, HexDecode,
            CookieFormat
        )
        
        fun getTypesByCategory(category: EncodingCategory): List<EncodingType> {
            return getAllTypes().filter { it.category == category }
        }
    }
}
