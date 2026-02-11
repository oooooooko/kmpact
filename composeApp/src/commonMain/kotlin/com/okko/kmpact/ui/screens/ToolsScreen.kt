package com.okko.kmpact.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.okko.kmpact.domain.model.ToolCategory
import com.okko.kmpact.domain.model.ToolCommand
import com.okko.kmpact.domain.model.ToolCommands
import com.okko.kmpact.platform.SystemPaths
import com.okko.kmpact.presentation.tools.*
import com.okko.kmpact.ui.components.FileInputField
import com.okko.kmpact.ui.components.TerminalLog
import com.okko.kmpact.ui.components.devtools.*
import com.okko.kmpact.ui.theme.AppColors

/**
 * 通用工具界面
 * 
 * 显示所有AndroidCmdTools功能
 */
@Composable
fun ToolsScreen(
    category: ToolCategory,
    viewModel: ToolsViewModel = viewModel { ToolsViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 处理副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ToolsEffect.ShowToast -> {
                    println("Toast: ${effect.message}")
                }
                is ToolsEffect.ShowError -> {

                }
                is ToolsEffect.ShowSuccess -> {
                    
                }
            }
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Gray50)
    ) {
        // 左侧：工具列表
        ToolsList(
            category = category,
            selectedCommand = uiState.selectedCommand,
            onCommandSelect = { command ->
                viewModel.handleIntent(ToolsIntent.SelectCommand(command))
            },
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight()
        )
        
        // 右侧：工具详情和执行
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 工具详情
            if (uiState.selectedCommand != null) {
                if (uiState.selectedCommand!!.category == ToolCategory.DEV_TOOLS) {
                    // 开发类工具显示自定义界面
                    DevToolsPlaceholder(
                        command = uiState.selectedCommand!!,
                        onToolUsed = {
                            // 记录到最近使用
                            viewModel.handleIntent(ToolsIntent.RecordToolUsage(uiState.selectedCommand!!))
                        }
                    )
                } else {
                    // 其他工具显示正常的执行界面
                    ToolDetail(
                        command = uiState.selectedCommand!!,
                        parameters = uiState.parameters,
                        isExecuting = uiState.isExecuting,
                        onParameterChange = { key, value ->
                            viewModel.handleIntent(ToolsIntent.UpdateParameter(key, value))
                        },
                        onExecute = {
                            viewModel.handleIntent(ToolsIntent.ExecuteCommand)
                        },
                        onCancel = {
                            viewModel.handleIntent(ToolsIntent.CancelExecution)
                        }
                    )
                }
            } else {
                EmptyState()
            }
            
            // 终端日志和输入（仅非开发类工具显示）
            if (uiState.selectedCommand?.category != ToolCategory.DEV_TOOLS) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 终端日志
                    TerminalLog(
                        logs = uiState.logs,
                        onClear = { viewModel.handleIntent(ToolsIntent.ClearLogs) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // 输入框（仅在需要输入时显示）
                    if (uiState.needsInput || uiState.isExecuting) {
                        InputField(
                            value = uiState.currentInput,
                            onValueChange = { viewModel.handleIntent(ToolsIntent.UpdateInput(it)) },
                            onSend = { viewModel.handleIntent(ToolsIntent.SendInput) },
                            enabled = uiState.needsInput,
                            placeholder = if (uiState.needsInput) "输入内容后按回车或点击发送..." else "等待命令提示..."
                        )
                    }
                }
            }
        }
    }
}

/**
 * 工具列表
 */
