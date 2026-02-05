#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 压缩包对比脚本（比较两个归档差异）
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

    echo "请输入旧 apk/aar/jar/aab 包的路径："
    read -r oldLibraryFilePath
    oldLibraryFilePath=$(parseComputerFilePath "${oldLibraryFilePath}")
    if [[ ! -f "${oldLibraryFilePath}" ]]; then
        echo "文件不存在，请检查 ${oldLibraryFilePath} 文件路径是否正确"
        exit 1
    fi
    if [[ ! "${oldLibraryFilePath}" =~ \.(apk|aar|jar|aab)$ ]]; then
        echo "文件错误，只支持文件名后缀为 apk, aar, jar, aab 包的文件"
        exit 1
    fi

    echo "请输入新 apk/aar/jar/aab 包的路径："
    read -r newLibraryFilePath
    newLibraryFilePath=$(parseComputerFilePath "${newLibraryFilePath}")
    if [[ ! -f "${newLibraryFilePath}" ]]; then
        echo "文件不存在，请检查 ${newLibraryFilePath} 文件路径是否正确"
        exit 1
    fi
    if [[ ! "${newLibraryFilePath}" =~ \.(apk|aar|jar|aab)$ ]]; then
        echo "文件错误，只支持文件名后缀为 apk, aar, jar, aab 包的文件"
        exit 1
    fi

    diffuseJar="${resourcesDirPath}$(getFileSeparator)diffuse-0.1.0.jar"
    java -jar "${diffuseJar}" diff "${oldLibraryFilePath}" "${newLibraryFilePath}"
}

clear
main