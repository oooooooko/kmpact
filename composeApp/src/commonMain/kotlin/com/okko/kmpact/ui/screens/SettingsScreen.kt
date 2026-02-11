package com.okko.kmpact.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.data.settings.*
import com.okko.kmpact.ui.components.Toast
import com.okko.kmpact.ui.components.rememberToastState
import com.okko.kmpact.ui.theme.AppColors
import kotlinx.coroutines.launch

/**
 * 设置界面
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val toastState = rememberToastState()
    var downloadPath by remember { mutableStateOf(SettingsManager.getCurrentSettings().downloadPath) }
    var isPathValid by remember { mutableStateOf(validatePath(downloadPath)) }
    
    // 验证路径
    LaunchedEffect(downloadPath) {
        isPathValid = validatePath(downloadPath)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Gray50)
    ) {
        // 顶部标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(32.dp)
            )
            
            Column {
                Text(
                    text = "应用设置",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "配置应用的下载路径和其他设置",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
        
        // 设置内容区域
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 下载路径设置
            DownloadPathSection(
                downloadPath = downloadPath,
                isPathValid = isPathValid,
                onPathChange = { downloadPath = it },
                onSelectFolder = {
                    selectFolder()?.let { path ->
                        downloadPath = path
                    }
                }
            )
        }
        
        // 底部按钮区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.TextSecondary
                )
            ) {
                Text("取消")
            }
            
            Button(
                onClick = {
                    val newSettings = AppSettings(
                        downloadPath = downloadPath
                    )
                    if (SettingsManager.saveSettings(newSettings)) {
                        toastState.showSuccess("设置已保存")
                        // 延迟返回
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(1500)
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                onBack()
                            }
                        }
                    } else {
                        toastState.showError("保存失败，请重试")
                    }
                },
                enabled = isPathValid,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存设置")
            }
        }
    }
    
    // Toast 显示
    Toast(
        toastData = toastState.toastData.value,
        onDismiss = { toastState.dismiss() }
    )
}

/**
 * 下载路径设置区域
 */
@Composable
private fun DownloadPathSection(
    downloadPath: String,
    isPathValid: Boolean,
    onPathChange: (String) -> Unit,
    onSelectFolder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "默认下载路径",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "设置图标和文件的默认下载位置",
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }
            }
            
            // 路径输入行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = downloadPath,
                    onValueChange = onPathChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入下载路径", fontSize = 14.sp) },
                    trailingIcon = {
                        if (isPathValid) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "路径有效",
                                tint = AppColors.Success
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "路径无效",
                                tint = AppColors.Error
                            )
                        }
                    },
                    isError = !isPathValid,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.BorderLight,
                        errorBorderColor = AppColors.Error
                    ),
                    singleLine = true
                )
                
                Button(
                    onClick = onSelectFolder,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("选择")
                }
            }
            
            // 路径验证提示
            if (!isPathValid) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AppColors.Error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "路径不存在或无效，请检查后重试",
                        fontSize = 12.sp,
                        color = AppColors.Error
                    )
                }
            }
        }
    }
}
