#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 编码全局配置脚本
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
            break
        elif [[ "${scopeChoose}" == "2" ]]; then
            break
        else
            echo "👻 无效选择，请重新输入"
            continue
        fi
    done

    echo "🤔 请选择中文文件名转义规则（可空，留空则默认选择第一个）："
    echo "1. 中文文件名正常显示（推荐）"
    echo "2. 中文文件名转义为十六进制格式"
    while true; do
        read -r quotePathChoose
        if [[ -z "${quotePathChoose}" ]]; then
            quotePathChoose="1"
        fi

        if [[ "${quotePathChoose}" == "1" || "${quotePathChoose}" == "2" ]]; then
            if [[ "${quotePathChoose}" == "1" ]]; then
                targetQuotePathEnabledValue="$(getQuotePathDisabledValue)"
            else
                targetQuotePathEnabledValue="$(getQuotePathEnabledValue)"
            fi
            break
        else
            echo "👻 无效选择，请重新输入"
            continue
        fi
    done

    targetEncoding=$(getUtf8EncodingValue)
    echo "请输入 Git 编码格式（可空，留空则默认 ${targetEncoding}）"
    read -r newEncoding
    if [[ -z "${newEncoding}" ]]; then
        newEncoding="${targetEncoding}"
    fi
}

setGitEncoding() {
    if [[ -n "${repositoryDirPath}" ]]; then
        setLocalGitConfig "${repositoryDirPath}" "$(getQuotePathKey)" "${targetQuotePathEnabledValue}"
        currentQuotePath=$(getLocalGitConfig "${repositoryDirPath}" "$(getQuotePathKey)")
    else
        setGlobalGitConfig "$(getQuotePathKey)" "${targetQuotePathEnabledValue}"
        currentQuotePath=$(getGlobalGitConfig "$(getQuotePathKey)")
    fi
    if [[ "${currentQuotePath}" != "${targetQuotePathEnabledValue}" ]]; then
        echo "❌ 中文文件名转义规则配置失败，请检查权限"
        exit 1
    fi

    if [[ -n "${repositoryDirPath}" ]]; then
        setLocalGitConfig "${repositoryDirPath}" "$(getCommitEncodingKey)" "${newEncoding}"
        setLocalGitConfig "${repositoryDirPath}" "$(getLogOutputEncodingKey)" "${newEncoding}"
        setLocalGitConfig "${repositoryDirPath}" "$(getGuiEncodingKey)" "${newEncoding}"
        currentCommitEncoding=$(getLocalGitConfig "${repositoryDirPath}" "$(getCommitEncodingKey)")
        currentLogEncoding=$(getLocalGitConfig "${repositoryDirPath}" "$(getLogOutputEncodingKey)")
        currentGuiEncoding=$(getLocalGitConfig "${repositoryDirPath}" "$(getGuiEncodingKey)")
    else
        setGlobalGitConfig "$(getCommitEncodingKey)" "${newEncoding}"
        setGlobalGitConfig "$(getLogOutputEncodingKey)" "${newEncoding}"
        setGlobalGitConfig "$(getGuiEncodingKey)" "${newEncoding}"
        currentCommitEncoding=$(getGlobalGitConfig "$(getCommitEncodingKey)")
        currentLogEncoding=$(getGlobalGitConfig "$(getLogOutputEncodingKey)")
        currentGuiEncoding=$(getGlobalGitConfig "$(getGuiEncodingKey)")
    fi
    if [[ "${currentCommitEncoding}" != "${newEncoding}" || "${currentLogEncoding}" != "${newEncoding}" || "${currentGuiEncoding}" != "${newEncoding}" ]]; then
        echo "❌ Git 编码配置失败，请检查权限"
        exit 1
    fi

    echo "✅ Git 编码配置全部完成"
}

main() {
    printCurrentSystemType
    checkGitEnvironment
    waitUserInputParameter
    setGitEncoding
}

clear
main