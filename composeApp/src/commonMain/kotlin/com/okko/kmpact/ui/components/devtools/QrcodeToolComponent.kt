package com.okko.kmpact.ui.components.devtools

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.ui.theme.AppColors
import com.okko.kmpact.ui.components.FileInputField
import com.okko.kmpact.utils.QrCodeService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 二维码编码解码工具组件
 */
@Composable
fun QrcodeToolComponent(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("生成二维码", "解码二维码")
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Gray50)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 标题区域
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(32.dp)
            )
            
            Column {
                Text(
                    text = "二维码编码解码",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "生成和识别二维码",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
        
        // Tab选项卡
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Tab标签
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Surface(
                            onClick = { selectedTab = index },
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedTab == index) AppColors.Primary else Color.Transparent
                        ) {
                            Text(
                                text = title,
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedTab == index) Color.White else AppColors.TextSecondary
                            )
                        }
                    }
                }
                
                // Tab内容
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    when (selectedTab) {
                        0 -> QrcodeGenerateTab()
                        1 -> QrcodeDecodeTab()
                    }
                }
            }
        }
    }
}

/**
 * 生成二维码Tab
 */
@Composable
private fun QrcodeGenerateTab() {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    
    var inputText by remember { mutableStateOf("") }
    var errorCorrectionLevel by remember { mutableStateOf(ErrorCorrectionLevel.M) }
    var qrSize by remember { mutableStateOf(400) }
    var customSize by remember { mutableStateOf("400") }
    var useCustomSize by remember { mutableStateOf(false) }
    var qrVersion by remember { mutableStateOf(QrVersion.AUTO) }
    var showErrorCorrectionDropdown by remember { mutableStateOf(false) }
    var showVersionDropdown by remember { mutableStateOf(false) }
    var isQrGenerated by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var qrCodeImage by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // 当输入内容改变时重置生成状态
    LaunchedEffect(inputText) {
        if (inputText.isBlank()) {
            isQrGenerated = false
            isGenerating = false
            qrCodeImage = null
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 输入区域
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
                    text = "输入内容",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text(
                            text = "请输入要生成二维码的内容...\n支持文本、网址、联系人信息等",
                            color = AppColors.TextTertiary
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.BorderLight
                    ),
                    maxLines = 5
                )
            }
        }
        
        // 设置区域
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
                    text = "二维码设置",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                
                // 容错率设置
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "容错率",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextPrimary
                    )
                    
                    Box {
                        Surface(
                            onClick = { showErrorCorrectionDropdown = !showErrorCorrectionDropdown },
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.Gray100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = errorCorrectionLevel.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AppColors.TextPrimary
                                    )
                                    Text(
                                        text = errorCorrectionLevel.description,
                                        fontSize = 12.sp,
                                        color = AppColors.TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = if (showErrorCorrectionDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = AppColors.TextSecondary
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showErrorCorrectionDropdown,
                            onDismissRequest = { showErrorCorrectionDropdown = false }
                        ) {
                            ErrorCorrectionLevel.entries.forEach { level ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = level.displayName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = level.description,
                                                fontSize = 12.sp,
                                                color = AppColors.TextSecondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        errorCorrectionLevel = level
                                        showErrorCorrectionDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 尺寸设置
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "图片尺寸",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextPrimary
                    )
                    
                    // 预设尺寸选择
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presetSizes = listOf(200, 300, 400, 500, 600)
                        presetSizes.forEach { size ->
                            FilterChip(
                                selected = !useCustomSize && qrSize == size,
                                onClick = {
                                    qrSize = size
                                    useCustomSize = false
                                },
                                label = {
                                    Text(
                                        text = "${size}px",
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppColors.Primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    
                    // 自定义尺寸
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = useCustomSize,
                            onCheckedChange = { useCustomSize = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppColors.Primary
                            )
                        )
                        Text(
                            text = "自定义尺寸:",
                            fontSize = 14.sp,
                            color = AppColors.TextPrimary
                        )
                        OutlinedTextField(
                            value = customSize,
                            onValueChange = { 
                                customSize = it
                                it.toIntOrNull()?.let { size ->
                                    if (size > 0) qrSize = size
                                }
                            },
                            modifier = Modifier.width(100.dp),
                            placeholder = { Text("400", fontSize = 14.sp) },
                            suffix = { Text("px", fontSize = 12.sp, color = AppColors.TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.BorderLight
                            ),
                            singleLine = true,
                            enabled = useCustomSize
                        )
                    }
                }
                
                // 码版本设置
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "码版本",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextPrimary
                    )
                    
                    Box {
                        Surface(
                            onClick = { showVersionDropdown = !showVersionDropdown },
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.Gray100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = qrVersion.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AppColors.TextPrimary
                                    )
                                    Text(
                                        text = qrVersion.description,
                                        fontSize = 12.sp,
                                        color = AppColors.TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = if (showVersionDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = AppColors.TextSecondary
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showVersionDropdown,
                            onDismissRequest = { showVersionDropdown = false },
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            QrVersion.entries.forEach { version ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = version.displayName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = version.description,
                                                fontSize = 12.sp,
                                                color = AppColors.TextSecondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        qrVersion = version
                                        showVersionDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 生成按钮
        Button(
            onClick = {
                // 使用QrCodeService生成真正的二维码
                isGenerating = true
                isQrGenerated = false
                qrCodeImage = null
                
                coroutineScope.launch {
                    try {
                        val actualSize = if (useCustomSize) {
                            customSize.toIntOrNull()?.takeIf { it > 0 } ?: 400
                        } else {
                            qrSize
                        }
                        
                        val image = QrCodeService.generateQrCode(
                            content = inputText,
                            size = actualSize,
                            errorCorrectionLevel = errorCorrectionLevel
                        )
                        
                        qrCodeImage = image
                        delay(500) // 短暂延迟以显示加载状态
                        isGenerating = false
                        isQrGenerated = image != null
                    } catch (e: Exception) {
                        isGenerating = false
                        println("生成二维码失败: ${e.message}")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = inputText.isNotBlank() && !isGenerating
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "生成中...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "生成二维码",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // 预览和下载区域
        if (inputText.isNotBlank()) {
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "二维码预览",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                    
                    // 二维码预览
                    Box(
                        modifier = Modifier
                            .size(minOf(qrSize.dp, 300.dp))
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(2.dp, AppColors.BorderLight, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isGenerating -> {
                                // 生成中状态
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = AppColors.Primary,
                                        strokeWidth = 4.dp
                                    )
                                    Text(
                                        text = "正在生成二维码...",
                                        fontSize = 14.sp,
                                        color = AppColors.TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            isQrGenerated -> {
                                // 已生成状态 - 显示真正的二维码
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    qrCodeImage?.let { image ->
                                        Image(
                                            bitmap = image,
                                            contentDescription = "生成的二维码",
                                            modifier = Modifier
                                                .size(minOf(qrSize.dp, 300.dp))
                                                .background(Color.White, RoundedCornerShape(4.dp))
                                                .border(1.dp, AppColors.BorderLight, RoundedCornerShape(4.dp))
                                                .padding(4.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } ?: run {
                                        // 备用显示
                                        Box(
                                            modifier = Modifier
                                                .size(200.dp)
                                                .background(AppColors.Gray100, RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "二维码加载中...",
                                                fontSize = 12.sp,
                                                color = AppColors.TextSecondary
                                            )
                                        }
                                    }
                                    
                                    Text(
                                        text = "二维码生成成功",
                                        fontSize = 12.sp,
                                        color = AppColors.Success,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "内容: ${inputText.take(20)}${if (inputText.length > 20) "..." else ""}",
                                        fontSize = 10.sp,
                                        color = AppColors.TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            else -> {
                                // 默认状态
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = AppColors.TextTertiary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "点击生成按钮创建二维码",
                                        fontSize = 12.sp,
                                        color = AppColors.TextTertiary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    
                    // 设置信息显示
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.Blue50
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "容错率:",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                                Text(
                                    text = errorCorrectionLevel.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextPrimary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "尺寸:",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                                Text(
                                    text = "${qrSize} × ${qrSize} px",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextPrimary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "版本:",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                                Text(
                                    text = qrVersion.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextPrimary
                                )
                            }
                        }
                    }
                    
                    // 下载按钮
                    if (isQrGenerated && qrCodeImage != null) {
                        var downloadStatus by remember { mutableStateOf("") }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    // 使用QrCodeService下载PNG
                                    coroutineScope.launch {
                                        downloadStatus = "正在下载 PNG..."
                                        
                                        val actualSize = if (useCustomSize) {
                                            customSize.toIntOrNull()?.takeIf { it > 0 } ?: 400
                                        } else {
                                            qrSize
                                        }
                                        
                                        val result = QrCodeService.downloadPng(
                                            content = inputText,
                                            size = actualSize,
                                            errorCorrectionLevel = errorCorrectionLevel
                                        )
                                        
                                        downloadStatus = result
                                        delay(if (result.contains("已保存到")) 5000 else 2000) // 成功时显示更长时间
                                        downloadStatus = ""
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = AppColors.Primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("下载 PNG", fontSize = 14.sp)
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    // 使用QrCodeService下载SVG
                                    coroutineScope.launch {
                                        downloadStatus = "正在下载 SVG..."
                                        
                                        val actualSize = if (useCustomSize) {
                                            customSize.toIntOrNull()?.takeIf { it > 0 } ?: 400
                                        } else {
                                            qrSize
                                        }
                                        
                                        val result = QrCodeService.downloadSvg(
                                            content = inputText,
                                            size = actualSize,
                                            errorCorrectionLevel = errorCorrectionLevel
                                        )
                                        
                                        downloadStatus = result
                                        delay(if (result.contains("已保存到")) 5000 else 2000) // 成功时显示更长时间
                                        downloadStatus = ""
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = AppColors.Primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("下载 SVG", fontSize = 14.sp)
                            }
                        }
                        
                        // 下载状态提示
                        if (downloadStatus.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = if (downloadStatus.contains("已保存到") || downloadStatus.contains("完成")) 
                                    AppColors.Green50 else if (downloadStatus.contains("失败")) 
                                    AppColors.Red50 else AppColors.Blue50
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    when {
                                        downloadStatus.contains("正在") -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = AppColors.Primary,
                                                strokeWidth = 2.dp
                                            )
                                        }
                                        downloadStatus.contains("已保存到") || downloadStatus.contains("完成") -> {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = AppColors.Success,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        downloadStatus.contains("失败") -> {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = null,
                                                tint = AppColors.Error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = AppColors.Info,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (downloadStatus.contains("已保存到:")) {
                                                downloadStatus.substringBefore("已保存到:")
                                            } else {
                                                downloadStatus
                                            },
                                            fontSize = 14.sp,
                                            color = when {
                                                downloadStatus.contains("已保存到") || downloadStatus.contains("完成") -> AppColors.Success
                                                downloadStatus.contains("失败") -> AppColors.Error
                                                else -> AppColors.Primary
                                            },
                                            fontWeight = FontWeight.Medium
                                        )
                                        
                                        // 如果包含文件路径，显示路径
                                        if (downloadStatus.contains("已保存到:")) {
                                            val filePath = downloadStatus.substringAfter("已保存到: ")
                                            Text(
                                                text = filePath,
                                                fontSize = 12.sp,
                                                color = AppColors.TextSecondary,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                    
                                    // 如果是成功状态，添加复制路径按钮
                                    if (downloadStatus.contains("已保存到:")) {
                                        IconButton(
                                            onClick = {
                                                val filePath = downloadStatus.substringAfter("已保存到: ")
                                                clipboardManager.setText(AnnotatedString(filePath))
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "复制路径",
                                                tint = AppColors.Primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 复制设置按钮
                        OutlinedButton(
                            onClick = {
                                val settings = "二维码设置信息:\n" +
                                        "容错率: ${errorCorrectionLevel.displayName}\n" +
                                        "尺寸: ${qrSize}×${qrSize}px\n" +
                                        "版本: ${qrVersion.displayName}\n" +
                                        "内容: $inputText"
                                clipboardManager.setText(AnnotatedString(settings))
                                
                                // 显示复制成功提示
                                coroutineScope.launch {
                                    downloadStatus = "设置信息已复制到剪贴板"
                                    delay(2000)
                                    downloadStatus = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.Primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("复制设置信息", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 解码二维码Tab
 */
@Composable
private fun QrcodeDecodeTab() {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    
    var selectedFilePath by remember { mutableStateOf("") }
    var decodedContent by remember { mutableStateOf("") }
    var isDecoding by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        
        // 文件选择区域
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
                    text = "选择二维码图片",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                
                // 使用统一的文件输入框
                FileInputField(
                    label = "二维码图片文件",
                    value = selectedFilePath,
                    onValueChange = { selectedFilePath = it },
                    placeholder = "选择二维码图片文件",
                    allowedExtensions = listOf("png", "jpg", "jpeg", "bmp", "gif")
                )
                
                // 解码按钮
                Button(
                    onClick = {
                        if (selectedFilePath.isNotBlank()) {
                            isDecoding = true
                            decodedContent = ""
                            
                            coroutineScope.launch {
                                try {
                                    val result = QrCodeService.decodeQrCodeFromPath(selectedFilePath)
                                    decodedContent = result ?: "无法识别二维码内容"
                                    delay(500) // 短暂延迟以显示加载状态
                                    isDecoding = false
                                } catch (e: Exception) {
                                    isDecoding = false
                                    decodedContent = "解码失败: ${e.message}"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = selectedFilePath.isNotBlank() && !isDecoding
                ) {
                    if (isDecoding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "解码中...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "解码二维码",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // 解码结果显示区域
        if (decodedContent.isNotEmpty()) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "解码结果",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextPrimary
                        )
                        
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(decodedContent))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "复制",
                                tint = AppColors.Primary
                            )
                        }
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (decodedContent.contains("失败") || decodedContent.contains("无法识别") || decodedContent.contains("不存在")) 
                            AppColors.Red50 else AppColors.Green50
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "解码内容",
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary
                            )
                            SelectionContainer {
                                Text(
                                    text = decodedContent,
                                    fontSize = 14.sp,
                                    color = AppColors.TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                    
                    // 操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                decodedContent = ""
                                selectedFilePath = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清除", fontSize = 14.sp)
                        }
                        
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(decodedContent))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("复制内容", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 数据模型 ====================

/**
 * 容错率等级
 */
enum class ErrorCorrectionLevel(
    val displayName: String,
    val description: String,
    val percentage: String
) {
    L("低 (L)", "约7%的错误恢复能力", "7%"),
    M("中 (M)", "约15%的错误恢复能力", "15%"),
    Q("高 (Q)", "约25%的错误恢复能力", "25%"),
    H("最高 (H)", "约30%的错误恢复能力", "30%")
}

/**
 * 二维码版本
 */
enum class QrVersion(
    val displayName: String,
    val description: String,
    val modules: String
) {
    AUTO("自动选择", "根据内容长度自动选择最适合的版本", "自动"),
    V1("版本 1", "最小版本，适合短文本", "21×21"),
    V2("版本 2", "适合短网址", "25×25"),
    V3("版本 3", "适合中等长度内容", "29×29"),
    V4("版本 4", "适合较长内容", "33×33"),
    V5("版本 5", "适合长文本", "37×37"),
    V6("版本 6", "适合很长的内容", "41×41"),
    V7("版本 7", "适合大量数据", "45×45"),
    V8("版本 8", "适合复杂数据", "49×49"),
    V9("版本 9", "适合大容量数据", "53×53"),
    V10("版本 10", "最大容量", "57×57")
}