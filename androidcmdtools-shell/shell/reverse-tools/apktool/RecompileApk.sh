#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 用 apktool 回编译 apk
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

    echo "请输入要回编译的 apk 源目录路径（即反编译后的目录）"
    read -r sourceDirPath
    sourceDirPath=$(parseComputerFilePath "${sourceDirPath}")

    if [[ ! -d "${sourceDirPath}" ]]; then
        echo "❌ 目录不存在，请检查 ${sourceDirPath} 目录路径是否正确"
        exit 1
    fi

    echo "请输入回编译输出的 apk 文件路径（可为空，默认输出到同名 .apk 文件）"
    read -r outputApkFilePath
    outputApkFilePath=$(parseComputerFilePath "${outputApkFilePath}")

    if [[ -z "${outputApkFilePath}" ]]; then
        base="${sourceDirPath%/}"
        outputApkFilePath="${base}-recompile-$(date "+%Y%m%d%H%M%S").apk"
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

    if [[ ! "${apktoolJarFilePath}" =~ \.(jar)$ ]]; then
        echo "❌ 文件错误，apktool 文件名后缀只能是 jar 结尾"
        exit 1
    fi
}

recompileApk() {
    echo "⏳ 正在回编译，过程可能会比较慢，请耐心等待"
    outputPrint="$(java -jar "${apktoolJarFilePath}" b -f "${sourceDirPath}" -o "${outputApkFilePath}" 2>&1)"
    exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ 回编译过程中出现错误，原因如下："
        echo "${outputPrint}"
        exit 1
    fi

    if [[ ! -f "${outputApkFilePath}" ]]; then
        echo "❌ 回编译失败，请检查 apktool 输出的信息："
        echo "${outputPrint}"
        exit 1
    fi

    echo "✅ 回编译 apk 完成，输出文件：${outputApkFilePath}"
}

main() {
    printCurrentSystemType
    checkJavaEnvironment
    waitUserInputParameter
    recompileApk
}

clear
main