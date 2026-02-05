package com.okko.kmpact.presentation.base

/**
 * MVI架构 - UiState基类
 * 
 * 作用：
 * - 统一状态模型规范
 * - 提供通用UI状态字段
 * 
 * 设计原则：
 * - 不可变（Immutable）
 * - 可复制、可对比
 * - 不包含一次性行为（如Toast、Navigation）
 */
interface BaseUiState {
    /**
     * 是否正在加载
     */
    val isLoading: Boolean
    
    /**
     * 错误信息
     */
    val error: String?
}
