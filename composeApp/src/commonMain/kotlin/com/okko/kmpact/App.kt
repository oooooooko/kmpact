package com.okko.kmpact

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.okko.kmpact.domain.model.ToolCategory
import com.okko.kmpact.ui.components.Sidebar
import com.okko.kmpact.ui.screens.DeviceManagerScreen
import com.okko.kmpact.ui.screens.ToolsScreen

/**
 * 应用主入口
 */
@Composable
fun App() {
    MaterialTheme {
        var selectedMenuItem by remember { mutableStateOf("package_tools") }
        
        Row(modifier = Modifier.fillMaxSize()) {
            // 侧边栏
            Sidebar(
                selectedItem = selectedMenuItem,
                onItemClick = { selectedMenuItem = it }
            )
            
            // 主内容区域
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedMenuItem) {
                    "device_manager" -> {
                        // 设备管理
                        DeviceManagerScreen()
                    }
                    "package_tools" -> {
                        // 包体工具
                        ToolsScreen(category = ToolCategory.PACKAGE_TOOLS)
                    }
                    "reverse_engineer" -> {
                        // 逆向工具
                        ToolsScreen(category = ToolCategory.REVERSE_TOOLS)
                    }
                    "adb_terminal" -> {
                        // 终端工具（暂时显示设备工具）
                        ToolsScreen(category = ToolCategory.DEVICE_TOOLS)
                    }
                    "ssh_key_tools" -> {
                        // SSH密钥工具
                        ToolsScreen(category = ToolCategory.KEY_TOOLS)
                    }
                }
            }
        }
    }
}