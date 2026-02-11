package com.okko.kmpact.ui.components.devtools.radix

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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.ui.theme.AppColors

/**
 * 进制转换UI组件
 */
@Composable
fun RadixConverterUI(
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var selectedRadix by remember { mutableStateOf(RadixType.DECIMAL) }
    var conversionResults by remember { mutableStateOf<Map<RadixType, Result<String>>>(emptyMap()) }
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
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "进制转换",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "支持 2、4、8、10、16 进制互转",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
        
        // 输入区域
        InputSection(
            inputText = inputText,
            onInputTextChange = { inputText = it },
            selectedRadix = selectedRadix,
            onRadixChange = { selectedRadix = it },
            onConvert = {
                if (inputText.isNotEmpty()) {
                    val results = RadixConverter.convertToAll(inputText, selectedRadix)
                    conversionResults = results
                    
                    // 检查是否有错误
                    val hasError = results.values.any { it.isFailure }
                    errorMessage = if (hasError) {
                        results.values.firstOrNull { it.isFailure }?.exceptionOrNull()?.message
                    } else {
                        null
                    }
                } else {
                    errorMessage = "请输入要转换的数字"
                }
            }
        )
        
        // 错误信息
        if (errorMessage != null) {
            ErrorCard(errorMessage = errorMessage!!)
        }
        
        // 转换结果
        if (conversionResults.isNotEmpty()) {
            ResultsSection(conversionResults = conversionResults)
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
    selectedRadix: RadixType,
    onRadixChange: (RadixType) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题和操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "输入数字",
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
            
            // 进制选择
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "源进制",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextSecondary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadixType.entries.forEach { radix ->
                        FilterChip(
                            selected = selectedRadix == radix,
                            onClick = { onRadixChange(radix) },
                            label = {
                                Text(
                                    text = radix.displayName,
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
                
                // 提示有效字符
                Text(
                    text = "有效字符：${RadixConverter.getValidChars(selectedRadix)}",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary
                )
            }
            
            // 输入框
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                placeholder = { 
                    Text(
                        "输入${selectedRadix.displayName}数字，例如：${getExampleForRadix(selectedRadix)}",
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
                    fontSize = 16.sp
                ),
                singleLine = true
            )
            
            // 转换按钮
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
 * 结果区域
 */
@Composable
private fun ResultsSection(conversionResults: Map<RadixType, Result<String>>) {
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
            
            RadixType.entries.forEach { radix ->
                val result = conversionResults[radix]
                if (result != null) {
                    ResultItem(
                        radix = radix,
                        result = result
                    )
                }
            }
        }
    }
}

/**
 * 结果项
 */
@Composable
private fun ResultItem(radix: RadixType, result: Result<String>) {
    val clipboardManager = LocalClipboardManager.current
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (result.isSuccess) AppColors.Gray50 else AppColors.Red50
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = radix.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextSecondary
                    )
                    
                    // 进制标识
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AppColors.Blue50
                    ) {
                        Text(
                            text = "${radix.radix}进制",
                            fontSize = 10.sp,
                            color = AppColors.Primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                result.fold(
                    onSuccess = { value ->
                        Text(
                            text = value,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextPrimary
                        )
                    },
                    onFailure = { error ->
                        Text(
                            text = error.message ?: "转换失败",
                            fontSize = 14.sp,
                            color = AppColors.Error
                        )
                    }
                )
            }
            
            if (result.isSuccess) {
                IconButton(
                    onClick = {
                        result.getOrNull()?.let { value ->
                            clipboardManager.setText(AnnotatedString(value))
                        }
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
}

/**
 * 获取示例数字
 */
private fun getExampleForRadix(radix: RadixType): String {
    return when (radix) {
        RadixType.BINARY -> "1010"
        RadixType.QUATERNARY -> "22"
        RadixType.OCTAL -> "12"
        RadixType.DECIMAL -> "10"
        RadixType.HEXADECIMAL -> "A"
    }
}
