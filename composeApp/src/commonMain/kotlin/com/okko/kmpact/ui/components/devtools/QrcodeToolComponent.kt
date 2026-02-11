package com.okko.kmpact.ui.components.devtools

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.ui.theme.AppColors

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
    var inputText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 输入区域
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
        
        // 生成按钮
        Button(
            onClick = {
                // TODO: 生成二维码逻辑
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = inputText.isNotBlank()
        ) {
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
        
        // 预览区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Gray50),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "二维码预览",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                
                // 二维码预览占位符
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(2.dp, AppColors.BorderLight, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (inputText.isBlank()) {
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
                                text = "二维码将在这里显示",
                                fontSize = 12.sp,
                                color = AppColors.TextTertiary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // TODO: 显示生成的二维码
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(120.dp)
                            )
                            Text(
                                text = "二维码生成中...",
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // 下载按钮
                if (inputText.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // TODO: 下载PNG
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
                            Text("PNG", fontSize = 14.sp)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                // TODO: 下载SVG
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
                            Text("SVG", fontSize = 14.sp)
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
    var decodedContent by remember { mutableStateOf("") }
    var hasImage by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 上传区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Blue50),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "上传二维码图片",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                
                // 上传区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            color = if (hasImage) AppColors.Success else AppColors.BorderLight,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            // TODO: 打开文件选择器
                            hasImage = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!hasImage) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "点击上传二维码图片",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = "支持 PNG、JPG、JPEG 格式",
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = AppColors.Success,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "图片已上传",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Success
                            )
                            Text(
                                text = "点击重新选择",
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }
                
                // 解码按钮
                Button(
                    onClick = {
                        // TODO: 解码二维码逻辑
                        decodedContent = "这是解码后的示例内容\nhttps://example.com"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = hasImage
                ) {
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
                                // TODO: 复制到剪贴板
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
                        color = AppColors.Green50
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "内容类型",
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary
                            )
                            Text(
                                text = "文本/网址", // TODO: 根据内容自动识别类型
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "解码内容",
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary
                            )
                            Text(
                                text = decodedContent,
                                fontSize = 14.sp,
                                color = AppColors.TextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    
                    // 操作按钮
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // TODO: 清除结果
                                decodedContent = ""
                                hasImage = false
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
                                // TODO: 如果是网址，打开浏览器
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("打开", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}