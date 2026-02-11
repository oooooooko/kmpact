package com.okko.kmpact.ui.components.devtools.icongen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.ui.components.FileInputField
import com.okko.kmpact.ui.components.Toast
import com.okko.kmpact.ui.components.rememberToastState
import com.okko.kmpact.ui.theme.AppColors
import kotlinx.coroutines.launch

/**
 * Android 图标生成器 UI
 */
@Composable
fun IconGeneratorUI(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val toastState = rememberToastState()
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    var sourceImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var backgroundColor by remember { mutableStateOf(Color.Transparent) }
    var backgroundColorInput by remember { mutableStateOf("") }
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedPadding by remember { mutableStateOf(PaddingPreset.NONE) }
    var hasRoundCorner by remember { mutableStateOf(false) }
    var cornerRadius by remember { mutableStateOf("17.54") }
    var isGenerating by remember { mutableStateOf(false) }
    var generationResult by remember { mutableStateOf<IconGenerationResult?>(null) }
    
    // 创建当前配置
    val currentConfig = remember(
        backgroundColor,
        selectedPadding,
        hasRoundCorner,
        cornerRadius
    ) {
        IconConfig(
            backgroundColor = backgroundColor,
            paddingPercentage = selectedPadding.percentage,
            hasRoundCorner = hasRoundCorner,
            cornerRadiusPercentage = cornerRadius.toFloatOrNull() ?: 17.54f
        )
    }
    
    // 生成预览图标
    val previewIcon = remember(sourceImage, currentConfig) {
        if (sourceImage != null) {
            IconGenerator.generatePreviewIcon(sourceImage!!, currentConfig, 120)
        } else {
            null
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Gray50)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 标题
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Android 图标生成",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "生成各种尺寸的 Android 应用图标",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
        
        // 图片上传区域
        ImageUploadSection(
            selectedImagePath = selectedImagePath,
            onImageSelected = { path ->
                selectedImagePath = path
                sourceImage = IconGenerator.loadImage(path)
            }
        )
        
        // 预览区域
        if (previewIcon != null) {
            PreviewSection(previewIcon = previewIcon)
        }
        
        // 配置区域
        ConfigurationSection(
            backgroundColor = backgroundColor,
            onBackgroundColorChange = { backgroundColor = it },
            backgroundColorInput = backgroundColorInput,
            onBackgroundColorInputChange = { backgroundColorInput = it },
            showColorPicker = showColorPicker,
            onShowColorPickerChange = { showColorPicker = it },
            selectedPadding = selectedPadding,
            onPaddingChange = { selectedPadding = it },
            hasRoundCorner = hasRoundCorner,
            onRoundCornerChange = { hasRoundCorner = it },
            cornerRadius = cornerRadius,
            onCornerRadiusChange = { cornerRadius = it }
        )
        
        // 生成按钮
        GenerateButton(
            enabled = selectedImagePath != null && !isGenerating,
            isGenerating = isGenerating,
            onGenerate = {
                if (selectedImagePath != null) {
                    isGenerating = true
                    // 使用协程实现延迟和异步操作
                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                        try {
                            // 延迟 500ms 显示 loading 效果
                            kotlinx.coroutines.delay(500)
                            // 生成图标
                            val result = IconGenerator.generateIcons(selectedImagePath!!, currentConfig)
                            // 切换到主线程更新 UI
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                generationResult = result
                                isGenerating = false
                                // 显示 Toast
                                if (result.success) {
                                    toastState.showSuccess("图标生成成功！已保存到：${result.outputPath}")
                                } else {
                                    toastState.showError(result.errorMessage)
                                }
                            }
                        } catch (e: Exception) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                generationResult = IconGenerationResult(
                                    success = false,
                                    errorMessage = "生成失败: ${e.message}"
                                )
                                isGenerating = false
                                toastState.showError("生成失败: ${e.message}")
                            }
                        }
                    }
                }
            }
        )
        
        // 生成结果（可选，保留用于详细信息）
        if (generationResult != null && generationResult!!.success) {
            ResultSection(result = generationResult!!)
        }
    }
    
    // Toast 显示
    Toast(
        toastData = toastState.toastData.value,
        onDismiss = { toastState.dismiss() }
    )
}

