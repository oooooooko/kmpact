#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Smali 转 Dex 脚本（smali 汇编）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../../common/SystemPlatform.sh"
[ -z "" ] || source "../../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../../common/FileTools.sh"
source "${scriptDirPath}/../../../common/FileTools.sh"

main() {
    printCurrentSystemType
    checkJavaEnvironment

    resourcesDirPath=$(getResourcesDirPath)
    if [[ -z "${resourcesDirPath}" ]]; then
        echo "❌ 未找到 resources 目录，请确保它位于脚本的当前目录或者父目录"
        exit 1
    fi
    echo "资源目录为：${resourcesDirPath}"

    echo "请输入要汇编的 smali 源目录路径："
    read -r inputSmaliDirPath
    inputSmaliDirPath=$(parseComputerFilePath "${inputSmaliDirPath}")

    if [[ ! -d "${inputSmaliDirPath}" ]]; then
        echo "❌ 目录不存在，请检查 ${inputSmaliDirPath} 目录路径是否正确"
        exit 1
    fi

    echo "请输入生成的 dex 文件路径（可空，默认同名 .dex）："
    read -r outputDexFilePath
    outputDexFilePath=$(parseComputerFilePath "${outputDexFilePath}")

    if [[ -z "${outputDexFilePath}" ]]; then
        outputDexFilePath="${inputSmaliDirPath}-smali2dex-$(date "+%Y%m%d%H%M%S").dex"
    fi

    outputPrint="$(java -jar "${resourcesDirPath}$(getFileSeparator)smali-2.5.2.jar" a "${inputSmaliDirPath}" -o "${outputDexFilePath}" 2>&1)"
    exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ smali 转 dex 失败，原因如下："
        echo "${outputPrint}"
        exit 1
    fi

    if [[ ! -f "${outputDexFilePath}" ]]; then
        echo "❌ 转换失败，请检查 smali 输出的信息："
        echo "${outputPrint}"
        exit 1
    fi

    echo "✅ 转换成功，输出路径：${outputDexFilePath}"
}

clear
main