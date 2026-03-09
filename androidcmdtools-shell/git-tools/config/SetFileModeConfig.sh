#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/02/02
#      desc    : Git 文件权限配置脚本
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../../common/SystemPlatform.sh" && \
source "../../common/EnvironmentTools.sh" && \
source "../../common/FileTools.sh" && \
source "../../business/GitTools.sh" && \
source "../../business/GitSelector.sh" && \
source "../../business/GitProperties.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

waitUserInputParameter() {
    if isWindows; then
        echo "💡 温馨提醒：因为 Windows 系统本身并不支持文件权限的概念，所以设置该配置项并不会有任何实际性作用"
    fi
    echo "🤔 请选择生效的范围："
    echo "1. 指定仓库生效（局部生效）"
    echo "2. 所有仓库生效（全局生效）"
    while true; do
        read -r scopeChoose
        if [[ "${scopeChoose}" == "1" ]]; then
            repositoryDirPath=$(selectLocalRepositoryPath)
            break
        elif [[ "${scopeChoose}" == "2" ]]; then
            break
        else
            echo "👻 无效选择，请重新输入"
            continue
        fi
    done

    echo "🤔 请选择文件权限检测规则："
    if isWindows; then
        echo "1. 忽略权限变更（推荐）"
        echo "2. 检测权限变更（不推荐）"
    else
        echo "1. 检测权限变更（推荐）"
        echo "2. 忽略权限变更（不推荐）"
    fi
    while true; do
        read -r fileModeChoose
        if isWindows; then
            if [[ "${fileModeChoose}" == "1" ]]; then
                targetFileMode="$(getFileModeDisabledValue)"
                break
            elif [[ "${fileModeChoose}" == "2" ]]; then
                targetFileMode="$(getFileModeEnabledValue)"
                break
            fi
        else
            if [[ "${fileModeChoose}" == "1" ]]; then
                targetFileMode="$(getFileModeEnabledValue)"
                break
            elif [[ "${fileModeChoose}" == "2" ]]; then
                targetFileMode="$(getFileModeDisabledValue)"
                break
            fi
        fi
        echo "👻 无效选择，请重新输入"
        continue
    done
}

setGitFileMode() {
    if [[ -n "${repositoryDirPath}" ]]; then
        setLocalGitConfig "${repositoryDirPath}" "$(getFileModeKey)" "${targetFileMode}"
        currentFileMode=$(getLocalGitConfig "${repositoryDirPath}" "$(getFileModeKey)")
    else
        setGlobalGitConfig "$(getFileModeKey)" "${targetFileMode}"
        currentFileMode=$(getGlobalGitConfig "$(getFileModeKey)")
    fi
    if [[ "${currentFileMode}" != "${targetFileMode}" ]]; then
        echo "❌ 文件权限检测规则配置失败，请检查权限"
        exit 1
    fi

    echo "✅ Git 文件权限检测规则配置完成"
}

main() {
    printCurrentSystemType
    checkGitEnvironment
    waitUserInputParameter
    setGitFileMode
}

clear
main