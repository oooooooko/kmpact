package com.okko.kmpact.ui.components.devtools.encoding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
 * 信息编码转换UI组件
 */
@Composable
fun EncodingConverterUI(
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(EncodingCategory.ENCODE) }
    var selectedType by remember { mutableStateOf<EncodingType?>(null) }
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Gray50)
    ) {
        // 左侧边栏
        LeftSidebar(
            selectedCategory = selectedCategory,
            onCategoryChange = { 
                selectedCategory = it
                selectedType = null
                inputText = ""
                outputText = ""
                errorMessage = null
            },
            selectedType = selectedType,
            onTypeSelect = { 
                selectedType = it
                outputText = ""
                errorMessage = null
            },
            modifier = Modifier.width(280.dp).fillMaxHeight()
        )
        
        // 右侧工作区
        WorkArea(
            selectedType = selectedType,
            inputText = inputText,
            onInputTextChange = { inputText = it },
            outputText = outputText,
            onOutputTextChange = { outputText = it },
            errorMessage = errorMessage,
            onErrorMessageChange = { errorMessage = it },
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
    }
}

/**
 * 左侧边栏
 */
@Composable
private fun LeftSidebar(
    selectedCategory: EncodingCategory,
    onCategoryChange: (EncodingCategory) -> Unit,
    selectedType: EncodingType?,
    onTypeSelect: (EncodingType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Transform,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "编码转换",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
        }
        
        // 分类标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EncodingCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(category) },
                    label = {
                        Text(
                            text = category.displayName,
                            fontSize = 14.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        
        // 编码类型列表
        val typesList = remember(selectedCategory) {
            EncodingType.getTypesByCategory(selectedCategory)
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(typesList) { type ->
                EncodingTypeItem(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = { onTypeSelect(type) }
                )
            }
        }
    }
}

/**
 * 编码类型列表项
 */
@Composable
private fun EncodingTypeItem(
    type: EncodingType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) AppColors.Blue50 else AppColors.Gray50
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = type.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextPrimary
            )
            Text(
                text = type.description,
                fontSize = 12.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}

/**
 * 工作区
 */
@Composable
private fun WorkArea(
    selectedType: EncodingType?,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    outputText: String,
    onOutputTextChange: (String) -> Unit,
    errorMessage: String?,
    onErrorMessageChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    
    if (selectedType == null) {
        // 空状态
        Box(
            modifier = modifier
                .background(AppColors.Gray50)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Transform,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "选择一个编码类型开始转换",
                    fontSize = 16.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .background(AppColors.Gray50)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 标题
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = selectedType.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = selectedType.description,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
            
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "输入",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextPrimary
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 粘贴按钮
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
                            
                            // 清空按钮
                            TextButton(
                                onClick = { 
                                    onInputTextChange("")
                                    onOutputTextChange("")
                                    onErrorMessageChange(null)
                                }
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
                        placeholder = { Text("请输入要转换的内容...", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Primary,
                            unfocusedBorderColor = AppColors.BorderLight
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    )
                }
            }
            
            // 转换按钮
            Button(
                onClick = {
                    val result = EncodingUtils.convert(inputText, selectedType)
                    result.fold(
                        onSuccess = { 
                            onOutputTextChange(it)
                            onErrorMessageChange(null)
                        },
                        onFailure = { 
                            onOutputTextChange("")
                            onErrorMessageChange("转换失败: ${it.message}")
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary
                ),
                enabled = inputText.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始转换", fontSize = 16.sp)
            }
            
            // 错误信息
            if (errorMessage != null) {
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
            
            // 输出区域
            if (outputText.isNotEmpty()) {
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
                                text = "输出",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.TextPrimary
                            )
                            
                            // 复制按钮
                            TextButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(outputText))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("复制", fontSize = 14.sp)
                            }
                        }
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.Gray50
                        ) {
                            Text(
                                text = outputText,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AppColors.TextPrimary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
