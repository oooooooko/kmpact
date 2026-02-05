#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 工具脚本
# ----------------------------------------------------------------------
isGitRepository() {
    local repositoryDirPath=$1
    (cd "${repositoryDirPath}" && git rev-parse --is-inside-work-tree > /dev/null 2>&1)
    return $?
}

getCurrentBranchName() {
    local repositoryDirPath=$1
    (cd "${repositoryDirPath}" && git symbolic-ref --short HEAD 2>/dev/null)
}

hasLocalBranch() {
    local repositoryDirPath=$1
    local branchName=$2
    (cd "${repositoryDirPath}" && git rev-parse --verify "${branchName}" > /dev/null 2>&1)
    return $?
}

hasRemoteBranch() {
    local repositoryDirPath=$1
    local remoteName=$2
    local branchName=$3
    (cd "${repositoryDirPath}" && git rev-parse --verify "${remoteName}/${branchName}" > /dev/null 2>&1)
    return $?
}

convertShortHashToLong() {
    local repositoryDirPath=$1
    local shortHash=$2
    (cd "${repositoryDirPath}" && git rev-parse "${shortHash}")
}

hasCommitHash() {
    local repositoryDirPath=$1
    local commitHash=$2
    (cd "${repositoryDirPath}" && git cat-file -t "${commitHash}" > /dev/null 2>&1)
    return $?
}

getGlobalGitConfig() {
    local configKey="$1"
    local configValue
    configValue=$(git config --global --get "${configKey}" 2>/dev/null)
    echo "${configValue}"
}

setGlobalGitConfig() {
    local configKey="$1"
    local configValue="$2"
    git config --global "${configKey}" "${configValue}"
}

getLocalGitConfig() {
    local repositoryDirPath="$1"
    local configKey="$2"
    local configValue
    configValue=$(cd "${repositoryDirPath}" && git config --local --get "${configKey}" 2>/dev/null)
    echo "${configValue}"
}

setLocalGitConfig() {
    local repositoryDirPath="$1"
    local configKey="$2"
    local configValue="$3"
    (cd "${repositoryDirPath}" && git config --local "${configKey}" "${configValue}")
}

isBranchRemoteChange() {
    local repositoryDirPath=$1
    local remoteName=$2
    local branchName=$3
    local localHash
    local remoteHash
    localHash=$(cd "${repositoryDirPath}" && git rev-parse "${branchName}")
    remoteHash=$(cd "${repositoryDirPath}" && git rev-parse "${remoteName}/${branchName}")
    if [[ "${localHash}" != "${remoteHash}" ]]; then
        echo "📝 本地和远端的提交哈希不一致" >&2
        return 0
    else
        echo "📝 本地和远端的提交哈希保持一致" >&2
        return 1
    fi
}

isTagRemoteChange() {
    local repositoryDirPath="${1}"
    local remoteName="${2}"
    local localTagsNames
    local remoteTagsLines
    local changeFlag=0

    localTagsNames=$(cd "${repositoryDirPath}" && git tag < /dev/null 2>/dev/null)
    remoteTagsLines=$(cd "${repositoryDirPath}" && git ls-remote --tags "${remoteName}" < /dev/null 2>/dev/null)

    if [[ -z "${localTagsNames}" && -z "${remoteTagsLines}" ]]; then
        return 0
    fi

    echo "🔍 正在对比标签..." >&2

    while IFS= read -r tag; do
        if [[ -z "${tag}" ]]; then
            continue
        fi
        local localHash
        local remoteHash
        localHash=$(cd "${repositoryDirPath}" && git rev-parse "${tag}^{}" < /dev/null 2>/dev/null)
        remoteHash=$(echo "${remoteTagsLines}" | awk -v t="${tag}" '$2=="refs/tags/" t "^{}" {print $1}')
        if [[ -z "${remoteHash}" ]]; then
            remoteHash=$(echo "${remoteTagsLines}" | awk -v t="${tag}" '$2=="refs/tags/" t {print $1}')
        fi
        if [[ -z "${remoteHash}" ]]; then
            echo "👉 本地标签 '${tag}' 在远端不存在" >&2
            changeFlag=1
        elif [[ "${localHash}" != "${remoteHash}" ]]; then
            echo "≠ 标签 '${tag}' 不一致 (本地: ${localHash:0:7} vs 远端: ${remoteHash:0:7})" >&2
            changeFlag=1
        fi
    done <<< "${localTagsNames}"

    local remoteTagNames
    remoteTagNames=$(echo "${remoteTagsLines}" | sed -e 's#.*refs/tags/##' -e 's#\^{}##' | sort | uniq)

    while IFS= read -r remoteTag; do
        if [[ -z "${remoteTag}" ]]; then
            continue
        fi
        if ! echo "${localTagsNames}" | grep -Fx "${remoteTag}" > /dev/null 2>&1; then
            echo "👈 远端标签 '${remoteTag}' 在本地不存在" >&2
            changeFlag=1
        fi
    done <<< "${remoteTagNames}"

    if (( changeFlag == 1 )); then
        return 0
    else
        return 1
    fi
}