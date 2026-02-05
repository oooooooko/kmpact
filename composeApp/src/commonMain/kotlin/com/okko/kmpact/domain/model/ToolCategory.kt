package com.okko.kmpact.domain.model

/**
 * 工具分类
 */
enum class ToolCategory(val displayName: String, val description: String) {
    /**
     * 包体工具
     */
    PACKAGE_TOOLS("包体工具", "APK签名、包对比等"),
    
    /**
     * 设备工具
     */
    DEVICE_TOOLS("设备工具", "设备管理、应用安装等"),
    
    /**
     * 逆向工具
     */
    REVERSE_TOOLS("逆向工具", "反编译、格式转换等"),
    
    /**
     * 密钥工具
     */
    KEY_TOOLS("密钥工具", "SSH密钥管理")
}

/**
 * 工具命令定义
 */
data class ToolCommand(
    /**
     * 命令ID
     */
    val id: String,
    
    /**
     * 命令名称
     */
    val name: String,
    
    /**
     * 命令描述
     */
    val description: String,
    
    /**
     * 所属分类
     */
    val category: ToolCategory,
    
    /**
     * 脚本路径（相对于AndroidCmdTools目录）
     */
    val scriptPath: String,
    
    /**
     * 是否需要选择设备
     */
    val requiresDevice: Boolean = false,
    
    /**
     * 是否需要输入参数
     */
    val requiresInput: Boolean = false
)

/**
 * 预定义的工具命令列表
 */
object ToolCommands {
    
    // ========== 包体工具 ==========
    
    val SIGN_APK = ToolCommand(
        id = "sign_apk",
        name = "对APK进行签名",
        description = "使用keystore为APK签名",
        category = ToolCategory.PACKAGE_TOOLS,
        scriptPath = "shell/package-tools/SignatureApk.sh",
        requiresInput = true
    )
    
    val GET_APK_SIGNATURE = ToolCommand(
        id = "get_apk_signature",
        name = "获取APK签名信息",
        description = "查看APK的签名详情",
        category = ToolCategory.PACKAGE_TOOLS,
        scriptPath = "shell/package-tools/GetApkSignature.sh",
        requiresInput = true
    )
    
    val SUPPORT_TO_ANDROIDX = ToolCommand(
        id = "support_to_androidx",
        name = "Support转AndroidX",
        description = "将Support库转换为AndroidX",
        category = ToolCategory.PACKAGE_TOOLS,
        scriptPath = "shell/package-tools/SupportToAndroidX.sh",
        requiresInput = true
    )
    
    val ANDROIDX_TO_SUPPORT = ToolCommand(
        id = "androidx_to_support",
        name = "AndroidX转Support",
        description = "将AndroidX转换为Support库",
        category = ToolCategory.PACKAGE_TOOLS,
        scriptPath = "shell/package-tools/AndroidXToSupport.sh",
        requiresInput = true
    )
    
    val COMPARE_PACKAGE = ToolCommand(
        id = "compare_package",
        name = "包体比较",
        description = "比较APK/AAR/JAR/AAB包体差异",
        category = ToolCategory.PACKAGE_TOOLS,
        scriptPath = "shell/package-tools/CompareArchives.sh",
        requiresInput = true
    )
    
    // ========== 设备工具 ==========
    
