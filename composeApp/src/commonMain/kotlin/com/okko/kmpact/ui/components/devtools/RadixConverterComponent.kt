package com.okko.kmpact.ui.components.devtools

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.okko.kmpact.ui.components.devtools.radix.RadixConverterUI

/**
 * 进制转换工具组件
 */
@Composable
fun RadixConverterComponent(
    modifier: Modifier = Modifier
) {
    RadixConverterUI(modifier = modifier)
}
