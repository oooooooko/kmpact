package com.okko.kmpact.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.okko.kmpact.presentation.packagetools.*
import com.okko.kmpact.ui.components.TerminalLog
import com.okko.kmpact.ui.theme.AppColors

/**
 * Package Tools 主界面
 * 包含APK签名和包对比功能
 */
@Composable
fun PackageToolsScreen(
    viewModel: PackageToolsViewModel = viewModel { PackageToolsViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 处理副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PackageToolsEffect.ShowToast -> {
                    // TODO: 显示Toast
                    println("Toast: ${effect.message}")
                }
                is PackageToolsEffect.ShowErrorDialog -> {
                    // TODO: 显示错误对话框
                }
                is PackageToolsEffect.ShowSuccessDialog -> {
                    // TODO: 显示成功对话框
                }
                is PackageToolsEffect.OpenFilePicker -> {
                    // TODO: 打开文件选择器
                }
                is PackageToolsEffect.OpenFolderPicker -> {
                    // TODO: 打开文件夹选择器
                }
                is PackageToolsEffect.DownloadDiffReport -> {
                    // TODO: 下载报告
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Gray50)
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // 页面标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Package & Build",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.Primary
                    )
                ) {
                    Text("Production", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    )
                ) {
                    Text("Debug", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        
        // APK签名和对齐区域
        ApkSigningSection(
            uiState = uiState,
            onIntent = viewModel::handleIntent
        )
        
        // 包对比区域
        PackageComparisonSection(
            uiState = uiState,
            onIntent = viewModel::handleIntent
        )
        
        // 终端日志区域
        TerminalLog(
            logs = uiState.logs,
            onClear = { viewModel.handleIntent(PackageToolsIntent.ClearLogs) }
        )
    }
}

/**
 * APK签名和对齐区域
 */
