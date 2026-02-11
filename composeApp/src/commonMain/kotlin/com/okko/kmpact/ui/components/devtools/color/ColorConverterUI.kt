package com.okko.kmpact.ui.components.devtools.color

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.ui.theme.AppColors

/**
 * 颜色转换UI组件
 */
@Composable
fun ColorConverterUI(
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var colorData by remember { mutableStateOf<ColorData?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
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
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "颜色值转换",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "支持 HEX、RGB、RGBA、HSL、HSLA、HSV、CMYK 格式互转",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
        
        // 输入区域
        InputSection(
            inputText = inputText,
            onInputTextChange = { inputText = it },
            onConvert = {
                val result = parseColor(inputText)
                if (result != null) {
                    colorData = result
                    errorMessage = null
                } else {
                    colorData = null
                    errorMessage = "无法识别的颜色格式，请输入有效的 HEX、RGB、RGBA 或 HSL 格式"
                }
            }
        )
        
        // 错误信息
        if (errorMessage != null) {
            ErrorCard(errorMessage = errorMessage!!)
        }
        
        // 颜色预览和转换结果
        if (colorData != null) {
            ColorPreviewSection(colorData = colorData!!)
            ColorFormatsSection(colorData = colorData!!)
        }
    }
}

/**
 * 输入区域
 */
@Composable
private fun InputSection(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onConvert: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "输入颜色值",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            clipboardManager.getText()?.text?.let {
                                onInputTextChange(it)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "粘贴",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("粘贴", fontSize = 14.sp)
                    }
                    
                    TextButton(
                        onClick = { onInputTextChange("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清空",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("清空", fontSize = 14.sp)
                    }
                }
            }
            
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                placeholder = { 
                    Text(
                        "输入颜色值，例如：#FF5733、rgb(255, 87, 51)、hsl(9, 100%, 60%)",
                        fontSize = 14.sp
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.BorderLight
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                singleLine = true
            )
            
            Button(
                onClick = onConvert,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary
                ),
                enabled = inputText.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.Transform,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("转换", fontSize = 16.sp)
            }
        }
    }
}

/**
 * 错误卡片
 */
@Composable
private fun ErrorCard(errorMessage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Red50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = AppColors.Error,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = AppColors.Error
            )
        }
    }
}

/**
 * 颜色预览区域
 */
@Composable
private fun ColorPreviewSection(colorData: ColorData) {
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
                text = "颜色预览",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            
            // 大色块预览
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        color = colorData.toComposeColor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = AppColors.BorderLight,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            
            // RGB 值显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ColorChannelInfo("R", colorData.red.toString(), Color.Red)
                ColorChannelInfo("G", colorData.green.toString(), Color.Green)
                ColorChannelInfo("B", colorData.blue.toString(), Color.Blue)
                if (colorData.alpha < 1.0f) {
                    val alphaStr = (colorData.alpha * 100).toInt() / 100.0
                    ColorChannelInfo("A", alphaStr.toString(), AppColors.Primary)
                }
            }
        }
    }
}

/**
 * 颜色通道信息
 */
@Composable
private fun ColorChannelInfo(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
    }
}

/**
 * 颜色格式区域
 */
@Composable
private fun ColorFormatsSection(colorData: ColorData) {
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
            Text(
                text = "转换结果",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            
            ColorFormatItem("HEX", colorData.toHex())
            ColorFormatItem("RGB", colorData.toRgb())
            ColorFormatItem("RGBA", colorData.toRgba())
            ColorFormatItem("HSL", colorData.toHsl())
            ColorFormatItem("HSLA", colorData.toHsla())
            ColorFormatItem("HSV", colorData.toHsv())
            ColorFormatItem("CMYK", colorData.toCmyk())
        }
    }
}

/**
 * 颜色格式项
 */
@Composable
private fun ColorFormatItem(format: String, value: String) {
    val clipboardManager = LocalClipboardManager.current
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = AppColors.Gray50
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = format,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AppColors.TextPrimary
                )
            }
            
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(value))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "复制",
                    tint = AppColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 解析颜色字符串
 */
private fun parseColor(input: String): ColorData? {
    val trimmed = input.trim()
    
    // 尝试各种格式
    return ColorData.fromHex(trimmed)
        ?: ColorData.fromRgba(trimmed)
        ?: ColorData.fromRgb(trimmed)
        ?: ColorData.fromHsl(trimmed)
}
