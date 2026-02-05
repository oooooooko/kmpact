package com.okko.kmpact.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.platform.FileChooser
import com.okko.kmpact.ui.theme.AppColors
import kotlinx.coroutines.launch

/**
 * 文件输入框组件
 * 
 * 支持：
 * - 手动输入路径
 * - 点击文件夹图标选择文件
 * - 拖拽文件到输入框（JVM 平台）
 * 
 * @param label 标签文本
 * @param value 当前值
 * @param onValueChange 值变化回调
 * @param placeholder 占位符文本
 * @param allowedExtensions 允许的文件扩展名（如 listOf("apk", "jar")）
 * @param isDirectory 是否选择文件夹
 */
@Composable
fun FileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    allowedExtensions: List<String>? = null,
    isDirectory: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val fileChooser = remember { FileChooser() }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        // 标签
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextSecondary
        )
        
        // 输入框和按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 文本输入框
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
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.BorderLight
                ),
                singleLine = true
            )
            
            // 文件选择按钮
            IconButton(
                onClick = {
                    scope.launch {
                        val selectedPath = if (isDirectory) {
                            fileChooser.chooseDirectory(title = label)
                        } else {
                            fileChooser.chooseFile(
                                title = label,
                                allowedExtensions = allowedExtensions
                            )
                        }
                        
                        selectedPath?.let { path ->
                            onValueChange(path)
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.Primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "选择${if (isDirectory) "文件夹" else "文件"}",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // 提示文本
        if (allowedExtensions != null && allowedExtensions.isNotEmpty()) {
            Text(
                text = "支持的文件类型: ${allowedExtensions.joinToString(", ") { ".$it" }}",
                fontSize = 11.sp,
                color = AppColors.TextTertiary
            )
        }
    }
}

/**
 * 拖拽区域组件（用于拖拽文件）
 * 
 * 注意：拖拽功能仅在 JVM 平台支持
 */
@Composable
fun FileDropZone(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    allowedExtensions: List<String>? = null,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextSecondary
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 2.dp,
                    color = if (isDragging) AppColors.Primary else AppColors.BorderLight,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    if (isDragging) AppColors.Blue50 else Color.White
                )
                .clickable {
                    // 点击也可以选择文件
                    val fileChooser = FileChooser()
                    val selectedPath = fileChooser.chooseFile(
                        title = label,
                        allowedExtensions = allowedExtensions
                    )
                    selectedPath?.let { onValueChange(it) }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = if (isDragging) AppColors.Primary else AppColors.TextTertiary,
                    modifier = Modifier.size(32.dp)
                )
                
                if (value.isEmpty()) {
                    Text(
                        text = if (isDragging) "释放文件" else "拖拽文件到此处或点击选择",
                        fontSize = 13.sp,
                        color = if (isDragging) AppColors.Primary else AppColors.TextSecondary
                    )
                } else {
                    Text(
                        text = value.split("/").lastOrNull() ?: value,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = value,
                        fontSize = 11.sp,
                        color = AppColors.TextTertiary
                    )
                }
            }
        }
        
        if (allowedExtensions != null && allowedExtensions.isNotEmpty()) {
            Text(
                text = "支持的文件类型: ${allowedExtensions.joinToString(", ") { ".$it" }}",
                fontSize = 11.sp,
                color = AppColors.TextTertiary
            )
        }
    }
}