    val INSTALL_APK = ToolCommand(
        id = "install_apk",
        name = "安装应用",
        description = "安装APK到设备",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/InstallApk.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val UNINSTALL_APP = ToolCommand(
        id = "uninstall_app",
        name = "卸载应用",
        description = "从设备卸载应用",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/UninstallApp.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val SCREENSHOT = ToolCommand(
        id = "screenshot",
        name = "保存截图到电脑",
        description = "截取设备屏幕并保存",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/SaveScreenshot.sh",
        requiresDevice = true
    )
    
    val SCREEN_RECORD = ToolCommand(
        id = "screen_record",
        name = "保存录屏到电脑",
        description = "录制设备屏幕并保存",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/SaveScreenRecording.sh",
        requiresDevice = true
    )
    
    val CLEAR_APP_DATA = ToolCommand(
        id = "clear_app_data",
        name = "清除应用数据",
        description = "清除指定应用的数据",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/ClearAppData.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val KILL_APP_PROCESS = ToolCommand(
        id = "kill_app_process",
        name = "杀死应用进程",
        description = "强制停止应用进程",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/KillAppProcess.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val VIEW_LOGCAT = ToolCommand(
        id = "view_logcat",
        name = "查看设备Logcat",
        description = "实时查看设备日志",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/DisplayLogcat.sh",
        requiresDevice = true
    )
    
    val EXPORT_APK = ToolCommand(
        id = "export_apk",
        name = "导出应用APK",
        description = "从设备导出已安装的APK",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/ExportApkFile.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val DEVICE_REBOOT = ToolCommand(
        id = "device_reboot",
        name = "设备重启",
        description = "重启Android设备",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/hardware/DeviceRestart.sh",
        requiresDevice = true
    )
    
    val DEVICE_SHUTDOWN = ToolCommand(
        id = "device_shutdown",
        name = "设备关机",
        description = "关闭Android设备",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/hardware/PowerOffDevice.sh",
        requiresDevice = true
    )
    
    val CONNECT_WIRELESS_ADB = ToolCommand(
        id = "connect_wireless_adb",
        name = "连接无线ADB",
        description = "通过WiFi连接设备",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/ConnectWirelessAdb.sh",
        requiresDevice = true
    )
    
    val DISCONNECT_WIRELESS_ADB = ToolCommand(
        id = "disconnect_wireless_adb",
        name = "断开无线ADB",
        description = "断开WiFi连接的设备",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/DisconnectWirelessAdb.sh"
    )
    
    val GRANT_PERMISSION = ToolCommand(
        id = "grant_permission",
        name = "授予应用权限",
        description = "授予应用指定权限",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/GrantPermission.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val REVOKE_PERMISSION = ToolCommand(
        id = "revoke_permission",
        name = "撤销应用权限",
        description = "撤销应用指定权限",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/RevokePermission.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val ENABLE_APP = ToolCommand(
        id = "enable_app",
        name = "启用应用",
        description = "启用被禁用的应用",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/EnabledApp.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val DISABLE_APP = ToolCommand(
        id = "disable_app",
        name = "禁用应用",
        description = "禁用指定应用",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/DisabledApp.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val SET_GLOBAL_PROXY = ToolCommand(
        id = "set_global_proxy",
        name = "设置全局代理",
        description = "设置设备全局HTTP代理",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/SetGlobalProxy.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val CLEAR_GLOBAL_PROXY = ToolCommand(
        id = "clear_global_proxy",
        name = "清除全局代理",
        description = "清除设备全局代理设置",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/ClearGlobalProxy.sh",
        requiresDevice = true
    )
    
    val GET_SCREEN_INFO = ToolCommand(
        id = "get_screen_info",
        name = "获取屏幕信息",
        description = "查看设备屏幕分辨率等信息",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/GetScreenInfo.sh",
        requiresDevice = true
    )
    
    val GET_TOP_ACTIVITY = ToolCommand(
        id = "get_top_activity",
        name = "获取顶层Activity",
        description = "查看当前顶层Activity信息",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/GetTopActivityContent.sh",
        requiresDevice = true
    )
    
    val EXPORT_ANR = ToolCommand(
        id = "export_anr",
        name = "导出ANR日志",
        description = "导出应用ANR崩溃日志",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/ExportAnrFile.sh",
        requiresDevice = true
    )
    
    val MANAGE_FILE = ToolCommand(
        id = "manage_file",
        name = "管理设备文件",
        description = "浏览和管理设备文件系统",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/ManageFile.sh",
        requiresDevice = true
    )
    
    val RUN_MONKEY_TEST = ToolCommand(
        id = "run_monkey_test",
        name = "运行Monkey测试",
        description = "执行随机压力测试",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/RunMonkeyTest.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    // ========== 模拟操作 ==========
    
    val CLICK_SCREEN = ToolCommand(
        id = "click_screen",
        name = "点击屏幕",
        description = "模拟点击屏幕指定坐标",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/simulation/ClickTheScreen.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val INPUT_TEXT = ToolCommand(
        id = "input_text",
        name = "输入文本",
        description = "模拟输入文本内容",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/simulation/InputText.sh",
        requiresDevice = true,
        requiresInput = true
    )
    
    val PRESS_BACK = ToolCommand(
        id = "press_back",
        name = "按返回键",
        description = "模拟按下返回键",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/simulation/PressBackKey.sh",
        requiresDevice = true
    )
    
    val PRESS_HOME = ToolCommand(
        id = "press_home",
        name = "按Home键",
        description = "模拟按下Home键",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/simulation/PressHomeKey.sh",
        requiresDevice = true
    )
    
    val PRESS_MENU = ToolCommand(
        id = "press_menu",
        name = "按菜单键",
        description = "模拟按下菜单键",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/simulation/PressMenuKey.sh",
        requiresDevice = true
    )
    
    val PRESS_POWER = ToolCommand(
        id = "press_power",
        name = "按电源键",
        description = "模拟按下电源键",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/simulation/PressPowerKey.sh",
        requiresDevice = true
    )
    
    val PRESS_TASK = ToolCommand(
        id = "press_task",
        name = "按任务键",
        description = "模拟按下任务切换键",
        category = ToolCategory.DEVICE_TOOLS,
        scriptPath = "shell/device-tools/simulation/PressTaskKey.sh",
        requiresDevice = true
    )
    
    // ========== 逆向工具 ==========
    
    val APKTOOL_DECOMPILE = ToolCommand(
        id = "apktool_decompile",
        name = "用apktool反编译APK",
        description = "反编译APK到smali代码",
        category = ToolCategory.REVERSE_TOOLS,
        scriptPath = "shell/reverse-tools/apktool/DecompileApk.sh",
        requiresInput = true
    )
    
    val APKTOOL_RECOMPILE = ToolCommand(
        id = "apktool_recompile",
        name = "用apktool回编译APK",
        description = "从smali代码回编译APK",
        category = ToolCategory.REVERSE_TOOLS,
        scriptPath = "shell/reverse-tools/apktool/RecompileApk.sh",
        requiresInput = true
    )
    
    val JADX_VIEW = ToolCommand(
        id = "jadx_view",
        name = "用jadx查看包体",
        description = "使用jadx查看APK源码",
        category = ToolCategory.REVERSE_TOOLS,
        scriptPath = "shell/reverse-tools/jadx/JadxView.sh",
        requiresInput = true
    )
    
    val JD_GUI_VIEW = ToolCommand(
        id = "jd_gui_view",
        name = "用jd-gui查看包体",
        description = "使用jd-gui查看JAR源码",
        category = ToolCategory.REVERSE_TOOLS,
        scriptPath = "shell/reverse-tools/jd-gui/JdGuiView.sh",
        requiresInput = true
    )
    
    val DEX_TO_JAR = ToolCommand(
        id = "dex_to_jar",
        name = "dex转jar",
        description = "将dex文件转换为jar",
        category = ToolCategory.REVERSE_TOOLS,
        scriptPath = "shell/reverse-tools/convert/jar-dex/DexToJar.sh",
        requiresInput = true
    )
    
    val JAR_TO_DEX = ToolCommand(
        id = "jar_to_dex",
        name = "jar转dex",
        description = "将jar文件转换为dex",
        category = ToolCategory.REVERSE_TOOLS,
        scriptPath = "shell/reverse-tools/convert/jar-dex/JarToDex.sh",
        requiresInput = true
    )
    
    val DEX_TO_SMALI = ToolCommand(
        id = "dex_to_smali",
        name = "dex转smali",
        description = "将dex文件转换为smali代码",
        category = ToolCategory.REVERSE_TOOLS,
        scriptPath = "shell/reverse-tools/convert/dex-smali/DexToSmali.sh",
        requiresInput = true
    )
    
    val SMALI_TO_DEX = ToolCommand(
        id = "smali_to_dex",
        name = "smali转dex",
        description = "将smali代码转换为dex",
        category = ToolCategory.REVERSE_TOOLS,
        scriptPath = "shell/reverse-tools/convert/dex-smali/SmaliToDex.sh",
        requiresInput = true
    )
    
    val DEX_TO_CLASS = ToolCommand(
        id = "dex_to_class",
        name = "dex转class",
        description = "将dex文件转换为class文件",
        category = ToolCategory.REVERSE_TOOLS,
        scriptPath = "shell/reverse-tools/convert/dex-class/DexToClass.sh",
        requiresInput = true
    )
    
    val CLASS_TO_DEX = ToolCommand(
        id = "class_to_dex",
        name = "class转dex",
        description = "将class文件转换为dex",
        category = ToolCategory.REVERSE_TOOLS,
        scriptPath = "shell/reverse-tools/convert/dex-class/ClassToDex.sh",
        requiresInput = true
    )
    
    // ========== 密钥工具 ==========
    
    val CREATE_SSH_KEY = ToolCommand(
        id = "create_ssh_key",
        name = "创建SSH密钥",
        description = "生成新的SSH密钥对",
        category = ToolCategory.KEY_TOOLS,
        scriptPath = "shell/ssh-key-tools/CreateSshKey.sh",
        requiresInput = true
    )
    
    val DELETE_SSH_KEY = ToolCommand(
        id = "delete_ssh_key",
        name = "删除SSH密钥",
        description = "删除指定的SSH密钥",
        category = ToolCategory.KEY_TOOLS,
        scriptPath = "shell/ssh-key-tools/DeleteSshKey.sh",
        requiresInput = true
    )
    
    val QUERY_SSH_PUBLIC_KEY = ToolCommand(
        id = "query_ssh_public_key",
        name = "查询SSH公钥",
        description = "查看SSH公钥内容",
        category = ToolCategory.KEY_TOOLS,
        scriptPath = "shell/ssh-key-tools/QuerySshPublicKey.sh"
    )
    
    val OPEN_SSH_KEY_DIR = ToolCommand(
        id = "open_ssh_key_dir",
        name = "打开SSH密钥目录",
        description = "在文件管理器中打开.ssh目录",
        category = ToolCategory.KEY_TOOLS,
        scriptPath = "shell/ssh-key-tools/OpenSshKeyDir.sh"
    )
    
    /**
     * 获取所有命令
     */
    fun getAllCommands(): List<ToolCommand> = listOf(
        // 包体工具
        SIGN_APK,
        GET_APK_SIGNATURE,
        SUPPORT_TO_ANDROIDX,
        ANDROIDX_TO_SUPPORT,
        COMPARE_PACKAGE,
        
        // 设备工具 - 基础操作
        INSTALL_APK,
        UNINSTALL_APP,
        SCREENSHOT,
        SCREEN_RECORD,
        CLEAR_APP_DATA,
        KILL_APP_PROCESS,
        VIEW_LOGCAT,
        EXPORT_APK,
        DEVICE_REBOOT,
        DEVICE_SHUTDOWN,
        
        // 设备工具 - 高级操作
        CONNECT_WIRELESS_ADB,
        DISCONNECT_WIRELESS_ADB,
        GRANT_PERMISSION,
        REVOKE_PERMISSION,
        ENABLE_APP,
        DISABLE_APP,
        SET_GLOBAL_PROXY,
        CLEAR_GLOBAL_PROXY,
        GET_SCREEN_INFO,
        GET_TOP_ACTIVITY,
        EXPORT_ANR,
        MANAGE_FILE,
        RUN_MONKEY_TEST,
        
        // 设备工具 - 模拟操作
        CLICK_SCREEN,
        INPUT_TEXT,
        PRESS_BACK,
        PRESS_HOME,
        PRESS_MENU,
        PRESS_POWER,
        PRESS_TASK,
        
        // 逆向工具
        APKTOOL_DECOMPILE,
        APKTOOL_RECOMPILE,
        JADX_VIEW,
        JD_GUI_VIEW,
        DEX_TO_JAR,
        JAR_TO_DEX,
        DEX_TO_SMALI,
        SMALI_TO_DEX,
        DEX_TO_CLASS,
        CLASS_TO_DEX,
        
        // 密钥工具
        CREATE_SSH_KEY,
        DELETE_SSH_KEY,
        QUERY_SSH_PUBLIC_KEY,
        OPEN_SSH_KEY_DIR
    )
    
    /**
     * 根据分类获取命令
     */
    fun getCommandsByCategory(category: ToolCategory): List<ToolCommand> {
        return getAllCommands().filter { it.category == category }
    }
    
    /**
     * 根据ID获取命令
     */
    fun getCommandById(id: String): ToolCommand? {
        return getAllCommands().find { it.id == id }
    }
}
