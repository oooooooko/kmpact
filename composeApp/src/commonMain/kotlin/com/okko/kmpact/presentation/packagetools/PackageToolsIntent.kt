package com.okko.kmpact.presentation.packagetools

import com.okko.kmpact.presentation.base.BaseIntent

/**
 * Package Tools 界面的用户意图（Intent）
 * 表达用户行为或系统事件，作为状态变化的唯一入口
 */
sealed interface PackageToolsIntent : BaseIntent {
    
    // ========== APK签名相关 ==========
    
    /**
     * 选择目标APK文件
     */
    data class SelectTargetArtifact(val filePath: String) : PackageToolsIntent
    
    /**
     * 输入密钥库密码
     */
    data class InputKeystorePassword(val password: String) : PackageToolsIntent
    
    /**
     * 选择密钥库文件
     */
    data class SelectKeystoreFile(val filePath: String) : PackageToolsIntent
    
    /**
     * 输入密钥别名
     */
    data class InputKeyAlias(val alias: String) : PackageToolsIntent
    
    /**
     * 输入输出目录
     */
    data class InputOutputDirectory(val directory: String) : PackageToolsIntent
    
    /**
     * 重置签名表单
     */
    data object ResetSigningForm : PackageToolsIntent
    
    /**
     * 开始签名和优化包
     */
    data object StartSignAndOptimize : PackageToolsIntent
    
    // ========== 包对比相关 ==========
    
    /**
     * 选择原始制品文件（用于对比）
     */
    data class SelectOriginalArtifact(val filePath: String) : PackageToolsIntent
    
    /**
     * 选择目标制品文件（用于对比）
     */
    data class SelectTargetArtifactForComparison(val filePath: String) : PackageToolsIntent
    
    /**
     * 生成详细差异报告
     */
    data object GenerateDetailedDiffReport : PackageToolsIntent
    
    // ========== 保存的配置相关 ==========
    
    /**
     * 选择保存的签名配置
     */
    data class SelectSavedProfile(val profileName: String) : PackageToolsIntent
    
    // ========== 日志相关 ==========
    
    /**
     * 清除日志
     */
    data object ClearLogs : PackageToolsIntent
}
