import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
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
        jsMain.dependencies {
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
        }
        wasmJsMain.dependencies {
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
            packageVersion = "1.0.0"
            description = "Android开发工具集成平台"
            copyright = "© 2026 KMP-OKKO Contributors. All rights reserved."
            vendor = "KMP-OKKO"
            
            // macOS 配置
            macOS {
                bundleID = "com.okko.kmpact"
                iconFile.set(project.file("icon.icns"))
                // 应用程序名称
                appCategory = "public.app-category.developer-tools"
            }
            
            // Windows 配置
            windows {
                iconFile.set(project.file("icon.ico"))
                menuGroup = "AndroidCmdTools"
                upgradeUuid = "A1B2C3D4-E5F6-7890-ABCD-EF1234567890"
            }

        }
    }
}
