package com.okko.kmpact.ui.components.devtools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.okko.kmpact.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.datetime.*
import kotlinx.datetime.format.*
import kotlin.time.Duration.Companion.seconds

/**
 * 时间戳转换工具组件
 */
@Composable
fun TimestampConverterComponent(
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var timestampInput by remember { mutableStateOf("") }
    var datetimeInput by remember { mutableStateOf("") }
    var selectedTimezone by remember { mutableStateOf(TimeZone.currentSystemDefault()) }
    var timestampUnit by remember { mutableStateOf(TimestampUnit.SECONDS) }
    var currentTimestamp by remember { mutableLongStateOf(System.currentTimeMillis() / 1000) }
    var currentTimestampUnit by remember { mutableStateOf(TimestampUnit.SECONDS) }
    var showTimezoneDropdown by remember { mutableStateOf(false) }
    var showCurrentTimestampUnitDropdown by remember { mutableStateOf(false) }
    
    // 实时更新当前时间戳
    LaunchedEffect(currentTimestampUnit) {
        while (true) {
            currentTimestamp = when (currentTimestampUnit) {
                TimestampUnit.SECONDS -> System.currentTimeMillis() / 1000
                TimestampUnit.MILLISECONDS -> System.currentTimeMillis()
            }
            delay(1.seconds)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Gray50)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 标题区域
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(32.dp)
            )
            
            Column {
                Text(
                    text = "时间戳转换",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "时间戳与日期时间互转",
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
        
        // 当前时间戳显示
        CurrentTimestampCard(
            currentTimestamp = currentTimestamp, 
            unit = currentTimestampUnit,
            onUnitChange = { currentTimestampUnit = it },
            showDropdown = showCurrentTimestampUnitDropdown,
            onShowDropdownChange = { showCurrentTimestampUnitDropdown = it },
            clipboardManager = clipboardManager
        )
        
        // 时区选择
        TimezoneSelector(
            selectedTimezone = selectedTimezone,
            onTimezoneChange = { selectedTimezone = it },
            showDropdown = showTimezoneDropdown,
            onShowDropdownChange = { showTimezoneDropdown = it }
        )
        
        // 时间戳转日期时间
        TimestampToDatetimeCard(
            timestampInput = timestampInput,
            onTimestampInputChange = { timestampInput = it },
            unit = timestampUnit,
            onUnitChange = { timestampUnit = it },
            timezone = selectedTimezone,
            clipboardManager = clipboardManager
        )
        
        // 日期时间转时间戳
        DatetimeToTimestampCard(
            datetimeInput = datetimeInput,
            onDatetimeInputChange = { datetimeInput = it },
            timezone = selectedTimezone,
            clipboardManager = clipboardManager
        )
    }
}

/**
 * 时间戳单位枚举
 */
enum class TimestampUnit(val displayName: String, val description: String) {
    SECONDS("秒", "Unix时间戳（秒）"),
    MILLISECONDS("毫秒", "Unix时间戳（毫秒）")
}

/**
 * 当前时间戳卡片
 */
@Composable
private fun CurrentTimestampCard(
    currentTimestamp: Long,
    unit: TimestampUnit,
    onUnitChange: (TimestampUnit) -> Unit,
    showDropdown: Boolean,
    onShowDropdownChange: (Boolean) -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
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
                    text = "当前时间戳",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                
                // 单位选择器
                Box {
                    Surface(
                        onClick = { onShowDropdownChange(!showDropdown) },
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.Blue50,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = unit.displayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Primary
                            )
                            Icon(
                                imageVector = if (showDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { onShowDropdownChange(false) }
                    ) {
                        TimestampUnit.entries.forEach { unitOption ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = unitOption.displayName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = unitOption.description,
                                            fontSize = 12.sp,
                                            color = AppColors.TextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    onUnitChange(unitOption)
                                    onShowDropdownChange(false)
                                }
                            )
                        }
                    }
                }
            }
            
            // 时间戳显示
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = AppColors.Gray100
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentTimestamp.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(currentTimestamp.toString()))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "复制",
                            tint = AppColors.Primary
                        )
                    }
                }
            }
            
            // 对应的日期时间
            val datetime = try {
                val instant = when (unit) {
                    TimestampUnit.SECONDS -> Instant.fromEpochSeconds(currentTimestamp)
                    TimestampUnit.MILLISECONDS -> Instant.fromEpochMilliseconds(currentTimestamp)
                }
                instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    .format(LocalDateTime.Format {
                        year(); char('-'); monthNumber(); char('-'); dayOfMonth()
                        char(' ')
                        hour(); char(':'); minute(); char(':'); second()
                    })
            } catch (e: Exception) {
                "无效时间戳"
            }
            
            Text(
                text = "对应时间：$datetime",
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}

/**
 * 时区选择器
 */
