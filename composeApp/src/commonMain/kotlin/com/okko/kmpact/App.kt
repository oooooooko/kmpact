package com.okko.kmpact

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.okko.kmpact.domain.model.ToolCategory
import com.okko.kmpact.presentation.tools.ToolsViewModel
import com.okko.kmpact.ui.components.Sidebar
import com.okko.kmpact.ui.screens.DeviceManagerScreen
import com.okko.kmpact.ui.screens.RecentToolsScreen
import com.okko.kmpact.ui.screens.ToolsScreen

/**
 * 应用主入口
 */
@Composable
fun App() {
    MaterialTheme {
        var selectedMenuItem by remember { mutableStateOf("recent_tools") }
        var refreshTrigger by remember { mutableStateOf(0) }
        
        // 创建一个全局的 ToolsViewModel 用于管理最近使用
        val toolsViewModel: ToolsViewModel = viewModel { ToolsViewModel() }
        
        Row(modifier = Modifier.fillMaxSize()) {
            // 侧边栏
            Sidebar(
                selectedItem = selectedMenuItem,
                onItemClick = { 
                    selectedMenuItem = it
                    // 切换到最近使用时刷新列表
                    if (it == "recent_tools") {
                        refreshTrigger++
                    }
                }
            )
            
            // 主内容区域
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedMenuItem) {
                    "recent_tools" -> {
                        // 最近使用
                        val recentTools = remember(refreshTrigger) { 
                            toolsViewModel.getRecentTools() 
                        }
                        RecentToolsScreen(
                            recentTools = recentTools,
                            onToolClick = { tool ->
                                // 根据工具分类跳转到对应页面
                                selectedMenuItem = when (tool.category) {
                                    ToolCategory.PACKAGE_TOOLS -> "package_tools"
                                    ToolCategory.DEVICE_TOOLS -> "adb_terminal"
                                    ToolCategory.REVERSE_TOOLS -> "reverse_engineer"
                                    ToolCategory.KEY_TOOLS -> "ssh_key_tools"
                                }
                            }
                        )
                    }
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
