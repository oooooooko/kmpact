#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 获取 apk 签名信息
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "../common/FileTools.sh"
source "${scriptDirPath}/../common/FileTools.sh"

main() {
    printCurrentSystemType
    checkJavaEnvironment

    resourcesDirPath=$(getResourcesDirPath)
    if [[ -z "${resourcesDirPath}" ]]; then
        echo "❌ 未找到 resources 目录，请确保它位于脚本的当前目录或者父目录"
        exit 1
    fi
    echo "资源目录为：${resourcesDirPath}"

    echo "请输入要要进行要验证的 apk 包的路径（不能为空）"
    read -r sourceApkFilePath
    sourceApkFilePath=$(parseComputerFilePath "${sourceApkFilePath}")

    if [[ ! -f "${sourceApkFilePath}" ]]; then
        echo "❌ 文件不存在，请检查 ${sourceApkFilePath} 文件路径是否正确"
        exit 1
    fi

    if [[ ! "${sourceApkFilePath}" =~ \.(apk)$ ]]; then
        echo "❌ 文件错误，只能验证文件名后缀为 apk 的文件"
        exit 1
    fi

    echo "请输入 apksigner jar 包的路径（可为空）"
    read -r apkSignerJarFilePath
    apkSignerJarFilePath=$(parseComputerFilePath "${apkSignerJarFilePath}")

    if [[ -z "${apkSignerJarFilePath}" ]]; then
        apkSignerJarFilePath="${resourcesDirPath}$(getFileSeparator)apksigner-36.0.0.jar"
    fi

    if [[ ! -f "${apkSignerJarFilePath}" ]]; then
        echo "❌ 文件不存在，请检查 ${apkSignerJarFilePath} 文件路径是否正确"
        exit 1
    fi

    if [[ ! "${apkSignerJarFilePath}" =~ \.(jar)$ ]]; then
        echo "❌ 文件错误，apksigner 文件名后缀只能是 jar 结尾"
        exit 1
    fi

    echo "要验证签名的 apk 包的路径：${sourceApkFilePath}"
    echo "apksigner jar 包的路径：${apkSignerJarFilePath}"

    outputPrint="$(java -jar "${apkSignerJarFilePath}" verify -verbose -print-certs-pem  "${sourceApkFilePath}" 2>&1)"
    exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ apk 签名验证失败，原因如下："
        echo "${outputPrint}"
        exit 1
    fi

    echo "✅ apk 签名验证成功，签名信息如下："
    echo "${outputPrint}"
}

clear
main