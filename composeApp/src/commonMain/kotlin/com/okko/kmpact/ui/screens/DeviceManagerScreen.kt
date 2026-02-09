package com.okko.kmpact.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.platform.DeviceInfo
import com.okko.kmpact.platform.DeviceManager
import com.okko.kmpact.ui.theme.AppColors
import kotlinx.coroutines.launch

/**
 * 设备管理界面
 */
@Composable
fun DeviceManagerScreen() {
    var devices by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    var selectedDevice by remember { mutableStateOf<DeviceInfo?>(null) }
    var deviceDetails by remember { mutableStateOf<DeviceInfo?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingDetails by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val deviceManager = remember { DeviceManager() }
    
    // 自动加载设备列表
    LaunchedEffect(Unit) {
        isLoading = true
        devices = deviceManager.getConnectedDevices()
        if (devices.isNotEmpty()) {
            selectedDevice = devices.first()
        }
        isLoading = false
    }
    
    // 加载选中设备的详细信息
    LaunchedEffect(selectedDevice) {
        selectedDevice?.let { device ->
            isLoadingDetails = true
            deviceDetails = deviceManager.getDeviceDetails(device.serialNumber)
            isLoadingDetails = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Gray50)
    ) {
        // 标题和刷新按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "设备管理",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "管理已连接的 Android 设备",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
            
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        devices = deviceManager.getConnectedDevices()
                        isLoading = false
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("刷新设备")
            }
        }
        
        // 可滚动内容区域
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentPadding = PaddingValues(
                top = 50.dp,
                bottom = 48.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 设备列表
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "已连接的设备",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            
                            Text(
                                text = "${devices.size} 台设备",
                                fontSize = 14.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        HorizontalDivider(color = AppColors.BorderLight)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AppColors.Primary)
                            }
                        } else if (devices.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = AppColors.TextTertiary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text(
                                        text = "未检测到设备",
                                        fontSize = 16.sp,
                                        color = AppColors.TextSecondary
                                    )
                                    Text(
                                        text = "请确保设备已连接并开启 USB 调试",
                                        fontSize = 13.sp,
                                        color = AppColors.TextTertiary
                                    )
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                devices.forEach { device ->
                                    DeviceItem(
                                        device = device,
                                        isSelected = device.serialNumber == selectedDevice?.serialNumber,
                                        onClick = { selectedDevice = device }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // 选中设备的详细信息
            if (selectedDevice != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "设备详情",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            
                            HorizontalDivider(color = AppColors.BorderLight)
                            
                            if (isLoadingDetails) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AppColors.Primary)
                                }
                            } else if (deviceDetails != null) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // 基本信息
                                    Text(
                                        text = "基本信息",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.Primary
                                    )
                                    deviceDetails!!.name?.let { DeviceDetailRow("名称", it) }
                                    deviceDetails!!.brand?.let { DeviceDetailRow("品牌", it) }
                                    DeviceDetailRow("型号", deviceDetails!!.model)
                                    DeviceDetailRow("序列号", deviceDetails!!.serialNumber)
                                    deviceDetails!!.androidVersion?.let { DeviceDetailRow("Android版本", it) }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // 硬件信息
                                    Text(
                                        text = "硬件信息",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.Primary
                                    )
                                    deviceDetails!!.processor?.let { DeviceDetailRow("处理器", it) }
                                    deviceDetails!!.memory?.let { DeviceDetailRow("内存", it) }
                                    deviceDetails!!.storageUsage?.let { DeviceDetailRow("存储使用", it) }
                                    deviceDetails!!.kernelVersion?.let { DeviceDetailRow("内核版本", it) }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // 显示信息
                                    Text(
                                        text = "显示信息",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.Primary
                                    )
                                    deviceDetails!!.physicalResolution?.let { DeviceDetailRow("物理分辨率", it) }
                                    deviceDetails!!.resolution?.let { DeviceDetailRow("当前分辨率", it) }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // 网络信息
                                    Text(
                                        text = "网络信息",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.Primary
                                    )
                                    deviceDetails!!.wifi?.let { DeviceDetailRow("WiFi", it) }
                                    deviceDetails!!.ipAddress?.let { DeviceDetailRow("IP地址", it) }
                                }
                            } else {
                                DeviceDetailRow("序列号", selectedDevice!!.serialNumber)
                                DeviceDetailRow("型号", selectedDevice!!.model)
                                DeviceDetailRow("状态", selectedDevice!!.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 设备项
 */
@Composable
private fun DeviceItem(
    device: DeviceInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) AppColors.Blue50 else Color.Transparent
    val borderColor = if (isSelected) AppColors.Primary else AppColors.BorderLight
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = if (isSelected) AppColors.Primary else AppColors.TextSecondary,
                modifier = Modifier.size(32.dp)
            )
            
            Column {
                Text(
                    text = device.model,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) AppColors.Primary else AppColors.TextPrimary
                )
                Text(
                    text = device.serialNumber,
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 设备详情行
 */
@Composable
private fun DeviceDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = AppColors.TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextPrimary
        )
    }
}