@Composable
private fun TimezoneSelector(
    selectedTimezone: TimeZone,
    onTimezoneChange: (TimeZone) -> Unit,
    showDropdown: Boolean,
    onShowDropdownChange: (Boolean) -> Unit
) {
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
                text = "时区设置",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            
            Box {
                Surface(
                    onClick = { onShowDropdownChange(!showDropdown) },
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.Gray100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedTimezone.id,
                            fontSize = 16.sp,
                            color = AppColors.TextPrimary
                        )
                        Icon(
                            imageVector = if (showDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = AppColors.TextSecondary
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { onShowDropdownChange(false) },
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    val commonTimezones = listOf(
                        TimeZone.UTC,
                        TimeZone.of("Asia/Shanghai"),
                        TimeZone.of("Asia/Tokyo"),
                        TimeZone.of("Europe/London"),
                        TimeZone.of("America/New_York"),
                        TimeZone.of("America/Los_Angeles"),
                        TimeZone.currentSystemDefault()
                    ).distinctBy { it.id }
                    
                    commonTimezones.forEach { timezone ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = timezone.id,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                onTimezoneChange(timezone)
                                onShowDropdownChange(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 时间戳转日期时间卡片
 */
@Composable
private fun TimestampToDatetimeCard(
    timestampInput: String,
    onTimestampInputChange: (String) -> Unit,
    unit: TimestampUnit,
    onUnitChange: (TimestampUnit) -> Unit,
    timezone: TimeZone,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    var showUnitDropdown by remember { mutableStateOf(false) }
    
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
                    text = "时间戳 → 日期时间",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                
                // 单位选择器
                Box {
                    Surface(
                        onClick = { showUnitDropdown = !showUnitDropdown },
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.Blue50,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = unit.displayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Primary
                            )
                            Icon(
                                imageVector = if (showUnitDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showUnitDropdown,
                        onDismissRequest = { showUnitDropdown = false }
                    ) {
                        TimestampUnit.entries.forEach { unitOption ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = unitOption.displayName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = unitOption.description,
                                            fontSize = 12.sp,
                                            color = AppColors.TextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    onUnitChange(unitOption)
                                    showUnitDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            // 时间戳输入
            OutlinedTextField(
                value = timestampInput,
                onValueChange = onTimestampInputChange,
                label = { Text("输入时间戳") },
                placeholder = { Text("例如：1640995200") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.BorderLight
                ),
                singleLine = true
            )
            
            // 转换结果
            val convertedDatetime = remember(timestampInput, unit, timezone) {
                if (timestampInput.isBlank()) {
                    ""
                } else {
                    try {
                        val timestamp = timestampInput.toLong()
                        val instant = when (unit) {
                            TimestampUnit.SECONDS -> Instant.fromEpochSeconds(timestamp)
                            TimestampUnit.MILLISECONDS -> Instant.fromEpochMilliseconds(timestamp)
                        }
                        instant.toLocalDateTime(timezone)
                            .format(LocalDateTime.Format {
                                year(); char('-'); monthNumber(); char('-'); dayOfMonth()
                                char(' ')
                                hour(); char(':'); minute(); char(':'); second()
                            })
                    } catch (e: Exception) {
                        "无效的时间戳格式"
                    }
                }
            }
            
            if (convertedDatetime.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.Green50
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "转换结果",
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary
                            )
                            Text(
                                text = convertedDatetime,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextPrimary
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(convertedDatetime))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "复制",
                                tint = AppColors.Primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 日期时间转时间戳卡片
 */
@Composable
private fun DatetimeToTimestampCard(
    datetimeInput: String,
    onDatetimeInputChange: (String) -> Unit,
    timezone: TimeZone,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
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
                text = "日期时间 → 时间戳",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            
            // 日期时间输入
            OutlinedTextField(
                value = datetimeInput,
                onValueChange = onDatetimeInputChange,
                label = { Text("输入日期时间") },
                placeholder = { Text("格式：2024-01-01 12:00:00") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.BorderLight
                ),
                singleLine = true
            )
            
            // 转换结果
            val convertedTimestamps = remember(datetimeInput, timezone) {
                if (datetimeInput.isBlank()) {
                    null
                } else {
                    try {
                        val localDateTime = LocalDateTime.parse(datetimeInput.replace(" ", "T"))
                        val instant = localDateTime.toInstant(timezone)
                        Pair(
                            instant.epochSeconds,
                            instant.toEpochMilliseconds()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            
            if (convertedTimestamps != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 秒时间戳
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.Green50
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "时间戳（秒）",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                                Text(
                                    text = convertedTimestamps.first.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextPrimary
                                )
                            }
                            
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(convertedTimestamps.first.toString()))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    tint = AppColors.Primary
                                )
                            }
                        }
                    }
                    
                    // 毫秒时间戳
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.Green50
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "时间戳（毫秒）",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                                Text(
                                    text = convertedTimestamps.second.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextPrimary
                                )
                            }
                            
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(convertedTimestamps.second.toString()))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    tint = AppColors.Primary
                                )
                            }
                        }
                    }
                }
            } else if (datetimeInput.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.Red50
                ) {
                    Text(
                        text = "无效的日期时间格式，请使用：YYYY-MM-DD HH:mm:ss",
                        fontSize = 14.sp,
                        color = AppColors.Error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}