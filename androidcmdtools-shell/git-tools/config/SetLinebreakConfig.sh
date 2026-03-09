#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 换行符全局配置脚本
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

    echo "🤔 请选择换行符自动转换规则："
    if isWindows; then
        echo "1. 自动转换为 CRLF（推荐）"
        echo "2. 不进行任何转换（不推荐）"
    else
        echo "1. 自动转换为 LF（推荐）"
        echo "2. 不进行任何转换（不推荐）"
    fi
    while true; do
        read -r autoCrlfChoose
        if [[ "${autoCrlfChoose}" == "1" ]]; then
            if isWindows; then
                targetAutoCrlf="$(getAutoCrlfEnabledValue)"
            else
                targetAutoCrlf="$(getAutoCrlfInputValue)"
            fi
            break
        elif [[ "${autoCrlfChoose}" == "2" ]]; then
            targetAutoCrlf="$(getAutoCrlfDisabledValue)"
            break
        else
            echo "👻 无效选择，请重新输入"
            continue
        fi
    done

    if [[ "${autoCrlfChoose}" == "1" ]]; then
        echo "🤔 请选择换行符安全校验规则："
        echo "1. 严格校验，遇到异常时阻断提交（推荐）"
        echo "2. 宽松校验，遇到异常时仅警告，但不阻断提交"
        echo "3. 关闭校验，遇到异常时不提示不阻断（不推荐）"
        while true; do
            read -r safeCrlfChoose
            if [[ "${safeCrlfChoose}" == "1" ]]; then
                targetSafeCrlf="$(getSafeCrlfEnabledValue)"
                break
            elif [[ "${safeCrlfChoose}" == "2" ]]; then
                targetSafeCrlf="$(getSafeCrlfWarnValue)"
                break
            elif [[ "${safeCrlfChoose}" == "3" ]]; then
                targetSafeCrlf="$(getSafeCrlfDisabledValue)"
                break
            else
                echo "👻 无效选择，请重新输入"
                continue
            fi
        done
    else
        targetSafeCrlf="$(getSafeCrlfDisabledValue)"
    fi
}

setGitLinebreak() {
    if [[ -n "${repositoryDirPath}" ]]; then
        setLocalGitConfig "${repositoryDirPath}" "$(getAutoCrlfKey)" "${targetAutoCrlf}"
        currentAutoCrlf=$(getLocalGitConfig "${repositoryDirPath}" "$(getAutoCrlfKey)")
    else
        setGlobalGitConfig "$(getAutoCrlfKey)" "${targetAutoCrlf}"
        currentAutoCrlf=$(getGlobalGitConfig "$(getAutoCrlfKey)")
    fi
    if [[ "${currentAutoCrlf}" != "${targetAutoCrlf}" ]]; then
        echo "❌ 核心换行符规则配置失败，请检查权限"
        exit 1
    fi

    if [[ -n "${repositoryDirPath}" ]]; then
        setLocalGitConfig "${repositoryDirPath}" "$(getSafeCrlfKey)" "${targetSafeCrlf}"
        currentSafeCrlf=$(getLocalGitConfig "${repositoryDirPath}" "$(getSafeCrlfKey)")
    else
        setGlobalGitConfig "$(getSafeCrlfKey)" "${targetSafeCrlf}"
        currentSafeCrlf=$(getGlobalGitConfig "$(getSafeCrlfKey)")
    fi
    if [[ "${currentSafeCrlf}" != "${targetSafeCrlf}" ]]; then
        echo "❌ 换行符安全校验规则配置失败，请检查权限"
        exit 1
    fi

    echo "✅ Git 换行符配置全部完成"
}

main() {
    printCurrentSystemType
    checkGitEnvironment
    waitUserInputParameter
    setGitLinebreak
}

clear
main