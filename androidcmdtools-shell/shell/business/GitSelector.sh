#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 选择器脚本
# ----------------------------------------------------------------------
[ -z "" ] || source "/GitTools.sh"
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/GitTools.sh"
[ -z "" ] || source "../common/FileTools.sh"
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/../common/FileTools.sh"

selectLocalRepositoryPath() {
    echo "请输入 Git 仓库目录路径" >&2
    read -r repositoryDirPath
    repositoryDirPath=$(parseComputerFilePath "${repositoryDirPath}")

    if [[ -z "${repositoryDirPath}" ]]; then
        echo "❌ 输入的目录为空，操作中止" >&2
        kill -SIGTERM $$
        exit 1
    fi

    if ! isGitRepository "${repositoryDirPath}"; then
        echo "❌ 该目录不是 Git 仓库，操作中止" >&2
        kill -SIGTERM $$
        exit 1
    fi
    echo "${repositoryDirPath}"
}

selectRemoteName() {
    local repositoryDirPath="$1"
    local remoteNameList
    remoteNameList=$(cd "${repositoryDirPath}" && git remote)

    local remoteNameCount=${#remoteNameList[@]}
    if (( remoteNameCount == 0 )); then
        echo "❌ 未找到任何远端仓库配置" >&2
        kill -SIGTERM $$
        exit 1
    elif (( remoteNameCount == 1 )); then
        remoteName="${remoteNameList[0]}"
        echo "${remoteName}"
        return 0
    else
        local currentBranch
        currentBranch=$(getCurrentBranchName "${repositoryDirPath}")

        local defaultRemote
        if [[ -n "${currentBranch}" ]]; then
            defaultRemote=$(cd "${repositoryDirPath}" && git config branch."${currentBranch}".remote 2>/dev/null)
        fi

        if [[ -z "${defaultRemote}" ]]; then
            if [[ "${remoteNameList[*]}" =~ "origin" ]]; then
                defaultRemote="origin"
            fi
        fi

        echo "检测到多个远端名称，请输入序号或远端名称（默认：${defaultRemote}）" >&2
        for i in "${!remoteNameList[@]}"; do
            local remote="${remoteNameList[$i]}"
            local mark=""
            if [[ "${remote}" == "${defaultRemote}" ]]; then
                mark="（当前分支绑定的远端分支）"
            fi
            echo "$((i+1)). ${remote}${mark}" >&2
        done

        local remoteName
        while true; do
            read -r inputRemoteName
            if [[ -z "${inputRemoteName}" ]]; then
                if [[ -n "${defaultRemote}" ]]; then
                    remoteName="${defaultRemote}"
                    echo "📝 已自动选择：${remoteName}" >&2
                    echo "${remoteName}"
                    return 0
                else
                    echo "❌ 未输入远端名称且当前分支未绑定远端" >&2
                    kill -SIGTERM $$
                    exit 1
                fi
            elif [[ "${inputRemoteName}" =~ ^[0-9]+$ ]]; then
                local index=$((inputRemoteName-1))
                if (( index >= 0 && index < ${#remoteNameList[@]} )); then
                    remoteName="${remoteNameList[$index]}"
                    echo "📝 已选择 ${remoteName} 远端名称" >&2
                    echo "${remoteName}"
                    return 0
                else
                    echo "👻 无效的序号：${inputRemoteName}，请重新输入" >&2
                    continue
                fi
            else
                local foundFlag="false"
                for remote in "${remoteNameList[@]}"; do
                    if [[ "${remote}" != "${inputRemoteName}" ]]; then
                        continue
                    fi
                    foundFlag="true"
                    break
                done

                if [[ ${foundFlag} == "true" ]]; then
                    remoteName="${inputRemoteName}"
                    echo "${remoteName}"
                    return 0
                else
                    echo "👻 无效的远端名称：${inputRemoteName}，请重新输入" >&2
                    continue
                fi
            fi
        done
    fi
}

selectBranchName() {
    local repositoryDirPath="$1"

    echo "请输入分支名称（可空，默认为当前分支）" >&2
    read -r branchName
    if [[ -z "${branchName}" ]]; then
        branchName=$(getCurrentBranchName "${repositoryDirPath}")
    fi
    echo "${branchName}"
}