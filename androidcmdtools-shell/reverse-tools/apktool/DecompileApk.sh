#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Apk 反编译脚本（使用 apktool 解包）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../common/FileTools.sh"
source "${scriptDirPath}/../../common/FileTools.sh"

waitUserInputParameter() {
    resourcesDirPath=$(getResourcesDirPath)
    echo "资源目录为：${resourcesDirPath}"

    echo "请输入要反编译 apk 包的路径"
    read -r sourceApkFilePath
    sourceApkFilePath=$(parseComputerFilePath "${sourceApkFilePath}")

    if [[ ! -f "${sourceApkFilePath}" ]]; then
        echo "❌ 文件不存在，请检查 ${sourceApkFilePath} 文件路径是否正确"
        exit 1
    fi

    if [[ ! "${sourceApkFilePath}" =~ \.(apk)$ ]]; then
        echo "❌ 文件错误，只能反编译文件名后缀为 apk 的文件"
        exit 1
    fi

    echo "请设置反编译 apk 输出目录路径（可为空，默认输出到和反编译 apk 文件同级且同名的目录下）"
    read -r apkDecompileDirPath
    apkDecompileDirPath=$(parseComputerFilePath "${apkDecompileDirPath}")

    if [[ -z "${apkDecompileDirPath}" ]]; then
        # 默认：在APK同级目录创建带时间戳的目录
        apkDecompileDirPath="${sourceApkFilePath%.*}-decompile-$(date "+%Y%m%d%H%M%S")"
    else
        # 安全检查：防止用户输入系统重要目录
        local dangerousPaths=(
            "$HOME"
            "$HOME/Desktop"
            "$HOME/Downloads"
            "$HOME/Documents"
            "$HOME/Pictures"
            "$HOME/Music"
            "$HOME/Videos"
            "/"
            "/Users"
            "/System"
            "/Applications"
            "/Library"
            "/private"
            "/usr"
            "/bin"
            "/sbin"
            "/etc"
            "/var"
            "/tmp"
        )
        
        # 检查是否是危险目录
        local isDangerous=false
        for dangerousPath in "${dangerousPaths[@]}"; do
            if [[ "${apkDecompileDirPath}" == "${dangerousPath}" ]]; then
                isDangerous=true
                break
            fi
        done
        
        if [[ "${isDangerous}" == true ]]; then
            echo "⚠️  检测到系统重要目录，为了安全，将在该目录下创建子目录"
        fi
        
        # 在用户指定的目录下创建带时间戳的子目录
        local timestamp=$(date "+%Y%m%d%H%M%S")
        local apkBaseName=$(basename "${sourceApkFilePath%.*}")
        apkDecompileDirPath="${apkDecompileDirPath}/apk-decompile-${apkBaseName}-${timestamp}"
        
        echo "📁 实际输出目录：${apkDecompileDirPath}"
    fi

    local apktoolJarFileName="apktool-2.12.1.jar"
    echo "请输入 apktool jar 包的路径（可为空，默认使用 ${apktoolJarFileName}）"
    read -r apktoolJarFilePath
    apktoolJarFilePath=$(parseComputerFilePath "${apktoolJarFilePath}")

    if [[ -z "${apktoolJarFilePath}" ]]; then
        apktoolJarFilePath="${resourcesDirPath}$(getFileSeparator)${apktoolJarFileName}"
    fi

    if [[ ! -f "${apktoolJarFilePath}" ]]; then
        echo "❌ 文件不存在，请检查 ${apktoolJarFilePath} 文件路径是否正确"
        exit 1
    fi

    echo "请输入 framework-res.apk 包所在的目录（可为空）"
    read -r frameworkResourcesDirPath
    frameworkResourcesDirPath=$(parseComputerFilePath "${frameworkResourcesDirPath}")

    frameworkResourcesFilePath="${frameworkResourcesDirPath}$(getFileSeparator)1.apk"
    if [[ -n "${frameworkResourcesDirPath}" ]]; then
        if [[ ! -d "${frameworkResourcesDirPath}" ]]; then
            echo "❌ 目录不存在，请检查 ${frameworkResourcesDirPath} 目录路径是否正确"
            exit 1
        fi
        if [[ ! -f "${frameworkResourcesFilePath}" ]]; then
            echo "❌ 文件不存在，请检查 ${frameworkResourcesFilePath} 文件路径是否正确"
            exit 1
        fi
    fi

    echo "反编译 apk 的路径：${sourceApkFilePath}"

    echo "反编译 apk 包输出目录路径：${apkDecompileDirPath}"

    if [[ -n "${apktoolJarFilePath}" ]]; then
        echo "apktool 包路径：${apktoolJarFilePath}"
    fi

    if [[ -n "${frameworkResourcesDirPath}" ]]; then
        echo "framework-res.apk 目录路径：${frameworkResourcesDirPath}"
        echo "framework-res.apk 文件路径：${frameworkResourcesFilePath}"
    fi
}

decompileApk() {
    echo "⏳ 正在反编译，过程可能会比较慢，请耐心等待"
    if [[ -d "${frameworkResourcesDirPath}" ]]; then
        outputPrint=$(java -jar "${apktoolJarFilePath}" d -f "${sourceApkFilePath}" -o "${apkDecompileDirPath}" -p "${frameworkResourcesDirPath}" 2>&1)
    else
        outputPrint=$(java -jar "${apktoolJarFilePath}" d -f "${sourceApkFilePath}" -o "${apkDecompileDirPath}" 2>&1)
    fi
    exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ 反编译过程中出现错误，原因如下："
        echo "${outputPrint}"
        exit 1
    fi

    # 核心逻辑：目录不存在或者目录为空
    if [[ ! -d "${apkDecompileDirPath}" || -z "$(ls -A "${apkDecompileDirPath}")" ]]; then
        echo "❌ 反编译失败，请检查 apktool 输出的信息："
        echo "${outputPrint}"
        exit 1
    fi

    echo "✅ 反编译 apk 完成，存放目录：${apkDecompileDirPath}"
}

main() {
    printCurrentSystemType
    checkJavaEnvironment
    waitUserInputParameter
    decompileApk
}

clear
main
