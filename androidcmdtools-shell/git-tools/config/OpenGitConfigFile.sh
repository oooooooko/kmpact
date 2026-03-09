#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/31
#      desc    : 打开 Git 配置文件脚本
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../../common/SystemPlatform.sh" && \
source "../../common/FileTools.sh" && \
source "../../common/EnvironmentTools.sh" && \
source "../../business/GitTools.sh" && \
source "../../business/GitSelector.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

waitUserInputParameter() {
    echo "请选择要打开的配置文件（可空，留空则默认打开全局配置文件）："
    echo "1. 仓库配置文件（.git/config）"
    echo "2. 全局配置文件（~/.gitconfig）"
    while true; do
        read -r openChoice
        if [[ "${openChoice}" == "1" ]]; then
            repositoryDirPath=$(selectLocalRepositoryPath)
            break
        elif [[ "${openChoice}" == "2" ]]; then
            break
        else
            echo "👻 无效选择，请重新输入"
            continue
        fi
    done
}

openGitConfigFile() {
    local configFilePath
    if [[ -n "${repositoryDirPath}" ]]; then
        configFilePath="${repositoryDirPath}$(getFileSeparator).git$(getFileSeparator)config"
    else
        configFilePath="${HOME}$(getFileSeparator).gitconfig"
        if [[ ! -f "${configFilePath}" ]]; then
            touch "${configFilePath}"
        fi
    fi

    if [[ ! -f "${configFilePath}" ]]; then
        echo "❌ 未找到仓库配置文件：${configFilePath}"
        exit 1
    fi

    openTextFile "${configFilePath}"
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ 已打开文件：${configFilePath}"
    else
        echo "❌ 打开文件失败，请手动打开文件：${configFilePath}"
    fi
}

main() {
    printCurrentSystemType
    checkGitEnvironment
    waitUserInputParameter
    openGitConfigFile
}

clear
main