#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 仓库初始化脚本
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "../common/FileTools.sh"
source "${scriptDirPath}/../common/FileTools.sh"

waitUserInputParameter() {
    echo "请输入要初始化为 Git 仓库的目录路径"
    read -r repositoryDirPath
    repositoryDirPath=$(parseComputerFilePath "${repositoryDirPath}")

    if [[ ! -d "${repositoryDirPath}" ]]; then
        echo "❌ 目录不存在：${repositoryDirPath}"
        exit 1
    fi

    if (cd "${repositoryDirPath}" && git rev-parse --is-inside-work-tree < /dev/null > /dev/null 2>&1); then
        echo "❌ 该目录已经是 Git 仓库，请勿重复操作"
        exit 1
    fi
}

initRepository() {
    (cd "${repositoryDirPath}" && git init)
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ 初始化 Git 仓库失败"
        exit 1
    fi

    echo "✅ 已初始化 Git 仓库：${repositoryDirPath} "
}

main() {
    printCurrentSystemType
    checkGitEnvironment
    waitUserInputParameter
    initRepository
}

clear
main