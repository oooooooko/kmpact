package com.okko.kmpact.ui.components.devtools.radix

/**
 * 进制类型
 */
enum class RadixType(val displayName: String, val radix: Int, val prefix: String) {
    BINARY("二进制", 2, "0b"),
    QUATERNARY("四进制", 4, ""),
    OCTAL("八进制", 8, "0o"),
    DECIMAL("十进制", 10, ""),
    HEXADECIMAL("十六进制", 16, "0x")
}

/**
 * 进制转换工具类
 */
object RadixConverter {
    
    /**
     * 转换数字到指定进制
     */
    fun convert(input: String, fromRadix: RadixType, toRadix: RadixType): Result<String> {
        return try {
            // 清理输入
            val cleanInput = cleanInput(input, fromRadix)
            
            if (cleanInput.isEmpty()) {
                return Result.failure(Exception("输入不能为空"))
            }
            
            // 转换为十进制
            val decimalValue = cleanInput.toLong(fromRadix.radix)
            
            // 转换为目标进制
            val result = when (toRadix) {
                RadixType.BINARY -> decimalValue.toString(2)
                RadixType.QUATERNARY -> decimalValue.toString(4)
                RadixType.OCTAL -> decimalValue.toString(8)
                RadixType.DECIMAL -> decimalValue.toString(10)
                RadixType.HEXADECIMAL -> decimalValue.toString(16).uppercase()
            }
            
            Result.success(toRadix.prefix + result)
        } catch (e: NumberFormatException) {
            Result.failure(Exception("无效的${fromRadix.displayName}数字"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 转换到所有进制
     */
    fun convertToAll(input: String, fromRadix: RadixType): Map<RadixType, Result<String>> {
        return RadixType.entries.associateWith { toRadix ->
            convert(input, fromRadix, toRadix)
        }
    }
    
    /**
     * 清理输入字符串
     */
    private fun cleanInput(input: String, radix: RadixType): String {
        var cleaned = input.trim().lowercase()
        
        // 移除常见前缀
        cleaned = cleaned.removePrefix("0b")  // 二进制
            .removePrefix("0o")  // 八进制
            .removePrefix("0x")  // 十六进制
            .removePrefix("0")   // 可能的八进制前缀
        
        // 移除空格和下划线（用于分隔的字符）
        cleaned = cleaned.replace(" ", "").replace("_", "")
        
        return cleaned
    }
    
    /**
     * 验证输入是否有效
     */
    fun isValidInput(input: String, radix: RadixType): Boolean {
        val cleanInput = cleanInput(input, radix)
        
        if (cleanInput.isEmpty()) return false
        
        return try {
            cleanInput.toLong(radix.radix)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取进制的有效字符集
     */
    fun getValidChars(radix: RadixType): String {
        return when (radix) {
            RadixType.BINARY -> "0-1"
            RadixType.QUATERNARY -> "0-3"
            RadixType.OCTAL -> "0-7"
            RadixType.DECIMAL -> "0-9"
            RadixType.HEXADECIMAL -> "0-9, A-F"
        }
    }
    
    /**
     * 格式化显示（添加分隔符）
     */
    fun formatDisplay(value: String, radix: RadixType): String {
        val cleanValue = value.removePrefix(radix.prefix)
        
        // 每4位添加一个空格（从右往左）
        return cleanValue.reversed()
            .chunked(4)
            .joinToString(" ")
            .reversed()
    }
}
