package com.okko.kmpact.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.domain.model.ToolCommand
import com.okko.kmpact.ui.theme.AppColors

/**
 * 最近使用工具屏幕
 */
@Composable
fun RecentToolsScreen(
    recentTools: List<ToolCommand>,
    onToolClick: (ToolCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.MainBg)
            .padding(32.dp)
    ) {
        // 标题
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Column {
                Text(
                    text = "最近使用",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "快速访问最近使用的工具",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
        
        // 工具列表
        if (recentTools.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "暂无记录",
                        tint = AppColors.TextTertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "暂无最近使用的工具",
                        fontSize = 16.sp,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = "使用任意工具后，将在此处显示",
                        fontSize = 14.sp,
                        color = AppColors.TextTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentTools) { tool ->
                    RecentToolItem(
                        tool = tool,
                        onClick = { onToolClick(tool) }
                    )
                }
            }
        }
    }
}

/**
 * 最近使用工具项
 */
@Composable
private fun RecentToolItem(
    tool: ToolCommand,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.CardBg)
            .border(1.dp, AppColors.Blue100, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 分类标签
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(AppColors.Blue50)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = tool.category.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.Primary
            )
        }
        
        // 工具信息
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = tool.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            Text(
                text = tool.description,
                fontSize = 13.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}
