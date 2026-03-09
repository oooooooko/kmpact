#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 分支强推脚本
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../../common/SystemPlatform.sh" && \
source "../../common/EnvironmentTools.sh" && \
source "../../common/FileTools.sh" && \
source "../../business/GitTools.sh" && \
source "../../business/GitSelector.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

waitUserInputParameter() {
    repositoryDirPath=$(selectLocalRepositoryPath)
    remoteName=$(selectRemoteName "${repositoryDirPath}")
    branchName=$(selectBranchName "${repositoryDirPath}")

    if ! hasLocalBranch "${repositoryDirPath}" "${branchName}"; then
        echo "❌ 本地分支不存在或无法获取：${branchName}"
        exit 1
    fi

    # --force-with-lease 依赖于本地的远程跟踪分支（remote-tracking branch）是否最新。
    # 如果用户很久没有 git fetch 本地认为远端还在旧版本，从而允许覆盖远端的新提交，这会让“安全强推”变得不那么安全。
    (cd "${repositoryDirPath}" && git fetch "${remoteName}")
    if ! hasRemoteBranch "${repositoryDirPath}" "${remoteName}" "${branchName}"; then
        echo "❌ 远端分支不存在或无法获取：${remoteName}/${branchName}"
        exit 1
    fi
}

forcePushBranch() {
    # 1. 检查是否完全一致
    if ! isBranchRemoteChange "${repositoryDirPath}" "${remoteName}" "${branchName}" > /dev/null 2>&1; then
        echo "💡 本地分支与远端分支内容一致，无需推送"
        exit 0
    fi

    # 2. 检查是否为快进模式 (Fast-forward)
    # 检查远端分支是否合并到了本地分支（即远端是本地的祖先）
    # git merge-base --is-ancestor <ancestor> <commit>
    local localCommit
    local remoteCommit
    localCommit=$(cd "${repositoryDirPath}" && git rev-parse "${branchName}")
    remoteCommit=$(cd "${repositoryDirPath}" && git rev-parse "${remoteName}/${branchName}")
    
    if (cd "${repositoryDirPath}" && git merge-base --is-ancestor "${remoteCommit}" "${localCommit}"); then
        echo "💡 检测到远端分支落后于本地分支，这种情况不需要强制推送，建议使用普通推送（git push）即可"
        exit 0
    fi

    timestamp=$(date "+%Y%m%d%H%M%S")
    backupBranch="${branchName}_${remoteName}_backup_${timestamp}"
    if ! (cd "${repositoryDirPath}" && git branch "${backupBranch}" "${remoteName}/${branchName}" < /dev/null > /dev/null 2>&1); then
        echo "❌ 创建本地备份分支失败：${backupBranch}"
        exit 1
    fi

    echo "🤔 请选择强制推送的策略："
    echo "1. 安全强推（推荐）：会先检查远端分支是否被他人更新，若有更新则推送失败，避免覆盖他人提交"
    echo "2. 暴力强推（不推荐）：不管远端分支是什么状态，直接用本地分支覆盖远端分支，可能会覆盖他人提交"
    forcePushStrategy=""
    while true; do
        read -r forcePushStrategyChoice
        if [[ "${forcePushStrategyChoice}" == "1" ]]; then
            forcePushStrategy="--force-with-lease"
            break
        elif [[ "${forcePushStrategyChoice}" == "2" ]]; then
            forcePushStrategy="--force"
            break
        else
            echo "👻 请选择正确的选项编号"
        fi
    done

    echo "👻 强制推送分支会强制覆盖远端分支的提交，你确定要继续吗？（y/n）"
    read -r forcePushBranchConfirm
    if [[ "${forcePushBranchConfirm}" =~ ^[nN]$ ]]; then
        echo "✅ 用户手动取消强制推送分支"
        exit 0
    elif [[ ! "${forcePushBranchConfirm}" =~ ^[yY]$ ]]; then
        echo "❌ 无效选择，已取消操作"
        exit 0
    fi

    echo "⏳ 正在强制推送本地分支 ${branchName} 到远端分支上 ${remoteName}..."
    (cd "${repositoryDirPath}" && git push ${forcePushStrategy} "${remoteName}" "${branchName}")
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ 强制推送成功，已将远端分支 ${branchName} 备份到本地分支 ${backupBranch}"
        exit 0
    fi

    echo "👻 强制推送失败，是否进行重试？（y/n）"
    while true; do
        read -r retryConfirm
        if [[ ${retryConfirm} =~ ^[nN]$ ]]; then
            (cd "${repositoryDirPath}" && git branch -D "${backupBranch}" < /dev/null > /dev/null 2>&1)
            echo "✅ 已放弃强制推送分支，已删除本地备份分支 ${backupBranch}"
            exit "${exitCode}"
        elif [[ ! ${retryConfirm} =~ ^[yY]$ ]]; then
            echo "👻 输入不正确，请输入正确的选项（y/n）"
            continue
        fi

        (cd "${repositoryDirPath}" && git push ${forcePushStrategy} "${remoteName}" "${branchName}")
        exitCode=$?
        if (( exitCode == 0 )); then
            if ! isBranchRemoteChange "${repositoryDirPath}" "${remoteName}" "${branchName}"; then
                echo "✅ 强制推送成功（${forcePushStrategy}），已将远端分支 ${branchName} 备份到本地分支 ${backupBranch}"
                exit 0
            else
                echo "❌ 检测到推送后本地分支和远端分支仍有差异，该操作可能未生效"
                exit 1
            fi
        else
            echo "❌ 强制推送失败，错误码：${exitCode}"
            exit "${exitCode}"
        fi
    done
}

main() {
    printCurrentSystemType
    checkGitEnvironment
    waitUserInputParameter
    forcePushBranch
}

clear
main