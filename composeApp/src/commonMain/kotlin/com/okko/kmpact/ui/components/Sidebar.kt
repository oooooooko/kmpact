package com.okko.kmpact.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.ui.theme.AppColors
import kmpact.composeapp.generated.resources.Res
import kmpact.composeapp.generated.resources.ic_launcher
import org.jetbrains.compose.resources.painterResource

/**
 * 侧边栏组件
 *
 * @param selectedItem 当前选中的菜单项
 * @param onItemClick 菜单项点击回调
 * @param onShowToast Toast显示回调
 */
@Composable
fun Sidebar(
    selectedItem: String,
    onItemClick: (String) -> Unit,
    onShowToast: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(252.dp)
            .fillMaxHeight()
            .background(AppColors.SidebarBg)
            .border(1.dp, AppColors.Blue100, RoundedCornerShape(0.dp))
    ) {
        // Logo区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_launcher),
                    contentDescription = "Logo",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = "AndroidCmdTools",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Blue900
            )
        }

        // 导航菜单
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            SidebarItem(
                icon = Icons.Default.History,
                label = "最近使用",
                isSelected = selectedItem == "recent_tools",
                onClick = { onItemClick("recent_tools") }
            )

            SidebarItem(
                icon = Icons.Default.DeviceHub,
                label = "设备管理",
                isSelected = selectedItem == "device_manager",
                onClick = { onItemClick("device_manager") }
            )

            SidebarItem(
                icon = Icons.Default.Build,
                label = "包体工具",
                isSelected = selectedItem == "package_tools",
                onClick = { onItemClick("package_tools") }
            )

            SidebarItem(
                icon = Icons.Default.Adb,
                label = "逆向工具",
                isSelected = selectedItem == "reverse_engineer",
                onClick = { onItemClick("reverse_engineer") }
            )

            SidebarItem(
                icon = Icons.Default.PhoneAndroid,
                label = "设备工具",
                isSelected = selectedItem == "adb_terminal",
                onClick = { onItemClick("adb_terminal") }
            )

            SidebarItem(
                icon = Icons.Default.Code,
                label = "开发类工具",
                isSelected = selectedItem == "dev_tools",
                onClick = { onItemClick("dev_tools") }
            )

            SidebarItem(
                icon = Icons.Default.Key,
                label = "SSH密钥工具",
                isSelected = selectedItem == "ssh_key_tools",
                onClick = { onItemClick("ssh_key_tools") }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 设置按钮
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            SettingsButton(
                onClick = {
                    onItemClick("settings")
                }
            )
        }
    }
}


/**
 * 侧边栏菜单项
 */
@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) AppColors.SidebarItemActive else Color.Transparent
    val textColor = if (isSelected) AppColors.Primary else AppColors.TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )
    }
}


/**
 * 设置按钮
 */
@Composable
private fun SettingsButton(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.Blue50)
            .border(1.dp, AppColors.Blue100, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "设置",
            tint = AppColors.Primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Release V2",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.Blue800
        )
    }
}