/**
 * 图片上传区域
 */
@Composable
private fun ImageUploadSection(
    selectedImagePath: String?,
    onImageSelected: (String) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "上传图片",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            
            // 使用 FileInputField 组件
            FileInputField(
                label = "图片文件",
                value = selectedImagePath ?: "",
                onValueChange = { path ->
                    if (path.isNotEmpty()) {
                        onImageSelected(path)
                    }
                },
                placeholder = "选择 PNG 或 JPG 图片文件",
                allowedExtensions = listOf("png", "jpg", "jpeg")
            )
        }
    }
}

/**
 * 预览区域
 */
@Composable
private fun PreviewSection(previewIcon: ImageBitmap) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "实时预览",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            
            // 预览图标 - 缩小到 120dp
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(0.dp), // 移除圆角，显示真实效果
                color = AppColors.Gray50,
                border = BorderStroke(1.dp, AppColors.BorderLight)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = previewIcon,
                        contentDescription = "预览图标",
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
            
            Text(
                text = "预览尺寸: 120x120",
                fontSize = 12.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}

/**
 * 配置区域
 */
@Composable
private fun ConfigurationSection(
    backgroundColor: Color,
    onBackgroundColorChange: (Color) -> Unit,
    backgroundColorInput: String,
    onBackgroundColorInputChange: (String) -> Unit,
    showColorPicker: Boolean,
    onShowColorPickerChange: (Boolean) -> Unit,
    selectedPadding: PaddingPreset,
    onPaddingChange: (PaddingPreset) -> Unit,
    hasRoundCorner: Boolean,
    onRoundCornerChange: (Boolean) -> Unit,
    cornerRadius: String,
    onCornerRadiusChange: (String) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "图标配置",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            
            // 背景色选择
            BackgroundColorSection(
                backgroundColor = backgroundColor,
                onBackgroundColorChange = onBackgroundColorChange,
                backgroundColorInput = backgroundColorInput,
                onBackgroundColorInputChange = onBackgroundColorInputChange,
                showColorPicker = showColorPicker,
                onShowColorPickerChange = onShowColorPickerChange
            )
            
            Divider(color = AppColors.BorderLight)
            
            // 内边距选择
            PaddingSection(
                selectedPadding = selectedPadding,
                onPaddingChange = onPaddingChange
            )
            
            Divider(color = AppColors.BorderLight)
            
            // 圆角选择
            RoundCornerSection(
                hasRoundCorner = hasRoundCorner,
                onRoundCornerChange = onRoundCornerChange,
                cornerRadius = cornerRadius,
                onCornerRadiusChange = onCornerRadiusChange
            )
        }
    }
}

/**
 * 背景色选择区域
 */
@Composable
private fun BackgroundColorSection(
    backgroundColor: Color,
    onBackgroundColorChange: (Color) -> Unit,
    backgroundColorInput: String,
    onBackgroundColorInputChange: (String) -> Unit,
    showColorPicker: Boolean,
    onShowColorPickerChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "背景颜色",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextSecondary
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 颜色预览
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(backgroundColor, RoundedCornerShape(8.dp))
                    .border(1.dp, AppColors.BorderLight, RoundedCornerShape(8.dp))
            )
            
            // 预设颜色
            listOf(
                Color.Transparent to "透明",
                Color.White to "白色",
                Color.Black to "黑色",
                Color(0xFF2196F3) to "蓝色",
                Color(0xFF4CAF50) to "绿色"
            ).forEach { (color, name) ->
                Surface(
                    onClick = { onBackgroundColorChange(color) },
                    shape = RoundedCornerShape(8.dp),
                    color = color,
                    border = BorderStroke(
                        width = if (backgroundColor == color) 2.dp else 1.dp,
                        color = if (backgroundColor == color) AppColors.Primary else AppColors.BorderLight
                    ),
                    modifier = Modifier.size(40.dp)
                ) {}
            }
        }
        
        // 颜色输入框
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = backgroundColorInput,
                onValueChange = { input ->
                    onBackgroundColorInputChange(input)
                    // 尝试解析颜色
                    parseColorFromInput(input)?.let { color ->
                        onBackgroundColorChange(color)
                    }
                },
                placeholder = { Text("输入颜色值 (#RRGGBB)", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.BorderLight
                ),
                singleLine = true
            )
            
            Text(
                text = "支持 #RGB、#RRGGBB 格式",
                fontSize = 12.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}

