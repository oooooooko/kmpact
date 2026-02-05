package com.okko.kmpact.platform

/**
 * 文件选择器接口
 * 
 * 使用 expect/actual 机制实现跨平台文件选择
 */
expect class FileChooser() {
    
    /**
     * 选择单个文件
     * 
     * @param title 对话框标题
     * @param allowedExtensions 允许的文件扩展名（如 "apk", "jar"）
     * @return 选中的文件路径，如果取消则返回 null
     */
    fun chooseFile(
        title: String = "选择文件",
        allowedExtensions: List<String>? = null
    ): String?
    
    /**
     * 选择文件夹
     * 
     * @param title 对话框标题
     * @return 选中的文件夹路径，如果取消则返回 null
     */
    fun chooseDirectory(
        title: String = "选择文件夹"
    ): String?
}
