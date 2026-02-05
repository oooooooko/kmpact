package com.okko.kmpact.presentation.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * MVI架构 - ViewModel基类
 * 
 * 作用：
 * - 抽象通用MVI行为
 * - 统一状态、事件、副作用管理方式
 * 
 * 职责：
 * - 接收并分发Intent
 * - 调用Domain层执行业务逻辑
 * - 管理加载态、异常态
 * - 协调Reducer更新UiState
 * - 发送SideEffect
 * 
 * 约束：
 * - 不直接操作UI
 * - 不依赖平台特有能力
 * - 不直接访问DataSource
 */
abstract class BaseViewModel<State : BaseUiState, Intent : BaseIntent, Effect : BaseEffect>(
    initialState: State
) : ViewModel() {
    
    /**
     * UI状态流（只读）
     */
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()
    
    /**
     * 副作用通道（一次性事件）
     */
    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    
    /**
     * 当前状态
     */
    protected val currentState: State
        get() = _uiState.value
    
    /**
     * 处理Intent（子类实现）
     */
    abstract fun handleIntent(intent: Intent)
    
    /**
     * 更新状态
     */
    protected fun updateState(reducer: State.() -> State) {
        _uiState.value = currentState.reducer()
    }
    
    /**
     * 发送副作用
     */
    protected suspend fun sendEffect(effect: Effect) {
        _effect.send(effect)
    }
}
