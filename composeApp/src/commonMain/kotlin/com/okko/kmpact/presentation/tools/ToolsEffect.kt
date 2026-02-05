package com.okko.kmpact.presentation.tools

import com.okko.kmpact.presentation.base.BaseEffect

/**
 * 工具界面的副作用
 */
sealed interface ToolsEffect : BaseEffect {
    
    /**
     * 显示Toast
     */
    data class ShowToast(val message: String) : ToolsEffect
    
    /**
     * 显示错误
     */
    data class ShowError(val title: String, val message: String) : ToolsEffect
    
    /**
     * 显示成功
     */
    data class ShowSuccess(val message: String) : ToolsEffect
}
