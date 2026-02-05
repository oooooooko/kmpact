package com.okko.kmpact.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
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
import kotlinx.coroutines.launch

/**
 * iTerm风格的终端日志组件
 * 
 * 支持：
 * - 文本选择和复制
 * - 一键复制所有日志
 * - 清除日志
 * - 自动滚动
 * 
 * @param logs 日志列表
 * @param onClear 清除日志回调
 * @param modifier 修饰符
 */
@Composable
fun TerminalLog(
    logs: List<LogEntry>,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var showCopyToast by remember { mutableStateOf(false) }
    
    // 自动滚动到最新日志
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }
    
    // 复制提示
    if (showCopyToast) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showCopyToast = false
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalColors.Background, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // macOS风格的窗口控制按钮
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFFFF5F56), RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFFFFBD2E), RoundedCornerShape(6.dp))
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF27C93F), RoundedCornerShape(6.dp))
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Terminal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TerminalColors.Text,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            // 操作按钮组
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 复制所有日志按钮
                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val allLogs = logs.joinToString("\n") { log ->
                                "[${log.timestamp}] ${log.level.name}: ${log.message}"
                            }
                            clipboardManager.setText(AnnotatedString(allLogs))
                            showCopyToast = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "复制所有日志",
                            tint = TerminalColors.Text,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                // 清除按钮
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清除日志",
                        tint = TerminalColors.Text,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 复制提示
        if (showCopyToast) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalColors.Success.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = TerminalColors.Success,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "已复制到剪贴板",
                    fontSize = 12.sp,
                    color = TerminalColors.Success,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // 日志内容区域（支持文本选择）
        SelectionContainer {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(TerminalColors.ContentBackground, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (logs.isEmpty()) {
                    item {
                        Text(
                            text = "$ 等待命令执行...",
                            fontSize = 13.sp,
                            color = TerminalColors.Prompt,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    items(logs) { log ->
                        LogEntryItem(log)
                    }
                }
            }
        }
        
        // 底部状态栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${logs.size} 条日志",
                fontSize = 10.sp,
                color = TerminalColors.Text.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 提示文本
                Text(
                    text = "可选中文本复制",
                    fontSize = 10.sp,
                    color = TerminalColors.Text.copy(alpha = 0.4f),
                    fontFamily = FontFamily.Monospace
                )
                
                if (logs.isNotEmpty()) {
                    val lastLog = logs.last()
                    Text(
                        text = lastLog.timestamp,
                        fontSize = 10.sp,
                        color = TerminalColors.Text.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * 单条日志项
 */
@Composable
private fun LogEntryItem(log: LogEntry) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 时间戳
        Text(
            text = log.timestamp,
            fontSize = 12.sp,
            color = TerminalColors.Timestamp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(80.dp)
        )
        
        // 日志级别标记
        Text(
            text = log.level.symbol,
            fontSize = 12.sp,
            color = log.level.color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        
        // 日志内容
        Text(
            text = log.message,
            fontSize = 12.sp,
            color = log.level.color,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 日志条目数据类
 */
data class LogEntry(
    val timestamp: String,
    val level: LogLevel,
    val message: String
)

/**
 * 日志级别
 */
enum class LogLevel(val symbol: String, val color: Color) {
    INFO("ℹ", TerminalColors.Info),
    SUCCESS("✓", TerminalColors.Success),
    WARNING("⚠", TerminalColors.Warning),
    ERROR("✗", TerminalColors.Error),
    COMMAND("$", TerminalColors.Command),
    OUTPUT("→", TerminalColors.Output)
}

/**
 * 终端配色方案
 */
object TerminalColors {
    val Background = Color(0xFF1E1E1E)
    val ContentBackground = Color(0xFF252526)
    val Text = Color(0xFFCCCCCC)
    val Prompt = Color(0xFF4EC9B0)
    val Timestamp = Color(0xFF858585)
    
    // 日志级别颜色
    val Info = Color(0xFF4FC1FF)
    val Success = Color(0xFF4EC9B0)
    val Warning = Color(0xFFDCDCAA)
    val Error = Color(0xFFF48771)
    val Command = Color(0xFFC586C0)
    val Output = Color(0xFF9CDCFE)
}
