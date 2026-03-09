#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 用户名邮箱设置脚本（配置全局或局部信息）
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
    echo "🤔 请选择生效的范围："
    echo "1. 指定仓库生效（局部生效）"
    echo "2. 所有仓库生效（全局生效）"
    while true; do
        read -r scopeChoose
        if [[ "${scopeChoose}" == "1" ]]; then
            repositoryDirPath=$(selectLocalRepositoryPath)

            currentName=$(getLocalGitConfig "${repositoryDirPath}" "$(getUserNameKey)")
            currentEmail=$(getLocalGitConfig "${repositoryDirPath}" "$(getUserEmailKey)")
            if [[ -z "${currentName}" ]]; then
                echo "📝 当前仓库尚未设置用户名"
            else
                echo "📝 当前仓库已设置的用户名：${currentName}"
            fi
            if [[ -z "${currentEmail}" ]]; then
                echo "📝 当前仓库尚未设置邮箱"
            else
                echo "📝 当前仓库已设置的邮箱：${currentEmail}"
            fi
            break
        elif [[ "${scopeChoose}" == "2" ]]; then
            currentName=$(getGlobalGitConfig "$(getUserNameKey)")
            currentEmail=$(getGlobalGitConfig "$(getUserEmailKey)")
            if [[ -z "${currentName}" ]]; then
                echo "📝 全局尚未设置用户名"
            else
                echo "📝 全局已设置的用户名：${currentName}"
            fi
            if [[ -z "${currentEmail}" ]]; then
                echo "📝 全局尚未设置邮箱"
            else
                echo "📝 全局已设置的邮箱：${currentEmail}"
            fi
            break
        else
            echo "👻 无效选择，请重新输入"
            continue
        fi
    done


    echo "请输入 Git 提交的用户名"
    while true; do
        read -r newUserName
        if [[ -z "${newUserName}" ]]; then
            echo "👻 用户名不能为空，请重新输入"
            continue
        else
            break
        fi
    done

    echo "请输入 Git 提交的邮箱"
    while true; do
        read -r newUserEmail
        if [[ -z "${newUserEmail}" ]]; then
            echo "👻 邮箱不能为空，请重新输入"
            continue
        else
            break
        fi
    done
}

setUserNameEmail() {
    if [[ -n "${repositoryDirPath}" ]]; then
        setLocalGitConfig "${repositoryDirPath}" "$(getUserNameKey)" "${newUserName}"
        setLocalGitConfig "${repositoryDirPath}" "$(getUserEmailKey)" "${newUserEmail}"
        currentName=$(getLocalGitConfig "${repositoryDirPath}" "$(getUserNameKey)")
        currentEmail=$(getLocalGitConfig "${repositoryDirPath}" "$(getUserEmailKey)")
        if [[ "${currentName}" != "${newUserName}" || "${currentEmail}" != "${newUserEmail}" ]]; then
            echo "❌ 设置失败，请检查是否有权限修改该仓库的配置"
            exit 1
        fi
        echo "✅ 已设置当前仓库的用户名与邮箱"
    else
        setGlobalGitConfig "$(getUserNameKey)" "${newUserName}"
        setGlobalGitConfig "$(getUserEmailKey)" "${newUserEmail}"
        currentName=$(getGlobalGitConfig "$(getUserNameKey)")
        currentEmail=$(getGlobalGitConfig "$(getUserEmailKey)")
        if [[ "${currentName}" != "${newUserName}" || "${currentEmail}" != "${newUserEmail}" ]]; then
            echo "❌ 设置失败，请检查是否有权限修改全局的配置"
            exit 1
        fi
        echo "✅ 已设置全局的用户名与邮箱"
    fi
}

main() {
    printCurrentSystemType
    checkGitEnvironment
    waitUserInputParameter
    setUserNameEmail
}

clear
main