/**
 * 解析颜色输入
 */
private fun parseColorFromInput(input: String): Color? {
    val hex = input.trim().removePrefix("#")
    
    return try {
        when (hex.length) {
            3 -> {
                // #RGB -> #RRGGBB
                val r = hex.substring(0, 1).repeat(2).toInt(16)
                val g = hex.substring(1, 2).repeat(2).toInt(16)
                val b = hex.substring(2, 3).repeat(2).toInt(16)
                Color(r, g, b)
            }
            6 -> {
                // #RRGGBB
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                Color(r, g, b)
            }
            8 -> {
                // #RRGGBBAA
                val r = hex.substring(0, 2).toInt(16)
                val g = hex.substring(2, 4).toInt(16)
                val b = hex.substring(4, 6).toInt(16)
                val a = hex.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * 内边距选择区域
 */
@Composable
private fun PaddingSection(
    selectedPadding: PaddingPreset,
    onPaddingChange: (PaddingPreset) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "内边距比例",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextSecondary
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaddingPreset.entries.forEach { preset ->
                FilterChip(
                    selected = selectedPadding == preset,
                    onClick = { onPaddingChange(preset) },
                    label = {
                        Text(
                            text = preset.displayName,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

/**
 * 圆角选择区域
 */
@Composable
private fun RoundCornerSection(
    hasRoundCorner: Boolean,
    onRoundCornerChange: (Boolean) -> Unit,
    cornerRadius: String,
    onCornerRadiusChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "圆角",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary
            )
            
            Switch(
                checked = hasRoundCorner,
                onCheckedChange = onRoundCornerChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppColors.Primary
                )
            )
        }
        
        if (hasRoundCorner) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "圆角半径:",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
                
                OutlinedTextField(
                    value = cornerRadius,
                    onValueChange = onCornerRadiusChange,
                    modifier = Modifier.width(120.dp),
                    suffix = { Text("%") },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.BorderLight
                    ),
                    singleLine = true
                )
                
                Text(
                    text = "（默认 17.54%）",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}

/**
 * 生成按钮
 */
@Composable
private fun GenerateButton(
    enabled: Boolean,
    isGenerating: Boolean,
    onGenerate: () -> Unit
) {
    Button(
        onClick = onGenerate,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary
        ),
        enabled = enabled
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("生成中...", fontSize = 16.sp)
        } else {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("生成并导出图标", fontSize = 16.sp)
        }
    }
}

/**
 * 结果区域
 */
@Composable
private fun ResultSection(result: IconGenerationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.success) AppColors.Green50 else AppColors.Red50
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (result.success) AppColors.Success else AppColors.Error,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (result.success) "生成成功！" else "生成失败",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (result.success) AppColors.Success else AppColors.Error
                )
                
                if (result.success) {
                    Text(
                        text = "导出路径: ${result.outputPath}",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = "已生成 ${result.generatedFiles.size} 个文件",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                } else {
                    Text(
                        text = result.errorMessage,
                        fontSize = 12.sp,
                        color = AppColors.Error
                    )
                }
            }
        }
    }
}