@Composable
private fun ApkSigningSection(
    uiState: PackageToolsUiState,
    onIntent: (PackageToolsIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 标题和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APK Signing & Alignment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                
                if (uiState.isV3SchemeSupported) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AppColors.Success)
                        )
                        Text(
                            text = "V3 SCHEME SUPPORTED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Success,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            
            // 表单区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 左列
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 目标制品
                    FileInputField(
                        label = "Target Artifact",
                        value = uiState.targetArtifactPath,
                        placeholder = "Select .apk, .jar, or .aab file",
                        onValueChange = { onIntent(PackageToolsIntent.SelectTargetArtifact(it)) },
                        onBrowseClick = { /* TODO: 打开文件选择器 */ }
                    )
                    
                    // 密钥库文件
                    FileInputField(
                        label = "Keystore File",
                        value = uiState.keystoreFilePath,
                        placeholder = "Path to .jks",
                        onValueChange = { onIntent(PackageToolsIntent.SelectKeystoreFile(it)) },
                        onBrowseClick = { /* TODO: 打开文件选择器 */ }
                    )
                    
                    // 输出目录
                    FileInputField(
                        label = "Output Directory",
                        value = uiState.outputDirectory,
                        placeholder = "/build/outputs/aligned",
                        onValueChange = { onIntent(PackageToolsIntent.InputOutputDirectory(it)) },
                        onBrowseClick = { /* TODO: 打开文件夹选择器 */ },
                        trailingIcon = Icons.Default.Clear
                    )
                }
                
                // 右列
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 密钥库密码
                    PasswordInputField(
                        label = "Keystore Password",
                        value = uiState.keystorePassword,
                        onValueChange = { onIntent(PackageToolsIntent.InputKeystorePassword(it)) }
                    )
                    
                    // 密钥别名
                    TextInputField(
                        label = "Key Alias",
                        value = uiState.keyAlias,
                        placeholder = "upload-key",
                        onValueChange = { onIntent(PackageToolsIntent.InputKeyAlias(it)) }
                    )
                }
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onIntent(PackageToolsIntent.ResetSigningForm) }
                ) {
                    Text(
                        "Reset Form",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Button(
                    onClick = { onIntent(PackageToolsIntent.StartSignAndOptimize) },
                    enabled = !uiState.isSigning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sign & Optimize Package",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 包对比区域
 */
@Composable
private fun PackageComparisonSection(
    uiState: PackageToolsUiState,
    onIntent: (PackageToolsIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 标题
            Text(
                text = "Package Comparison",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            
            // 对比区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 原始制品
                ArtifactComparisonCard(
                    title = "ORIGINAL ARTIFACT (A)",
                    artifactPath = uiState.originalArtifactPath,
                    files = uiState.originalArtifactFiles,
                    onDropFile = { onIntent(PackageToolsIntent.SelectOriginalArtifact(it)) },
                    modifier = Modifier.weight(1f)
                )
                
                // 目标制品
                ArtifactComparisonCard(
                    title = "TARGET ARTIFACT (B)",
                    artifactPath = uiState.targetArtifactPathForComparison,
                    files = uiState.targetArtifactFiles,
                    onDropFile = { onIntent(PackageToolsIntent.SelectTargetArtifactForComparison(it)) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 生成报告按钮
            Button(
                onClick = { onIntent(PackageToolsIntent.GenerateDetailedDiffReport) },
                enabled = !uiState.isGeneratingReport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Generate Detailed Diff Report",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 文件输入框组件
 */
@Composable
private fun FileInputField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onBrowseClick: () -> Unit,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextSecondary,
            letterSpacing = 0.3.sp
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    fontSize = 13.sp,
                    color = AppColors.TextTertiary
                )
            },
            trailingIcon = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (trailingIcon != null && value.isNotEmpty()) {
                        IconButton(
                            onClick = { onValueChange("") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = trailingIcon,
                                contentDescription = "Clear",
                                tint = AppColors.TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onBrowseClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Browse",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.BorderLight
            ),
            singleLine = true
        )
    }
}

/**
 * 密码输入框组件
 */
@Composable
private fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextSecondary,
            letterSpacing = 0.3.sp
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    "••••••••••",
                    fontSize = 13.sp,
                    color = AppColors.TextTertiary
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.BorderLight
            ),
            singleLine = true
        )
    }
}

/**
 * 文本输入框组件
 */
@Composable
private fun TextInputField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextSecondary,
            letterSpacing = 0.3.sp
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    fontSize = 13.sp,
                    color = AppColors.TextTertiary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.BorderLight
            ),
            singleLine = true
        )
    }
}

/**
 * 制品对比卡片
 */
@Composable
private fun ArtifactComparisonCard(
    title: String,
    artifactPath: String,
    files: List<ArtifactFile>,
    onDropFile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(1.dp, AppColors.BorderLight, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Gray50)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 标题
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextTertiary,
            letterSpacing = 1.sp
        )
        
        // 拖放区域
        if (artifactPath.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(2.dp, AppColors.BorderLight, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .clickable { /* TODO: 打开文件选择器 */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        tint = AppColors.TextTertiary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        "Drop target file here",
                        fontSize = 12.sp,
                        color = AppColors.TextTertiary
                    )
                }
            }
        } else {
            // 文件列表
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                files.forEach { file ->
                    ArtifactFileItem(file)
                }
            }
        }
    }
}

/**
 * 制品文件项
 */
@Composable
private fun ArtifactFileItem(file: ArtifactFile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (file.isModified) AppColors.Blue50 else Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (file.isModified) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Modified",
                    tint = AppColors.Primary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = file.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (file.isModified) AppColors.Primary else AppColors.TextPrimary
            )
        }
        
        Text(
            text = file.size,
            fontSize = 11.sp,
            fontWeight = if (file.isModified) FontWeight.Bold else FontWeight.Normal,
            color = if (file.isModified) AppColors.Primary else AppColors.TextSecondary
        )
    }
}
