import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.okko.kmpact.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AndroidCmdTools"
            packageVersion = "1.0.1"
            description = "Android开发工具集成平台"
            copyright = "© 2026 KMP-OKKO Contributors. All rights reserved."
            vendor = "KMP-OKKO"
            
            // 包含脚本资源文件
            // 将项目根目录的androidcmdtools-shell和androidcmdtools-resources复制到应用包
            includeAllModules = true
            
            // macOS 配置
            macOS {
                bundleID = "com.okko.kmpact"
                iconFile.set(project.file("icon.icns"))
                appCategory = "public.app-category.developer-tools"
                
                // 包含额外的资源文件
                infoPlist {
                    extraKeysRawXml = """
                        <key>LSMinimumSystemVersion</key>
                        <string>10.13</string>
                    """.trimIndent()
                }
            }
            
            // Windows 配置
            windows {
                iconFile.set(project.file("icon.ico"))
                menuGroup = "AndroidCmdTools"
                upgradeUuid = "A1B2C3D4-E5F6-7890-ABCD-EF1234567890"
            }
            
            // Linux 配置
            linux {
                iconFile.set(project.file("icon.png"))
                packageName = "androidcmdtools"
                debMaintainer = "kmpokko@example.com"
                menuGroup = "Development"
                appCategory = "Development"
            }
        }
        
        // 构建时包含资源文件
        buildTypes.release.proguard {
            isEnabled.set(false)
        }
    }
}

// 创建资源复制任务
val copyResourcesToApp = tasks.register<Copy>("copyResourcesToApp") {
    group = "compose desktop"
    description = "复制资源文件到应用包"

    // 在配置阶段设置源目录和目标目录，避免在执行阶段访问 project
    val sourceDir = project.rootDir
    val targetPath = layout.buildDirectory.dir("compose/binaries/main/app/AndroidCmdTools.app/Contents/app")
    
    from(sourceDir) {
        include("androidcmdtools-shell/**")
        include("androidcmdtools-resources/**")
    }
    
    // macOS 应用包的目标路径
    into(targetPath)
}

// 在所有任务配置完成后，设置依赖关系
afterEvaluate {
    // 在 createDistributable 之后复制资源
    tasks.findByName("createDistributable")?.let { task ->
        task.finalizedBy(copyResourcesToApp)
    }
    
    // 在打包任务之前确保资源已复制
    tasks.matching { 
        it.name == "packageDmg" || 
        it.name == "packageMsi" || 
        it.name == "packageDeb" ||
        it.name == "packageDistributionForCurrentOS"
    }.configureEach {
        mustRunAfter(copyResourcesToApp)
    }
}

