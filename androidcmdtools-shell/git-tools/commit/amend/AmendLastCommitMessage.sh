#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 修改最后一次提交信息脚本（amend message）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../../common/SystemPlatform.sh"
[ -z "" ] || source "../../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../../common/FileTools.sh"
source "${scriptDirPath}/../../../common/FileTools.sh"
[ -z "" ] || source "../../../business/GitTools.sh"
source "${scriptDirPath}/../../../business/GitTools.sh"
[ -z "" ] || source "../../../business/GitSelector.sh"
source "${scriptDirPath}/../../../business/GitSelector.sh"

main() {
    printCurrentSystemType
    checkGitEnvironment

    repositoryDirPath=$(selectLocalRepositoryPath)

    echo "💡 当前脚本仅支持修改最近一次提交的消息"

    if ! (cd "${repositoryDirPath}" && git rev-parse HEAD < /dev/null > /dev/null 2>&1); then
        echo "❌ 当前仓库没有任何提交，无法修改提交消息"
        exit 1
    fi

    echo "当前最近一次提交消息如下："
    (cd "${repositoryDirPath}" && git log -1 --pretty=%B)

    echo "请输入新的提交消息"
    read -r newMessage
    if [[ -z "${newMessage}" ]]; then
        echo "❌ 提交消息不能为空"
        exit 1
    fi

    prevCommit=$(convertShortHashToLong "${repositoryDirPath}" "HEAD")
    origMessage=$(cd "${repositoryDirPath}" && git log -1 --pretty=%B)
    if [[ "${newMessage}" == "${origMessage}" ]]; then
        echo "❌ 新的提交消息与之前一致，未执行修改"
        exit 1
    fi

    (cd "${repositoryDirPath}" && git commit --amend -m "${newMessage}")
    latestMessage=$(cd "${repositoryDirPath}" && git log -1 --pretty=%B)
    if [[ "${latestMessage}" == "${newMessage}" ]]; then
        echo "🤔 提交的消息修改完成，请确认本次修改是否符合你的预期？"
        echo "1. 是的，符合预期"
        echo "2. 不是，给我改回去"
        while true; do
            read -r resultChoice
            if [[ "${resultChoice}" == "1" ]]; then
                echo "✅ 修改最后一次提交的消息成功，如遇到无法推送分支，则应使用强制推送分支"
                exit 0
            elif [[ "${resultChoice}" == "2" ]]; then
                (cd "${repositoryDirPath}" && git reset --hard "${prevCommit}")
                restoredMessage=$(cd "${repositoryDirPath}" && git log -1 --pretty=%B)
                if [[ "${restoredMessage}" == "${origMessage}" ]]; then
                    echo "✅ 已经还原到最初的状态"
                    echo "还原后的提交消息为："
                    echo "${restoredMessage}"
                    exit 0
                else
                    echo "❌ 还原失败，请手动使用 git reflog 进行恢复"
                    exit 1
                fi
            else
                echo "👻 请选择正确的选项编号"
                continue
            fi
        done
    else
        echo "❌ 修改未成功，最新提交消息与输入不一致"
        echo "最新提交消息为："
        echo "${latestMessage}"
        exit 1
    fi
}

clear
main