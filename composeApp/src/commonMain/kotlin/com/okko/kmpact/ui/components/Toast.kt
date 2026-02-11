package com.okko.kmpact.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.ui.theme.AppColors
import kotlinx.coroutines.delay

/**
 * Toast 类型
 */
enum class ToastType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

/**
 * Toast 位置
 */
enum class ToastPosition {
    TOP,
    CENTER,
    BOTTOM
}

/**
 * Toast 数据
 */
data class ToastData(
    val message: String,
    val type: ToastType = ToastType.INFO,
    val duration: Long = 3000L,
    val position: ToastPosition = ToastPosition.BOTTOM
)

/**
 * Toast 组件
 */
@Composable
fun Toast(
    toastData: ToastData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(toastData) {
        if (toastData != null) {
            visible = true
            delay(toastData.duration)
            visible = false
            delay(300) // 等待动画完成
            onDismiss()
        }
    }
    
    // 根据位置确定对齐方式
    val alignment = when (toastData?.position) {
        ToastPosition.TOP -> Alignment.TopCenter
        ToastPosition.CENTER -> Alignment.Center
        ToastPosition.BOTTOM -> Alignment.BottomCenter
        null -> Alignment.BottomCenter
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        AnimatedVisibility(
            visible = visible && toastData != null,
            enter = when (toastData?.position) {
                ToastPosition.TOP -> slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
                ToastPosition.CENTER -> fadeIn(animationSpec = tween(300)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(300)
                )
                else -> slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exit = when (toastData?.position) {
                ToastPosition.TOP -> slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
                ToastPosition.CENTER -> fadeOut(animationSpec = tween(300)) + scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(300)
                )
                else -> slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            if (toastData != null) {
                ToastContent(toastData)
            }
        }
    }
}

/**
 * Toast 内容
 */
@Composable
private fun ToastContent(toastData: ToastData) {
    val (icon, iconColor, backgroundColor) = when (toastData.type) {
        ToastType.SUCCESS -> Triple(Icons.Default.CheckCircle, AppColors.Success, Color(0xFF10B981).copy(alpha = 0.9f))
        ToastType.ERROR -> Triple(Icons.Default.Error, AppColors.Error, Color(0xFFEF4444).copy(alpha = 0.9f))
        ToastType.WARNING -> Triple(Icons.Default.Warning, AppColors.Warning, Color(0xFFF59E0B).copy(alpha = 0.9f))
        ToastType.INFO -> Triple(Icons.Default.Info, AppColors.Info, Color(0xFF3B82F6).copy(alpha = 0.9f))
    }
    
    // 根据位置调整 padding
    val verticalPadding = when (toastData.position) {
        ToastPosition.TOP -> PaddingValues(top = 48.dp, start = 24.dp, end = 24.dp)
        ToastPosition.CENTER -> PaddingValues(horizontal = 24.dp)
        ToastPosition.BOTTOM -> PaddingValues(bottom = 48.dp, start = 24.dp, end = 24.dp)
    }
    
    Row(
        modifier = Modifier
            .padding(verticalPadding)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = toastData.message,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            lineHeight = 20.sp
        )
    }
}

/**
 * Toast 状态管理
 */
class ToastState {
    private val _toastData = mutableStateOf<ToastData?>(null)
    val toastData: State<ToastData?> = _toastData
    
    fun showToast(
        message: String, 
        type: ToastType = ToastType.INFO, 
        duration: Long = 3000L,
        position: ToastPosition = ToastPosition.BOTTOM
    ) {
        _toastData.value = ToastData(message, type, duration, position)
    }
    
    fun showSuccess(
        message: String, 
        duration: Long = 3000L,
        position: ToastPosition = ToastPosition.BOTTOM
    ) {
        showToast(message, ToastType.SUCCESS, duration, position)
    }
    
    fun showError(
        message: String, 
        duration: Long = 3000L,
        position: ToastPosition = ToastPosition.BOTTOM
    ) {
        showToast(message, ToastType.ERROR, duration, position)
    }
    
    fun showWarning(
        message: String, 
        duration: Long = 3000L,
        position: ToastPosition = ToastPosition.BOTTOM
    ) {
        showToast(message, ToastType.WARNING, duration, position)
    }
    
    fun showInfo(
        message: String, 
        duration: Long = 3000L,
        position: ToastPosition = ToastPosition.BOTTOM
    ) {
        showToast(message, ToastType.INFO, duration, position)
    }
    
    fun dismiss() {
        _toastData.value = null
    }
}

/**
 * 记住 Toast 状态
 */
@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}