@Composable
private fun ToolsList(
    category: ToolCategory,
    selectedCommand: ToolCommand?,
    onCommandSelect: (ToolCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    val commands = remember(category) {
        ToolCommands.getCommandsByCategory(category)
    }
    
    Column(
        modifier = modifier
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 分类标题
        Text(
            text = category.displayName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = category.description,
            fontSize = 13.sp,
            color = AppColors.TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        HorizontalDivider(color = AppColors.BorderLight)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 工具列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(commands) { command ->
                ToolItem(
                    command = command,
                    isSelected = command.id == selectedCommand?.id,
                    onClick = { onCommandSelect(command) }
                )
            }
        }
    }
}

/**
 * 工具项
 */
@Composable
private fun ToolItem(
    command: ToolCommand,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) AppColors.Blue50 else Color.Transparent
    val borderColor = if (isSelected) AppColors.Primary else AppColors.BorderLight
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = command.name,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) AppColors.Primary else AppColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        if (command.description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = command.description,
                fontSize = 12.sp,
                color = AppColors.TextTertiary,
                lineHeight = 16.sp
            )
        }
        
        // 标签
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            if (command.requiresDevice) {
                Badge(text = "需要设备", color = AppColors.Warning)
            }
            if (command.requiresInput) {
                Badge(text = "需要输入", color = AppColors.Info)
            }
        }
    }
}

/**
 * 标签
 */
