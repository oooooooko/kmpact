#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 文件或者目录对比脚本
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../common/SystemPlatform.sh" && \
source "../common/EnvironmentTools.sh" && \
source "../common/FileTools.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

main() {
    printCurrentSystemType
    checkGitEnvironment

    echo "请输入旧文件或旧目录路径："
    read -r oldFilePath
    oldFilePath=$(parseComputerFilePath "${oldFilePath}")
    if [[ -z "${oldFilePath}" ]]; then
        echo "❌ 旧文件或旧目录路径不能为空"
        exit 1
    fi
    if [[ ! -e "${oldFilePath}" ]]; then
        echo "❌ 旧文件或旧目录不存在：${oldFilePath}"
        exit 1
    fi

    echo "请输入新文件或新目录路径："
    read -r newFilePath
    newFilePath=$(parseComputerFilePath "${newFilePath}")
    if [[ -z "${newFilePath}" ]]; then
        echo "❌ 新文件或新目录路径不能为空"
        exit 1
    fi
    if [[ ! -e "${newFilePath}" ]]; then
        echo "❌ 新文件或新目录不存在：${newFilePath}"
        exit 1
    fi

    if [[ -d "${oldFilePath}" && -f "${newFilePath}" ]] || [[ -f "${oldFilePath}" && -d "${newFilePath}" ]]; then
        echo "❌ 路径类型不一致（文件 vs 文件夹），无法对比"
        exit 1
    fi

    echo "⏳ 正在对比差异..."
    git --no-pager diff --no-index -- "${oldFilePath}" "${newFilePath}"
}

clear
main