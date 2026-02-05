# AndroidCmdTools Shell Scripts

这个目录包含了所有AndroidCmdTools的shell脚本。

## 目录结构

```
androidcmdtools-shell/
├── business/           # 业务逻辑脚本
│   ├── DevicesSelector.sh      # 设备选择器
│   ├── GitProperties.sh        # Git属性
│   ├── GitSelector.sh          # Git选择器
│   └── GitTools.sh             # Git工具
│
├── common/             # 通用工具脚本
│   ├── EnvironmentTools.sh     # 环境工具
│   ├── FileTools.sh            # 文件工具
│   ├── IpAddressTools.sh       # IP地址工具
│   ├── PasteTools.sh           # 粘贴工具
│   ├── ProcessTools.sh         # 进程工具
│   └── SystemPlatform.sh       # 系统平台检测
│
├── device-tools/       # 设备工具
│   ├── env/                    # 环境相关
│   ├── flash/                  # 刷机相关
│   ├── hardware/               # 硬件相关
│   ├── jump/                   # 跳转相关
│   ├── simulation/             # 模拟相关
│   ├── InstallApk.sh           # 安装APK
│   ├── UninstallApp.sh         # 卸载应用
│   ├── SaveScreenshot.sh       # 截图
│   └── ...                     # 更多设备工具
│
├── package-tools/      # 包体工具
│   ├── SignatureApk.sh         # APK签名
│   ├── GetApkSignature.sh      # 获取签名信息
│   ├── CompareArchives.sh      # 包体对比
│   ├── SupportToAndroidX.sh   # Support转AndroidX
│   └── AndroidXToSupport.sh   # AndroidX转Support
│
├── reverse-tools/      # 逆向工具
│   ├── apktool/                # apktool相关
│   ├── jadx/                   # jadx相关
│   ├── jd-gui/                 # jd-gui相关
│   └── convert/                # 格式转换
│
└── ssh-key-tools/      # SSH密钥工具
    ├── CreateSshKey.sh         # 创建SSH密钥
    ├── DeleteSshKey.sh         # 删除SSH密钥
    ├── OpenSshKeyDir.sh        # 打开密钥目录
    └── QuerySshPublicKey.sh    # 查询公钥
```

## 使用方法

### 通过应用UI使用

1. 启动应用：`./gradlew :composeApp:run`
2. 在UI中选择相应的功能
3. 填写必要的参数
4. 点击执行按钮
5. 在终端日志中查看执行结果

### 直接执行脚本

```bash
# 进入脚本目录
cd androidcmdtools-shell

# 执行脚本（以APK签名为例）
bash package-tools/SignatureApk.sh
```

## 脚本说明

所有脚本都支持：
- ✅ 交互式输入
- ✅ 参数验证
- ✅ 错误处理
- ✅ 进度提示
- ✅ 批量处理
- ✅ 并行执行

## 依赖要求

### 必需工具

- **bash** - Shell解释器
- **adb** - Android Debug Bridge
- **Java** - JDK 8+

### 可选工具

- **apksigner** - APK签名工具（包体工具需要）
- **apktool** - APK反编译工具（逆向工具需要）
- **jadx** - Java反编译工具（逆向工具需要）
- **jd-gui** - Java反编译GUI（逆向工具需要）

## 注意事项

1. **执行权限**：所有脚本已自动添加执行权限
2. **路径问题**：脚本使用相对路径，请确保在正确的目录执行
3. **平台兼容**：脚本主要针对macOS/Linux，Windows需要Git Bash或WSL
4. **资源文件**：部分脚本需要resources目录中的资源文件

## 故障排除

### 脚本无法执行

```bash
# 添加执行权限
chmod +x androidcmdtools-shell/**/*.sh
```

### 找不到命令

```bash
# 检查adb是否安装
which adb

# 检查Java是否安装
java -version
```

### 脚本路径错误

确保在项目根目录执行应用，脚本会自动定位到正确的路径。

## 更多信息

- 原始项目：https://github.com/getActivity/AndroidCmdTools
- 作者：Android 轮子哥
- 许可证：Apache 2.0
