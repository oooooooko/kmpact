package com.okko.kmpact.presentation.packagetools

import androidx.lifecycle.viewModelScope
import com.okko.kmpact.domain.model.ToolCommands
import com.okko.kmpact.domain.usecase.ExecuteCommandUseCaseImpl
import com.okko.kmpact.presentation.base.BaseViewModel
import com.okko.kmpact.ui.components.LogEntry
import com.okko.kmpact.ui.components.LogLevel
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
/**
 * Package Tools ViewModel
 * 
 * 职责：
 * - 接收并分发Intent
 * - 调用Domain层执行业务逻辑（待实现）
 * - 管理加载态、异常态
 * - 更新UiState
 * - 发送SideEffect
 */
class PackageToolsViewModel : BaseViewModel<PackageToolsUiState, PackageToolsIntent, PackageToolsEffect>(
    initialState = PackageToolsUiState()
) {
    
    // UseCase实例
    private val executeCommandUseCase = ExecuteCommandUseCaseImpl()
    
    init {
        // 初始化时加载保存的配置
        loadSavedProfiles()
        addLog(LogLevel.INFO, "Package Tools 已就绪")
    }
    
    /**
     * 处理用户意图
     */
    override fun handleIntent(intent: PackageToolsIntent) {
        when (intent) {
            // APK签名相关
            is PackageToolsIntent.SelectTargetArtifact -> handleSelectTargetArtifact(intent.filePath)
            is PackageToolsIntent.InputKeystorePassword -> handleInputKeystorePassword(intent.password)
            is PackageToolsIntent.SelectKeystoreFile -> handleSelectKeystoreFile(intent.filePath)
            is PackageToolsIntent.InputKeyAlias -> handleInputKeyAlias(intent.alias)
            is PackageToolsIntent.InputOutputDirectory -> handleInputOutputDirectory(intent.directory)
            is PackageToolsIntent.ResetSigningForm -> handleResetSigningForm()
            is PackageToolsIntent.StartSignAndOptimize -> handleStartSignAndOptimize()
            
            // 包对比相关
            is PackageToolsIntent.SelectOriginalArtifact -> handleSelectOriginalArtifact(intent.filePath)
            is PackageToolsIntent.SelectTargetArtifactForComparison -> handleSelectTargetArtifactForComparison(intent.filePath)
            is PackageToolsIntent.GenerateDetailedDiffReport -> handleGenerateDetailedDiffReport()
            
            // 保存的配置相关
            is PackageToolsIntent.SelectSavedProfile -> handleSelectSavedProfile(intent.profileName)
            
            // 日志相关
            is PackageToolsIntent.ClearLogs -> handleClearLogs()
        }
    }
    
    // ========== APK签名处理方法 ==========
    
    private fun handleSelectTargetArtifact(filePath: String) {
        updateState { copy(targetArtifactPath = filePath) }
        addLog(LogLevel.INFO, "已选择目标文件: $filePath")
    }
    
    private fun handleInputKeystorePassword(password: String) {
        updateState { copy(keystorePassword = password) }
    }
    
    private fun handleSelectKeystoreFile(filePath: String) {
        updateState { copy(keystoreFilePath = filePath) }
        addLog(LogLevel.INFO, "已选择密钥库文件: $filePath")
    }
    
    private fun handleInputKeyAlias(alias: String) {
        updateState { copy(keyAlias = alias) }
    }
    
    private fun handleInputOutputDirectory(directory: String) {
        updateState { copy(outputDirectory = directory) }
        addLog(LogLevel.INFO, "输出目录: $directory")
    }
    
    private fun handleResetSigningForm() {
        updateState {
            copy(
                targetArtifactPath = "",
                keystorePassword = "",
                keystoreFilePath = "",
                keyAlias = "",
                outputDirectory = "",
                signingProgress = 0
            )
        }
        addLog(LogLevel.INFO, "已重置签名表单")
    }
    
    private fun handleStartSignAndOptimize() {
        viewModelScope.launch {
            addLog(LogLevel.COMMAND, "执行命令: 对APK进行签名")
            updateState { copy(isSigning = true, signingProgress = 0) }
            
            // 验证输入
            if (currentState.targetArtifactPath.isEmpty()) {
                addLog(LogLevel.ERROR, "请选择目标APK文件")
                updateState { copy(isSigning = false) }
                sendEffect(PackageToolsEffect.ShowToast("请选择目标APK文件"))
                return@launch
            }
            
            if (currentState.keystoreFilePath.isEmpty()) {
                addLog(LogLevel.ERROR, "请选择密钥库文件")
                updateState { copy(isSigning = false) }
                sendEffect(PackageToolsEffect.ShowToast("请选择密钥库文件"))
                return@launch
            }
            
            // 执行签名命令
            val command = ToolCommands.SIGN_APK
            val parameters = mapOf(
                "apkPath" to currentState.targetArtifactPath,
                "keystorePath" to currentState.keystoreFilePath,
                "storePassword" to currentState.keystorePassword,
                "keyAlias" to currentState.keyAlias,
                "outputDir" to currentState.outputDirectory
            )
            
            val result = executeCommandUseCase.execute(
                command = command,
                parameters = parameters,
                onOutput = { output ->
                    addLog(LogLevel.OUTPUT, output)
                }
            )
            
            result.fold(
                onSuccess = { commandResult ->
                    if (commandResult.success) {
                        addLog(LogLevel.SUCCESS, "APK签名成功")
                        sendEffect(PackageToolsEffect.ShowSuccessDialog("APK签名成功"))
                    } else {
                        addLog(LogLevel.ERROR, "APK签名失败: ${commandResult.error}")
                        sendEffect(PackageToolsEffect.ShowErrorDialog("签名失败", commandResult.error ?: "未知错误"))
                    }
                },
                onFailure = { error ->
                    addLog(LogLevel.ERROR, "执行失败: ${error.message}")
                    sendEffect(PackageToolsEffect.ShowErrorDialog("执行失败", error.message ?: "未知错误"))
                }
            )
            
            updateState { copy(isSigning = false, signingProgress = 100) }
        }
    }
    
    // ========== 包对比处理方法 ==========
    
    private fun handleSelectOriginalArtifact(filePath: String) {
        updateState { 
            copy(
                originalArtifactPath = filePath,
                // TODO: 解析文件列表
                originalArtifactFiles = listOf(
                    ArtifactFile("AndroidManifest.xml", "4.2 KB"),
                    ArtifactFile("classes.dex", "1.2 MB"),
                    ArtifactFile("classes2.dex", "3.4 MB")
                )
            )
        }
        addLog(LogLevel.INFO, "已选择原始制品: $filePath")
    }
    
    private fun handleSelectTargetArtifactForComparison(filePath: String) {
        updateState { 
            copy(
                targetArtifactPathForComparison = filePath,
                // TODO: 解析文件列表
                targetArtifactFiles = listOf(
                    ArtifactFile("AndroidManifest.xml", "4.8 KB", isModified = true),
                    ArtifactFile("classes.dex", "2.2 MB"),
                    ArtifactFile("classes2.dex", "3.2 MB")
                )
            )
        }
        addLog(LogLevel.INFO, "已选择目标制品: $filePath")
    }
    
    private fun handleGenerateDetailedDiffReport() {
        viewModelScope.launch {
            addLog(LogLevel.COMMAND, "执行命令: 生成包体差异报告")
            updateState { copy(isGeneratingReport = true) }
            
            // 验证输入
            if (currentState.originalArtifactPath.isEmpty() || currentState.targetArtifactPathForComparison.isEmpty()) {
                addLog(LogLevel.ERROR, "请选择要对比的两个包体文件")
                updateState { copy(isGeneratingReport = false) }
                sendEffect(PackageToolsEffect.ShowToast("请选择要对比的两个包体文件"))
                return@launch
            }
            
            // 执行对比命令
            val command = ToolCommands.COMPARE_PACKAGE
            val parameters = mapOf(
                "originalPath" to currentState.originalArtifactPath,
                "targetPath" to currentState.targetArtifactPathForComparison
            )
            
            val result = executeCommandUseCase.execute(
                command = command,
                parameters = parameters,
                onOutput = { output ->
                    addLog(LogLevel.OUTPUT, output)
                }
            )
            
            result.fold(
                onSuccess = { commandResult ->
                    if (commandResult.success) {
                        addLog(LogLevel.SUCCESS, "差异报告生成成功")
                        sendEffect(PackageToolsEffect.ShowSuccessDialog("差异报告生成成功"))
                    } else {
                        addLog(LogLevel.ERROR, "报告生成失败: ${commandResult.error}")
                        sendEffect(PackageToolsEffect.ShowErrorDialog("生成失败", commandResult.error ?: "未知错误"))
                    }
                },
                onFailure = { error ->
                    addLog(LogLevel.ERROR, "执行失败: ${error.message}")
                    sendEffect(PackageToolsEffect.ShowErrorDialog("执行失败", error.message ?: "未知错误"))
                }
            )
            
            updateState { copy(isGeneratingReport = false) }
        }
    }
    
    // ========== 保存的配置处理方法 ==========
    
    private fun loadSavedProfiles() {
        // TODO: 从本地加载保存的配置
        updateState {
            copy(
                savedProfiles = listOf(
                    SigningProfile("Release Signing v2", "")
                )
            )
        }
    }
    
    private fun handleSelectSavedProfile(profileName: String) {
        val profile = currentState.savedProfiles.find { it.name == profileName }
        updateState { copy(selectedProfile = profile) }
        addLog(LogLevel.INFO, "已选择配置: $profileName")
        
        // TODO: 加载配置详情
    }
    
    // ========== 日志处理方法 ==========
    
    private fun handleClearLogs() {
        updateState { copy(logs = emptyList()) }
    }
    
    /**
     * 添加日志
     */
    private fun addLog(level: LogLevel, message: String) {
        val timestamp = getCurrentTimestamp()
        val logEntry = LogEntry(
            timestamp = timestamp,
            level = level,
            message = message
        )
        updateState { 
            copy(logs = logs + logEntry) 
        }
    }
    
    /**
     * 获取当前时间戳
     */
    private fun getCurrentTimestamp(): String {
        val localDateTime = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())

        return localDateTime.hour.toString().padStart(2, '0') +
                ":${localDateTime.minute.toString().padStart(2, '0')}" +
                ":${localDateTime.second.toString().padStart(2, '0')}"
    }

}

