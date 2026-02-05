#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : SSH 密钥删除脚本（移除指定密钥）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/FileTools.sh"
source "${scriptDirPath}/../common/FileTools.sh"

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
    while IFS= read -r -d '' file; do
        pubKeys+=("${file}")
    done < <(find "${sshDirPath}" -maxdepth 1 -type f -name "*.pub" -print0)

    if (( ${#pubKeys[@]} == 0 )); then
        echo "❌ 未发现任何公钥文件（*.pub），无法进行删除 SSH 密钥"
        exit 1
    fi

    echo "发现以下公钥文件（将按所选项删除成对密钥）："
    for i in "${!pubKeys[@]}"; do
        index=$((i+1))
        privateKeyFilePath="${pubKeys[${i}]%.pub}"
        hint=""
        if [[ -f "${privateKeyFilePath}" ]]; then
            hint="（将删除：${privateKeyFilePath} 与 ${pubKeys[${i}]}）"
        else
            hint="（将删除：${pubKeys[${i}]}）"
        fi
        echo "${index}. ${pubKeys[${i}]} ${hint}"
    done

    echo "请输入要删除的序号（必填，仅支持单个）"
    read -r selectIndex

    if [[ -z "${selectIndex}" ]]; then
        echo "❌ 序号不能为空"
        exit 1
    fi
    if [[ ! "${selectIndex}" =~ ^[0-9]+$ ]]; then
        echo "❌ 序号无效"
        exit 1
    fi
    index=$((selectIndex-1))
    if (( index < 0 || index >= ${#pubKeys[@]} )); then
        echo "❌ 序号超出范围"
        exit 1
    fi

    publicKeyFilePath="${pubKeys[${index}]}"
    privateKeyFilePath="${publicKeyFilePath%.pub}"

    echo "即将删除以下文件："
    echo "• ${publicKeyFilePath}"
    if [[ -f "${privateKeyFilePath}" ]]; then
        echo "• ${privateKeyFilePath}"
    fi
    echo "是否确认删除？（y/n）"
    read -r deleteChoose
    if [[ "${deleteChoose}" == "n" || "${deleteChoose}" == "N" ]]; then
        echo "✅ 用户手动取消操作"
        exit 0
    elif [[ "${deleteChoose}" != "y" && "${deleteChoose}" != "Y" ]]; then
        echo "❌ 无效选择，已取消操作"
        exit 1
    fi
}

deleteSshKeyFiles() {
    keyBaseName="$(basename "${privateKeyFilePath}")"
    if [[ "${keyBaseName}" != "id_ed25519" && "${keyBaseName}" != "id_rsa" ]]; then
        configPath="${sshDirPath}$(getFileSeparator)config"
        configKeyPath="${privateKeyFilePath//\\//}"
        if [[ -f "${configPath}" ]]; then
            tempConfig="${configPath}.tmp.$$"
            awk -v path="${configKeyPath}" '
                function flush() { if (buf_len > 0) { if (keep) printf "%s", buf; buf=""; buf_len=0; keep=1 } }
                BEGIN { buf=""; buf_len=0; keep=1; in_block=0 }
                /^Host[[:space:]]+/ { flush(); in_block=1; buf=$0 "\n"; buf_len+=length($0)+1; next }
                /^[[:space:]]*$/ { buf=buf $0 "\n"; buf_len+=length($0)+1; next }
                {
                    buf=buf $0 "\n"; buf_len+=length($0)+1;
                    if ($0 ~ ("IdentityFile[[:space:]]+" path)) { keep=0 }
                }
                END { flush() }
            ' "${configPath}" > "${tempConfig}"
            mv "${tempConfig}" "${configPath}"
            chmod 600 "${configPath}"
            echo "✅ 已从配置文件移除包含该密钥的 Host 块：${configPath}"
        fi
    fi

    echo "⏳ 正在删除密钥文件..."

    if [[ -f "${publicKeyFilePath}" ]]; then
        rm -f "${publicKeyFilePath}"
        if [[ -f "${publicKeyFilePath}" ]]; then
            echo "❌ 公钥文件删除失败，文件路径：${publicKeyFilePath}"
            exit 1
        fi
    fi

    if [[ -f "${privateKeyFilePath}" ]]; then
        rm -f "${privateKeyFilePath}"
        if [[ -f "${privateKeyFilePath}" ]]; then
            echo "❌ 私钥文件删除失败，文件路径：${privateKeyFilePath}"
            exit 1
        fi
    fi

    echo "✅ 密钥文件删除成功"
}

main() {
    printCurrentSystemType
    waitUserInputParameter
    deleteSshKeyFiles
}

clear
main
