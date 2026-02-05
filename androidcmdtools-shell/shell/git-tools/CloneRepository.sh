#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 仓库克隆脚本（支持 SSH/HTTPS）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "../common/FileTools.sh"
source "${scriptDirPath}/../common/FileTools.sh"

waitUserInputParameter() {
    echo "请输入要克隆的远端仓库地址（例如 https://... 或 git@...）："
    read -r repositoryUrl
    if [[ -z "${repositoryUrl}" ]]; then
        echo "❌ 远端仓库地址不能为空"
        exit 1
    fi

    echo "⏳ 正在获取远端分支列表，请稍候..."
    defaultBranchName=$(git ls-remote --symref "${repositoryUrl}" HEAD 2>/dev/null | grep "^ref:" | awk '{print $2}' | sed 's/refs\/heads\///')
    remoteBranchNames=$(git ls-remote --heads "${repositoryUrl}" 2>/dev/null | awk '{print $2}' | sed 's/refs\/heads\///')

    if [[ -n "${remoteBranchNames}" ]]; then
        IFS=$'\n' read -r -d '' -a brancheNameList <<< "${remoteBranchNames}"
        if [[ ${#brancheNameList[@]} -eq 1 ]]; then
            branchName="${brancheNameList[0]}"
            echo "📝 检测到远端分支只有一个：${branchName}，已自动选择"
        else
            echo "🤔 检测到多个远端分支，请输入序号或分支名称（可空，留空则默认拉取 ${defaultBranchName} 分支）："
            for i in "${!brancheNameList[@]}"; do
                local tempBranchName="${brancheNameList[$i]}"
                if [[ "${tempBranchName}" == "${defaultBranchName}" ]]; then
                    echo -e "\033[31m$((i+1)). ${tempBranchName}\033[0m"
                else
                    echo "$((i+1)). ${tempBranchName}"
                fi
            done
            while true; do
                read -r inputBranch
                if [[ -z "${inputBranch}" ]]; then
                    branchName="${defaultBranchName}"
                    break
                elif [[ "${inputBranch}" =~ ^[0-9]+$ ]]; then
                    index=$((inputBranch-1))
                    if (( index >= 0 && index < ${#brancheNameList[@]} )); then
                        branchName="${brancheNameList[$index]}"
                        echo "📝 已选择 ${branchName} 分支"
                        break
                    else
                        echo "👻 无效的序号，请重新输入"
                        continue
                    fi
                else
                    local foundFlag="false"
                    for tempBranchName in "${brancheNameList[@]}"; do
                        if [[ "${tempBranchName}" != "${inputBranch}" ]]; then
                            continue
                        fi
                        foundFlag="true"
                        break
                    done

                    if [[ ${foundFlag} == "true" ]]; then
                        branchName="${inputBranch}"
                        break
                    else
                        echo "👻 无效的分支名称，请重新输入"
                        continue
                    fi
                fi
            done
        fi
    else
        echo "👻 无法获取远端分支列表（可能是网络问题或仓库地址错误），请手动输入"
        echo "请输入要克隆的远端分支名称（可空，默认拉取主分支）"
        read -r branchName
    fi

    repositoryName=$(basename "${repositoryUrl}" .git)

    echo "请输入克隆到的目标目录（不需要带项目名称，不指定则默认克隆到当前文件夹）"
    read -r outputDirPath
    outputDirPath=$(parseComputerFilePath "${outputDirPath}")

    if [[ -z "${outputDirPath}" ]]; then
        workDirPath=$(getWorkDirPath)
        echo "当前工作目录为：${workDirPath}"
        targetDirPath="${workDirPath}$(getFileSeparator)${repositoryName}"
    else
        targetDirPath="${outputDirPath}$(getFileSeparator)${repositoryName}"
    fi

    if [[ -d "${targetDirPath}" && -n "$(ls -A "${targetDirPath}")" ]]; then
        echo "👻 目标目录已存在且非空，是否覆盖？（y/n）"
        while true; do
            read -r overwriteConfirm
            if [[ "${overwriteConfirm}" == "y" || "${overwriteConfirm}" == "Y" ]]; then
                echo "🧹 正在清理原目录以覆盖..."
                rm -rf "${targetDirPath}"
                break
            elif [[ "${overwriteConfirm}" == "n" || "${overwriteConfirm}" == "N" ]]; then
                baseDirPath=$(dirname "${targetDirPath}")
                suffix=2
                newDirPath="${baseDirPath}$(getFileSeparator)${repositoryName} (${suffix})"
                while [[ -d "${newDirPath}" ]]; do
                    suffix=$((suffix+1))
                    newDirPath="${baseDirPath}$(getFileSeparator)${repositoryName} (${suffix})"
                done
                targetDirPath="${newDirPath}"
                echo "📁 将克隆到新的目录：${targetDirPath}"
                break
            else
                echo "👻 输入不正确，请输入正确的选项（y/n）"
                continue
            fi
        done
    fi
}

loopCloneRepository() {
    currentRetryCount=1
    maxRetryCount=10
    echo "⏳ 项目拉取进行中，仓库地址：${repositoryUrl}"
    while true; do
        while (( currentRetryCount <= maxRetryCount )); do
            if (( currentRetryCount > 1 )); then
                case ${currentRetryCount} in
                    2) sleep 3 ;;
                    3) sleep 6 ;;
                    4) sleep 9 ;;
                    5) sleep 12 ;;
                    *) sleep 3 ;;
                esac
            fi
            if [[ -z "${branchName}" ]]; then
                git clone --progress "${repositoryUrl}" "${targetDirPath}"
            else
                git clone --progress -b "${branchName}" "${repositoryUrl}" "${targetDirPath}"
            fi
            exitCode=$?
            if (( exitCode == 0 )); then
                echo "✅ 项目拉取成功，已克隆到 ${targetDirPath}"
                exit 0
            fi
            echo "👻 项目拉取失败，正在重试（第 ${currentRetryCount}/${maxRetryCount} 次）"
            if [[ -d "${targetDirPath}" ]]; then
                # 安全检查，防止误删
                if [[ -n "${targetDirPath}" && "${targetDirPath}" != "/" ]]; then
                    rm -rf "${targetDirPath}"
                fi
            fi
            currentRetryCount=$((currentRetryCount+1))
            continue
        done
        echo "👻 已重试 ${maxRetryCount} 次仍失败，是否继续重试？（y/n）"
        while true; do
            read -r retryConfirm
            if [[ "${retryConfirm}" == "y" || "${retryConfirm}" == "Y" ]]; then
                currentRetryCount=1
                break
            elif [[ "${retryConfirm}" == "n" || "${retryConfirm}" == "N" ]]; then
                echo "✅ 用户手动取消重试"
                exit 0
            else
                echo "👻 输入不正确，请输入正确的选项（y/n）"
                continue
            fi
        done
    done
}

main() {
    printCurrentSystemType
    checkGitEnvironment
    waitUserInputParameter
    loopCloneRepository
}

clear
main