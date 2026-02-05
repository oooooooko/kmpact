package com.okko.kmpact.presentation.packagetools

import com.okko.kmpact.presentation.base.BaseEffect

/**
 * Package Tools 界面的副作用（一次性事件）
 * 
 * 设计原则：
 * - 只消费一次
 * - 与UiState严格区分
 * - 不参与状态回放
 */
sealed interface PackageToolsEffect : BaseEffect {
    
    /**
     * 显示Toast消息
     */
    data class ShowToast(val message: String) : PackageToolsEffect
    
    /**
     * 显示错误对话框
     */
    data class ShowErrorDialog(val title: String, val message: String) : PackageToolsEffect
    
    /**
     * 显示成功对话框
     */
    data class ShowSuccessDialog(val message: String) : PackageToolsEffect
    
    /**
     * 打开文件选择器
     */
    data class OpenFilePicker(val fileType: FileType) : PackageToolsEffect
    
    /**
     * 打开文件夹选择器
     */
    data object OpenFolderPicker : PackageToolsEffect
    
    /**
     * 下载差异报告
     */
    data class DownloadDiffReport(val reportPath: String) : PackageToolsEffect
}

/**
 * 文件类型
 */
enum class FileType {
    APK,        // APK文件
    AAB,        // AAB文件
    KEYSTORE    // 密钥库文件
}
