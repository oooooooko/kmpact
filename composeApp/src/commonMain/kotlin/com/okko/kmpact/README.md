# AndroidCmdTools - MVI架构说明

## 项目结构

```
com.example.okko.kmp_okko/
├── presentation/              # Presentation层（MVI核心）
│   ├── base/                 # MVI基类
│   │   ├── BaseIntent.kt     # Intent基类
│   │   ├── BaseUiState.kt    # UiState基类
│   │   ├── BaseEffect.kt     # Effect基类
│   │   └── BaseViewModel.kt  # ViewModel基类
│   │
│   └── packagetools/         # Package Tools功能模块
│       ├── PackageToolsIntent.kt      # 用户意图定义
│       ├── PackageToolsUiState.kt     # UI状态定义
│       ├── PackageToolsEffect.kt      # 副作用定义
│       └── PackageToolsViewModel.kt   # 状态管理
│
├── ui/                       # UI层（View）
│   ├── theme/               # 主题配置
│   │   └── Color.kt         # 颜色定义
│   │
│   ├── components/          # 可复用组件
│   │   └── Sidebar.kt       # 侧边栏组件
│   │
│   └── screens/             # 页面
│       └── PackageToolsScreen.kt  # Package Tools界面
│
├── domain/                   # Domain层（待实现）
│   ├── usecase/             # 业务用例
│   └── model/               # 领域模型
│
├── data/                     # Data层（待实现）
│   ├── repository/          # 数据仓库
│   └── datasource/          # 数据源
│
└── App.kt                    # 应用入口

```

## MVI架构说明

### 数据流向

```
用户操作 → Intent → ViewModel → UseCase → Repository → DataSource
                      ↓
                   UiState → UI渲染
                      ↓
                   Effect → 一次性事件（Toast、Dialog等）
```

### 核心原则

1. **单向数据流**：数据只能从ViewModel流向UI
2. **状态唯一**：UI状态由UiState统一管理
3. **Intent驱动**：所有状态变化必须通过Intent触发
4. **副作用分离**：一次性事件通过Effect处理，不污染UiState

## 已实现功能

### Package Tools界面

- ✅ APK签名和对齐表单
- ✅ 包对比功能UI
- ✅ 文件选择器集成点（待实现具体逻辑）
- ✅ 表单验证和状态管理
- ✅ 响应式布局

### 侧边栏导航

- ✅ 多页面导航
- ✅ 保存的配置显示
- ✅ 活动状态指示

## 待实现功能

### Domain层

- [ ] SignAndOptimizeUseCase - APK签名和优化
- [ ] GenerateDiffReportUseCase - 生成差异报告
- [ ] LoadProfilesUseCase - 加载保存的配置

### Data层

- [ ] FileRepository - 文件操作
- [ ] ConfigRepository - 配置管理
- [ ] LocalDataSource - 本地数据源
- [ ] RemoteDataSource - 远程数据源（如需要）

### UI功能

- [ ] 文件拖放支持
- [ ] 文件选择器集成
- [ ] Toast提示
- [ ] 错误对话框
- [ ] 进度指示器
- [ ] 报告下载

### 其他页面

- [ ] Device Manager界面
- [ ] Reverse Engineer界面
- [ ] ADB Terminal界面

## 使用方法

### 发送Intent

```kotlin
// 在UI层
viewModel.handleIntent(
    PackageToolsIntent.SelectTargetArtifact("/path/to/app.apk")
)
```

### 订阅状态

```kotlin
// 在Composable中
val uiState by viewModel.uiState.collectAsState()

// 使用状态渲染UI
Text(text = uiState.targetArtifactPath)
```

### 处理副作用

```kotlin
LaunchedEffect(Unit) {
    viewModel.effect.collect { effect ->
        when (effect) {
            is PackageToolsEffect.ShowToast -> {
                // 显示Toast
            }
            // 处理其他副作用...
        }
    }
}
```

## 扩展指南

### 添加新功能

1. 在`presentation/`下创建新的功能模块
2. 定义Intent、UiState、Effect
3. 实现ViewModel继承BaseViewModel
4. 在`ui/screens/`创建对应的Screen
5. 在App.kt中添加导航

### 添加业务逻辑

1. 在`domain/usecase/`创建UseCase
2. 在`data/repository/`实现Repository接口
3. 在ViewModel中调用UseCase
4. 更新UiState反映结果

## 注意事项

- ✅ UI层只负责展示，不包含业务逻辑
- ✅ 所有状态变化必须通过Intent触发
- ✅ 一次性事件使用Effect，不放入UiState
- ✅ ViewModel不直接访问DataSource
- ✅ 保持数据单向流动
