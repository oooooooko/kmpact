#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/31
#      desc    : Git 配置一键优化脚本
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../../common/SystemPlatform.sh" && \
source "../../common/EnvironmentTools.sh" && \
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

    targetEncoding=$(getUtf8EncodingValue)
    if [[ -n "${repositoryDirPath}" ]]; then
        echo "将对仓库 ${repositoryDirPath} 进行 Git 最佳配置："
    else
        echo "将进行全局 Git 最佳配置（适用于所有仓库）："
    fi
    echo "1) 编码设置为 ${targetEncoding}；"
    echo "2) 中文文件名正常显示"
    if isWindows; then
        echo "3) 忽略文件权限变更"
        echo "4) 自动转换换行符为 CRLF"
    else
        echo "3) 检测文件权限变更"
        echo "4) 自动转换换行符为 LF"
    fi
    if [[ -n "${repositoryDirPath}" ]]; then
        echo "是否继续执行以上仓库最佳配置？(y/n) "
    else
        echo "是否继续执行以上全局最佳配置？(y/n) "
    fi
    while true; do
        read -r configConfirm
        if [[ "${configConfirm}" =~ ^[yY]$ ]]; then
            setOptimalConfig
            if [[ -n "${repositoryDirPath}" ]]; then
                echo "✅ 仓库 Git 最佳配置完成"
            else
                echo "✅ 全局 Git 最佳配置完成"
            fi
            break
        elif [[ "${configConfirm}" =~ ^[nN]$ ]]; then
            echo "✅ 用户手动取消操作"
            break
        else
            echo "👻 输入不正确，请输入正确的选项（y/n）"
            continue
        fi
    done
}

setOptimalConfig() {
    local targetQuotePath
    targetQuotePath="$(getQuotePathDisabledValue)"
    if [[ -n "${repositoryDirPath}" ]]; then
        setLocalGitConfig "${repositoryDirPath}" "$(getQuotePathKey)" "${targetQuotePath}"
    else
        setGlobalGitConfig "$(getQuotePathKey)" "${targetQuotePath}"
    fi
    local currentQuotePath
    if [[ -n "${repositoryDirPath}" ]]; then
        currentQuotePath=$(getLocalGitConfig "${repositoryDirPath}" "$(getQuotePathKey)")
    else
        currentQuotePath=$(getGlobalGitConfig "$(getQuotePathKey)")
    fi
    if [[ "${currentQuotePath}" != "${targetQuotePath}" ]]; then
        echo "❌ 中文文件名转义规则配置失败"; exit 1
    fi

    if [[ -n "${repositoryDirPath}" ]]; then
        setLocalGitConfig "${repositoryDirPath}" "$(getCommitEncodingKey)" "${targetEncoding}"
        setLocalGitConfig "${repositoryDirPath}" "$(getLogOutputEncodingKey)" "${targetEncoding}"
        setLocalGitConfig "${repositoryDirPath}" "$(getGuiEncodingKey)" "${targetEncoding}"
    else
        setGlobalGitConfig "$(getCommitEncodingKey)" "${targetEncoding}"
        setGlobalGitConfig "$(getLogOutputEncodingKey)" "${targetEncoding}"
        setGlobalGitConfig "$(getGuiEncodingKey)" "${targetEncoding}"
    fi
    local currentCommitEncoding
    local currentLogOutputEncoding
    local currentGuiEncoding
    if [[ -n "${repositoryDirPath}" ]]; then
        currentCommitEncoding=$(getLocalGitConfig "${repositoryDirPath}" "$(getCommitEncodingKey)")
        currentLogOutputEncoding=$(getLocalGitConfig "${repositoryDirPath}" "$(getLogOutputEncodingKey)")
        currentGuiEncoding=$(getLocalGitConfig "${repositoryDirPath}" "$(getGuiEncodingKey)")
    else
        currentCommitEncoding=$(getGlobalGitConfig "$(getCommitEncodingKey)")
        currentLogOutputEncoding=$(getGlobalGitConfig "$(getLogOutputEncodingKey)")
        currentGuiEncoding=$(getGlobalGitConfig "$(getGuiEncodingKey)")
    fi
    if [[ "${currentCommitEncoding}" != "${targetEncoding}" || "${currentLogOutputEncoding}" != "${targetEncoding}" || "${currentGuiEncoding}" != "${targetEncoding}" ]]; then
        echo "❌ 编码配置失败"
        exit 1
    fi

    local targetFileMode
    if isWindows; then
        targetFileMode="$(getFileModeDisabledValue)"
    else
        targetFileMode="$(getFileModeEnabledValue)"
    fi
    if [[ -n "${repositoryDirPath}" ]]; then
        setLocalGitConfig "${repositoryDirPath}" "$(getFileModeKey)" "${targetFileMode}"
    else
        setGlobalGitConfig "$(getFileModeKey)" "${targetFileMode}"
    fi
    local currentFileMode
    if [[ -n "${repositoryDirPath}" ]]; then
        currentFileMode=$(getLocalGitConfig "${repositoryDirPath}" "$(getFileModeKey)")
    else
        currentFileMode=$(getGlobalGitConfig "$(getFileModeKey)")
    fi
    if [[ "${currentFileMode}" != "${targetFileMode}" ]]; then
        echo "❌ 文件权限检测规则配置失败"
        exit 1
    fi

    local targetAutoCrlf
    if isWindows; then
        targetAutoCrlf="$(getAutoCrlfEnabledValue)"
    else
        targetAutoCrlf="$(getAutoCrlfInputValue)"
    fi
    if [[ -n "${repositoryDirPath}" ]]; then
        setLocalGitConfig "${repositoryDirPath}" "$(getAutoCrlfKey)" "${targetAutoCrlf}"
    else
        setGlobalGitConfig "$(getAutoCrlfKey)" "${targetAutoCrlf}"
    fi
    local currentAutoCrlf
    if [[ -n "${repositoryDirPath}" ]]; then
        currentAutoCrlf=$(getLocalGitConfig "${repositoryDirPath}" "$(getAutoCrlfKey)")
    else
        currentAutoCrlf=$(getGlobalGitConfig "$(getAutoCrlfKey)")
    fi
    if [[ "${currentAutoCrlf}" != "${targetAutoCrlf}" ]]; then
        echo "❌ 核心换行符规则配置失败"
        exit 1
    fi

    local targetSafeCrlf
    if [[ "${targetAutoCrlf}" == "$(getAutoCrlfDisabledValue)" ]]; then
        targetSafeCrlf="$(getSafeCrlfDisabledValue)"
    else
        targetSafeCrlf="$(getSafeCrlfEnabledValue)"
    fi
    if [[ -n "${repositoryDirPath}" ]]; then
        setLocalGitConfig "${repositoryDirPath}" "$(getSafeCrlfKey)" "${targetSafeCrlf}"
    else
        setGlobalGitConfig "$(getSafeCrlfKey)" "${targetSafeCrlf}"
    fi
    local currentSafeCrlf
    if [[ -n "${repositoryDirPath}" ]]; then
        currentSafeCrlf=$(getLocalGitConfig "${repositoryDirPath}" "$(getSafeCrlfKey)")
    else
        currentSafeCrlf=$(getGlobalGitConfig "$(getSafeCrlfKey)")
    fi
    if [[ "${currentSafeCrlf}" != "${targetSafeCrlf}" ]]; then
        echo "❌ 换行符安全校验规则配置失败"
        exit 1
    fi
}

main() {
    printCurrentSystemType
    checkGitEnvironment
    waitUserInputParameter
    setOptimalConfig
}

clear
main