#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 标签强推脚本
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../common/FileTools.sh"
source "${scriptDirPath}/../../common/FileTools.sh"
[ -z "" ] || source "../../business/GitTools.sh"
source "${scriptDirPath}/../../business/GitTools.sh"
[ -z "" ] || source "../../business/GitSelector.sh"
source "${scriptDirPath}/../../business/GitSelector.sh"

waitUserInputParameter() {
    repositoryDirPath=$(selectLocalRepositoryPath)
    remoteName=$(selectRemoteName "${repositoryDirPath}")
}

forcePushTags() {
    if ! isTagRemoteChange "${repositoryDirPath}" "${remoteName}" > /dev/null 2>&1; then
        echo "💡 本地标签与远端标签完全一致，无需推送"
        exit 0
    fi

    echo "💡 检测到本地标签与远端标签存在不一致，Git 标签冲突无法通过普通推送解决，必须执行强制推送覆盖远端"
    echo "👻 是否用本地标签覆盖远端对应的标签？（y/n）"
    while true; do
        read -r rewriteTagConfirm
        if [[ "${rewriteTagConfirm}" == "y" || "${rewriteTagConfirm}" == "Y" ]]; then
            echo "👻 该操作会覆盖远端的标签，这是一个危险的操作，你确定要继续吗？（y/n）"
            read -r forcePushBranchConfirm
            if [[ ${forcePushBranchConfirm} != "y" && ${forcePushBranchConfirm} != "Y" ]]; then
                echo "✅ 已放弃强制推送标签"
                exit 0
            fi
            echo "💊 该操作一旦完成将不可逆，并且没有任何形式的备份（没有后悔药），你确定要继续吗？（y/n）"
            read -r forcePushBranchConfirm
            if [[ ${forcePushBranchConfirm} != "y" && ${forcePushBranchConfirm} != "Y" ]]; then
                echo "✅ 已放弃强制推送标签"
                exit 0
            fi

            echo "⏳ 正在强制推送本地标签到远端标签上 ${remoteName}..."
            (cd "${repositoryDirPath}" && git push --force "${remoteName}" --tags)
            local exitCode=$?
            if (( exitCode == 0 )); then
                if ! isTagRemoteChange "${repositoryDirPath}" "${remoteName}"; then
                    echo "✅ 已用本地标签覆盖远端标签"
                    exit 0
                else
                    echo "❌ 检测到推送后本地标签和远端标签仍有差异，该操作可能未生效"
                    exit 1
                fi
            else
                echo "❌ 推送标签失败，错误码：${exitCode}"
                exit "${exitCode}"
            fi
        elif [[ "${rewriteTagConfirm}" == "n" || "${rewriteTagConfirm}" == "N" ]]; then
            echo "✅ 已跳过标签推送"
            exit 0
        else
            echo "👻 输入不正确，请输入正确的选项（y/n）"
            continue
        fi
    done
}

main() {
    printCurrentSystemType
    checkGitEnvironment
    waitUserInputParameter
    forcePushTags
}

clear
main