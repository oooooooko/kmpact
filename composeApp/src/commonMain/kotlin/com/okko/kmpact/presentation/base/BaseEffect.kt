package com.okko.kmpact.presentation.base

/**
 * MVI架构 - SideEffect基类
 * 
 * 作用：
 * - 统一一次性事件类型
 * - 规范UI副作用表达
 * 
 * 设计原则：
 * - 只消费一次
 * - 与UiState严格区分
 * - 不参与状态回放
 * 
 * 用途：
 * 表达不可重放的UI行为（如Toast、Dialog、Navigation）
 */
interface BaseEffect
