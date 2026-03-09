#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : SSH 密钥目录打开脚本（打开 ~/.ssh）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../common/SystemPlatform.sh" && \
source "../common/FileTools.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

openSshKeyDirFromFileManager() {
    keysDirPath="${HOME}$(getFileSeparator).ssh"
    if [[ ! -e "${keysDirPath}" ]]; then
        mkdir -p "${keysDirPath}"
    fi
    if [[ ! -d "${keysDirPath}" ]]; then
        echo "❌ SSH 密钥目录不存在：${keysDirPath}，请检查该路径是否被其他文件占用"
        exit 1
    fi
    openDir "${keysDirPath}"
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ 已打开目录：${keysDirPath}"
    else
        echo "❌ 打开目录失败，请手动打开目录：${keysDirPath}"
    fi
}

main() {
    printCurrentSystemType
    openSshKeyDirFromFileManager
}

clear
main