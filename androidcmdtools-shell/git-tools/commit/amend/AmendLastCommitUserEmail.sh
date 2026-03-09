#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 修改最后一次提交用户信息脚本（amend author/email）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../../../common/SystemPlatform.sh" && \
source "../../../common/EnvironmentTools.sh" && \
source "../../../common/FileTools.sh" && \
source "../../../business/GitTools.sh" && \
source "../../../business/GitSelector.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

main() {
    printCurrentSystemType
    checkGitEnvironment

    repositoryDirPath=$(selectLocalRepositoryPath)

    echo "💡 当前脚本仅支持修改最近一次提交的用户名和邮箱"

    if ! (cd "${repositoryDirPath}" && git rev-parse HEAD < /dev/null > /dev/null 2>&1); then
        echo "❌ 当前仓库没有任何提交，无法修改"
        exit 1
    fi

    echo "当前最近一次提交的身份信息如下："
    currAn=$(cd "${repositoryDirPath}" && git log -1 --pretty=%an)
    currAe=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ae)
    currCn=$(cd "${repositoryDirPath}" && git log -1 --pretty=%cn)
    currCe=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ce)
    if [[ "${currAn}" == "${currCn}" && "${currAe}" == "${currCe}" ]]; then
        echo "${currAn} <${currAe}>"
    else
        echo "作者：${currAn} <${currAe}>"
        echo "提交者：${currCn} <${currCe}>"
    fi

    echo "请输入新的用户名"
    read -r newUserName
    if [[ -z "${newUserName}" ]]; then
        echo "❌ 用户名不能为空"
        exit 1
    fi

    echo "请输入新的邮箱"
    read -r newUserEmail
    if [[ -z "${newUserEmail}" ]]; then
        echo "❌ 邮箱不能为空"
        exit 1
    fi

    currentAuthorName=$(cd "${repositoryDirPath}" && git log -1 --pretty=%an)
    currentAuthorEmail=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ae)
    currentCommitterName=$(cd "${repositoryDirPath}" && git log -1 --pretty=%cn)
    currentCommitterEmail=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ce)

    if [[ "${newUserName}" == "${currentAuthorName}" && "${newUserEmail}" == "${currentAuthorEmail}" && "${newUserName}" == "${currentCommitterName}" && "${newUserEmail}" == "${currentCommitterEmail}" ]]; then
        echo "❌ 新的用户名和邮箱与当前一致，未执行修改"
        exit 1
    fi

    prevCommit=$(convertShortHashToLong "${repositoryDirPath}" "HEAD")
    (cd "${repositoryDirPath}" && git -c user.name="${newUserName}" -c user.email="${newUserEmail}" commit --amend --no-edit --author="${newUserName} <${newUserEmail}>")
    latestAuthorName=$(cd "${repositoryDirPath}" && git log -1 --pretty=%an)
    latestAuthorEmail=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ae)
    latestCommitterName=$(cd "${repositoryDirPath}" && git log -1 --pretty=%cn)
    latestCommitterEmail=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ce)

    if [[ "${latestAuthorName}" != "${newUserName}" || "${latestAuthorEmail}" != "${newUserEmail}" || "${latestCommitterName}" != "${newUserName}" || "${latestCommitterEmail}" != "${newUserEmail}" ]]; then
        echo "❌ 修改最后一次提交的用户名和邮箱失败"
        exit 1
    fi

    echo "🤔 提交的用户名和邮箱修改完成，请确认本次修改是否符合你的预期？"
    echo "1. 是的，符合预期"
    echo "2. 不是，给我改回去"
    while true; do
        read -r resultChoice
        if [[ "${resultChoice}" == "1" ]]; then
            echo "✅ 修改最后一次提交的用户名和邮箱成功，如遇到无法推送分支，则应使用强制推送分支"
            exit 0
        elif [[ "${resultChoice}" == "2" ]]; then
            (cd "${repositoryDirPath}" && git reset --hard "${prevCommit}")
            restoredAuthorName=$(cd "${repositoryDirPath}" && git log -1 --pretty=%an)
            restoredAuthorEmail=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ae)
            restoredCommitterName=$(cd "${repositoryDirPath}" && git log -1 --pretty=%cn)
            restoredCommitterEmail=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ce)
            if [[ "${restoredAuthorName}" == "${currentAuthorName}" && "${restoredAuthorEmail}" == "${currentAuthorEmail}" && "${restoredCommitterName}" == "${currentCommitterName}" && "${restoredCommitterEmail}" == "${currentCommitterEmail}" ]]; then
                echo "✅ 还原成功，已回到最初的提交身份"
                exit 0
            else
                echo "❌ 还原失败，提交身份与最初不一致"
                exit 1
            fi
        else
            echo "👻 请选择正确的选项编号"
            continue
        fi
    done
}

clear
main