@Composable
private fun Badge(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

/**
 * 工具详情
 */
@Composable
private fun ToolDetail(
    command: ToolCommand,
    parameters: Map<String, String>,
    isExecuting: Boolean,
    onParameterChange: (String, String) -> Unit,
    onExecute: () -> Unit,
    onCancel: () -> Unit = {}
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = command.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    if (command.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = command.description,
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }
                
                Icon(
                    imageVector = getCategoryIcon(command.category),
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            HorizontalDivider(color = AppColors.BorderLight)
            
            // 参数输入（根据命令类型动态生成）
            if (command.requiresInput) {
                ParameterInputs(
                    command = command,
                    parameters = parameters,
                    onParameterChange = onParameterChange
                )
            }
            
            // 执行按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onExecute,
                    enabled = !isExecuting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("执行中...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("执行命令", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                // 取消按钮（仅在执行中显示）
                if (isExecuting) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Error
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(120.dp)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("取消", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 参数输入
 */
@Composable
private fun ParameterInputs(
    command: ToolCommand,
    parameters: Map<String, String>,
    onParameterChange: (String, String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "参数配置",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary
        )
        
        // 根据命令类型显示不同的参数输入
        when (command.category) {
            ToolCategory.PACKAGE_TOOLS -> {
                PackageToolsParameters(command, parameters, onParameterChange)
            }
            ToolCategory.DEVICE_TOOLS -> {
                DeviceToolsParameters(command, parameters, onParameterChange)
            }
            ToolCategory.REVERSE_TOOLS -> {
                ReverseToolsParameters(command, parameters, onParameterChange)
            }
            else -> {
                // 通用参数输入
                GenericParameters(parameters, onParameterChange)
            }
        }
    }
}

/**
 * 包体工具参数
 */
@Composable
private fun PackageToolsParameters(
    command: ToolCommand,
    parameters: Map<String, String>,
    onParameterChange: (String, String) -> Unit
) {
    when (command.id) {
        "sign_apk" -> {
            FileInputField(
                label = "APK文件路径",
                value = parameters["apkPath"] ?: "",
                onValueChange = { onParameterChange("apkPath", it) },
                placeholder = "选择或输入APK文件路径",
                allowedExtensions = listOf("apk")
            )
            FileInputField(
                label = "密钥库路径",
                value = parameters["keystorePath"] ?: "",
                onValueChange = { onParameterChange("keystorePath", it) },
                placeholder = "选择或输入密钥库文件路径",
                allowedExtensions = listOf("jks", "keystore")
            )
            ParameterTextField(
                label = "密钥库密码",
                value = parameters["storePassword"] ?: "",
                onValueChange = { onParameterChange("storePassword", it) },
                placeholder = "输入密钥库密码",
                isPassword = true
            )
            ParameterTextField(
                label = "密钥别名",
                value = parameters["keyAlias"] ?: "",
                onValueChange = { onParameterChange("keyAlias", it) },
                placeholder = "输入密钥别名"
            )
        }
        "get_apk_signature" -> {
            FileInputField(
                label = "APK文件路径",
                value = parameters["apkPath"] ?: "",
                onValueChange = { onParameterChange("apkPath", it) },
                placeholder = "选择或输入APK文件路径",
                allowedExtensions = listOf("apk")
            )
        }
        "compare_package" -> {
            FileInputField(
                label = "原始包路径",
                value = parameters["originalPath"] ?: "",
                onValueChange = { onParameterChange("originalPath", it) },
                placeholder = "选择或输入原始包路径",
                allowedExtensions = listOf("apk", "aar", "jar", "aab")
            )
            FileInputField(
                label = "目标包路径",
                value = parameters["targetPath"] ?: "",
                onValueChange = { onParameterChange("targetPath", it) },
                placeholder = "选择或输入目标包路径",
                allowedExtensions = listOf("apk", "aar", "jar", "aab")
            )
        }
        else -> {
            FileInputField(
                label = "文件路径",
                value = parameters["filePath"] ?: "",
                onValueChange = { onParameterChange("filePath", it) },
                placeholder = "选择或输入文件路径"
            )
        }
    }
}

/**
 * 设备工具参数
 */
@Composable
private fun DeviceToolsParameters(
    command: ToolCommand,
    parameters: Map<String, String>,
    onParameterChange: (String, String) -> Unit
) {
    when (command.id) {
        "install_apk" -> {
            FileInputField(
                label = "APK文件路径",
                value = parameters["input"] ?: "",
                onValueChange = { onParameterChange("input", it) },
                placeholder = "选择或输入APK文件路径",
                allowedExtensions = listOf("apk")
            )
        }
        "uninstall_app" -> {
            ParameterTextField(
                label = "包名",
                value = parameters["input"] ?: "",
                onValueChange = { onParameterChange("input", it) },
                placeholder = "输入应用包名"
            )
        }
        "clear_app_data", "kill_app_process", "export_apk" -> {
            ParameterTextField(
                label = "应用包名",
                value = parameters["packageName"] ?: "",
                onValueChange = { onParameterChange("packageName", it) },
                placeholder = "输入应用包名，如：com.example.app"
            )
        }
        else -> {
            // 大多数设备工具不需要额外参数
            Text(
                text = "此命令不需要额外参数",
                fontSize = 13.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}

/**
 * 逆向工具参数
 */
@Composable
private fun ReverseToolsParameters(
    command: ToolCommand,
    parameters: Map<String, String>,
    onParameterChange: (String, String) -> Unit
) {
    val extensions = when (command.id) {
        "apktool_decompile", "jadx_view" -> listOf("apk")
        "jd_gui_view", "jar_to_dex" -> listOf("jar")
        "dex_to_jar" -> listOf("dex")
        else -> null
    }
    
    FileInputField(
        label = "文件路径",
        value = parameters["filePath"] ?: "",
        onValueChange = { onParameterChange("filePath", it) },
        placeholder = "选择或输入文件路径",
        allowedExtensions = extensions
    )
}

/**
 * 通用参数输入
 */
@Composable
private fun GenericParameters(
    parameters: Map<String, String>,
    onParameterChange: (String, String) -> Unit
) {
    Text(
        text = "此命令不需要额外参数",
        fontSize = 13.sp,
        color = AppColors.TextSecondary
    )
}

/**
 * 参数输入框
 */
@Composable
private fun ParameterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextSecondary
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
            visualTransformation = if (isPassword) {
                androidx.compose.ui.text.input.PasswordVisualTransformation()
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
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
 * 输入框组件
 */
@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean,
    placeholder: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 快捷路径标签
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "快捷路径:",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                
                // 下载文件夹
                QuickPathChip(
                    label = "下载",
                    onClick = {
                        val downloadsPath = SystemPaths.getDownloadsPath()
                        onValueChange(downloadsPath)
                    },
                    enabled = enabled
                )
                
                // 桌面文件夹
                QuickPathChip(
                    label = "桌面",
                    onClick = {
                        val desktopPath = SystemPaths.getDesktopPath()
                        onValueChange(desktopPath)
                    },
                    enabled = enabled
                )
            }
            
            // 快捷输入标签（y/n和数字0-9）
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "快捷输入:",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                
                QuickInputChip(label = "y", onClick = { onValueChange("y") }, enabled = enabled)
                QuickInputChip(label = "n", onClick = { onValueChange("n") }, enabled = enabled)
                
                Spacer(modifier = Modifier.width(4.dp))
                
                (0..9).forEach { num ->
                    QuickInputChip(
                        label = num.toString(),
                        onClick = { onValueChange(num.toString()) },
                        enabled = enabled
                    )
                }
            }
            
            // 输入框和发送按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.BorderLight,
                        disabledBorderColor = AppColors.BorderLight,
                        disabledTextColor = AppColors.TextSecondary
                    ),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Send
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSend = { if (enabled) onSend() }
                    )
                )
                
                Button(
                    onClick = onSend,
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary,
                        disabledContainerColor = AppColors.Gray300
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "发送",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("发送", fontSize = 14.sp)
                }
            }
        }
    }
}

