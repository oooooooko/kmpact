#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : SSH 公钥显示脚本（打印公钥）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../common/SystemPlatform.sh" && \
source "../common/FileTools.sh" && \
source "../common/PasteTools.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

waitUserInputParameter() {
    sshDirPath="${HOME}$(getFileSeparator).ssh"
    if [[ ! -d "${sshDirPath}" ]]; then
        echo "🤔 未找到 ~/.ssh 目录，请选择你的操作："
        echo "1. 创建目录"
        echo "2. 取消"
        read -r createChoose
        if [[ "${createChoose}" == "1" ]]; then
            mkdir -p "${sshDirPath}"
            chmod 700 "${sshDirPath}"
            echo "💡 已创建 ~/.ssh 目录"
        elif [[ "${createChoose}" == "2" ]]; then
            echo "✅ 用户手动取消操作"
            exit 0
        else
            echo "❌ 无效选择，已取消操作"
            exit 1
        fi
    fi

    pubKeys=()
    while IFS= read -r -d '' filePath; do
        pubKeys+=("${filePath}")
    done < <(find "${sshDirPath}" -maxdepth 1 -type f -name "*.pub" -print0)

    if (( ${#pubKeys[@]} == 0 )); then
        echo "❌ 未发现任何公钥文件（*.pub），请先创建新的 SSH 密钥"
        exit 1
    fi

    echo "发现以下公钥文件："
    for i in "${!pubKeys[@]}"; do
        index=$((i+1))
        echo "${index}. ${pubKeys[${i}]}"
    done

    echo "请输入要查看/复制的序号（可空，为空则显示全部）"
    read -r selectIndex

    if [[ -z "${selectIndex}" ]]; then
        for filePath in "${pubKeys[@]}"; do
            echo "========== ${filePath} =========="
            cat "${filePath}"
        done
    else
        if [[ ! "${selectIndex}" =~ ^[0-9]+$ ]]; then
            echo "❌ 序号无效"
            exit 1
        fi
        index=$((selectIndex-1))
        if (( index < 0 || index >= ${#pubKeys[@]} )); then
            echo "❌ 序号超出范围"
            exit 1
        fi
        filePath="${pubKeys[${index}]}"
        echo "========== ${filePath} =========="
        cat "${filePath}"
        echo "========== ${filePath} =========="
        echo "是否复制公钥内容到剪贴板？（y/n）"
        read -r copyConfirm
        if [[ "${copyConfirm}" =~ ^[yY]$ ]]; then
            if copyTextFileToPaste "${filePath}"; then
                echo "✅ 公钥内容已复制到剪贴板"
            else
                echo "❌ 复制失败，请手动复制"
            fi
        fi
    fi
}

main() {
    printCurrentSystemType
    waitUserInputParameter
}

clear
main