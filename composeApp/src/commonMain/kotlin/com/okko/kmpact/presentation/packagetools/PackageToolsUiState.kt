package com.okko.kmpact.presentation.packagetools

import com.okko.kmpact.presentation.base.BaseUiState
import com.okko.kmpact.ui.components.LogEntry

/**
 * Package Tools 界面的UI状态
 * 
 * 设计原则：
 * - 不可变（Immutable）
 * - 描述界面在某一时刻的完整状态
 * - 提供UI渲染所需的全部数据
 */
data class PackageToolsUiState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    
    // ========== APK签名状态 ==========
    
    /**
     * 目标APK文件路径
     */
    val targetArtifactPath: String = "",
    
    /**
     * 密钥库密码
     */
    val keystorePassword: String = "",
    
    /**
     * 密钥库文件路径
     */
    val keystoreFilePath: String = "",
    
    /**
     * 密钥别名
     */
    val keyAlias: String = "",
    
    /**
     * 输出目录
     */
    val outputDirectory: String = "",
    
    /**
     * 是否支持V3签名
     */
    val isV3SchemeSupported: Boolean = true,
    
    /**
     * 签名进度（0-100）
     */
    val signingProgress: Int = 0,
    
    /**
     * 是否正在签名
     */
    val isSigning: Boolean = false,
    
    // ========== 包对比状态 ==========
    
    /**
     * 原始制品文件路径
     */
    val originalArtifactPath: String = "",
    
    /**
     * 原始制品文件列表
     */
    val originalArtifactFiles: List<ArtifactFile> = emptyList(),
    
    /**
     * 目标制品文件路径（用于对比）
     */
    val targetArtifactPathForComparison: String = "",
    
    /**
     * 目标制品文件列表
     */
    val targetArtifactFiles: List<ArtifactFile> = emptyList(),
    
    /**
     * 是否正在生成报告
     */
    val isGeneratingReport: Boolean = false,
    
    // ========== 保存的配置 ==========
    
    /**
     * 保存的签名配置列表
     */
    val savedProfiles: List<SigningProfile> = emptyList(),
    
    /**
     * 当前选中的配置
     */
    val selectedProfile: SigningProfile? = null,
    
    // ========== 日志输出 ==========
    
    /**
     * 终端日志列表
     */
    val logs: List<LogEntry> = emptyList()
) : BaseUiState

/**
 * 制品文件信息
 */
data class ArtifactFile(
    val name: String,
    val size: String,
    val isModified: Boolean = false
)

/**
 * 签名配置
 */
data class SigningProfile(
    val name: String,
    val version: String
)