/**
 * 快捷路径标签
 */
@Composable
private fun QuickPathChip(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = if (enabled) AppColors.Blue100 else AppColors.Gray200,
        modifier = Modifier.height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = if (enabled) AppColors.Primary else AppColors.Gray500,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) AppColors.Primary else AppColors.Gray500
            )
        }
    }
}

/**
 * 快捷输入标签（数字和字母）
 */
@Composable
private fun QuickInputChip(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        color = if (enabled) AppColors.Gray100 else AppColors.Gray200,
        modifier = Modifier.size(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) AppColors.TextPrimary else AppColors.Gray500
            )
        }
    }
}

/**
 * 空状态
 */
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "请从左侧选择一个工具",
                fontSize = 16.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}

/**
 * 获取分类图标
 */
private fun getCategoryIcon(category: ToolCategory): ImageVector {
    return when (category) {
        ToolCategory.PACKAGE_TOOLS -> Icons.Default.Archive
        ToolCategory.DEVICE_TOOLS -> Icons.Default.PhoneAndroid
        ToolCategory.REVERSE_TOOLS -> Icons.Default.Code
        ToolCategory.KEY_TOOLS -> Icons.Default.Key
        ToolCategory.DEV_TOOLS -> Icons.Default.Code
    }
}

/**
 * 开发类工具占位符
 */
@Composable
private fun DevToolsPlaceholder(
    command: ToolCommand,
    onToolUsed: () -> Unit
) {
    // 记录工具使用（只在首次渲染时记录）
    LaunchedEffect(command.id) {
        onToolUsed()
    }
    
    when (command.id) {
        "json_beautify" -> JsonBeautifyComponent()
        "regex_cheatsheet" -> RegexCheatsheetComponent()
        "encoding_converter" -> EncodingConverterComponent()
        "timestamp_converter" -> TimestampConverterComponent()
        "color_converter" -> ColorConverterComponent()
        "radix_converter" -> RadixConverterComponent()
        "android_icon_generator" -> AndroidIconGeneratorComponent()
        "qrcode_tool" -> QrcodeToolComponent()
        else -> {
            // 默认占位符
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 工具图标
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    // 工具名称
                    Text(
                        text = command.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    
                    // 工具描述
                    Text(
                        text = command.description,
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 提示信息
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Blue50),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Text(
                                text = "此功能正在开发中",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Primary
                            )
                            
                            Text(
                                text = "开发类工具将采用全新的交互界面，敬请期待！",
                                fontSize = 13.sp,
                                color = AppColors.TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
