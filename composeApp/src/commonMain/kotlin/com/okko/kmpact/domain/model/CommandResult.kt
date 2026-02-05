package com.okko.kmpact.domain.model

/**
 * 命令执行结果
 */
data class CommandResult(
    /**
     * 是否成功
     */
    val success: Boolean,
    
    /**
     * 输出内容
     */
    val output: String,
    
    /**
     * 错误信息
     */
    val error: String? = null,
    
    /**
     * 退出码
     */
    val exitCode: Int = 0
)
