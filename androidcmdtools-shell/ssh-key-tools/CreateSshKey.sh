#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : SSH 密钥创建脚本（生成并配置密钥）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../common/SystemPlatform.sh" && \
source "../common/EnvironmentTools.sh" && \
source "../common/FileTools.sh" && \
source "../common/PasteTools.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

KEY_TYPE_ED25519="ed25519"
KEY_TYPE_RSA="rsa"

waitUserInputParameter() {
    echo "🤔 请选择密钥类型，留空则默认选择 ${KEY_TYPE_ED25519}："
    echo "1. ${KEY_TYPE_ED25519}"
    echo "2. ${KEY_TYPE_RSA}"
    read -r keyTypeChoice
    if [[ "${keyTypeChoice}" == "2" ]]; then
        keyType="${KEY_TYPE_RSA}"
    else
        keyType="${KEY_TYPE_ED25519}"
    fi

    echo "请输入密钥注释（通常为邮箱，用于标识）"
    read -r keyComment
    if [[ -z "${keyComment}" ]]; then
        echo "❌ 注释不能为空"
        exit 1
    fi

    sshDirPath="${HOME}$(getFileSeparator).ssh"
    mkdir -p "${sshDirPath}"
    chmod 700 "${sshDirPath}"

    defaultName="id_${keyType}"
    echo "请输入密钥文件名（可空，默认 ${defaultName}）"
    read -r keyName
    if [[ -z "${keyName}" ]]; then
        keyName="${defaultName}"
    fi

    keyPath="${sshDirPath}$(getFileSeparator)${keyName}"
    if [[ -f "${keyPath}" || -f "${keyPath}.pub" ]]; then
        echo "🤔 检测到同名密钥已存在，请选择你的操作："
        [[ -f "${keyPath}" ]] && echo "私钥：${keyPath}"
        [[ -f "${keyPath}.pub" ]] && echo "公钥：${keyPath}.pub"
        echo "1. 覆盖原有的密钥"
        echo "2. 取消生成密钥"
        read -r overwriteChoice
        if [[ "${overwriteChoice}" == "2" ]]; then
            echo "✅ 用户手动取消操作"
            exit 0
        elif [[ "${overwriteChoice}" != "1" ]]; then
            echo "❌ 无效选择，已取消操作"
            exit 1
        fi
    fi

    echo "💡 密钥保护密码用于在使用私钥时进行二次验证，能降低私钥被窃取后立即被滥用的风险，设有保护密码时，每次使用私钥可能需要输入该密码，可以不设密码，使用更便捷但风险更高"
    echo "请输入密钥保护密码（可空，默认不设置）"
    read -r passphrase
    if [[ -z "${passphrase}" ]]; then
        echo "未设置保护密码，将以空密码生成密钥"
    fi
}

createSshKeyFiles() {
    if [[ "${keyType}" == "rsa" ]]; then
        ssh-keygen -t rsa -b 4096 -C "${keyComment}" -f "${keyPath}" -N "${passphrase}"
    else
        ssh-keygen -t ed25519 -C "${keyComment}" -f "${keyPath}" -N "${passphrase}"
    fi

    if [[ ! -f "${keyPath}" && ! -f "${keyPath}.pub" ]]; then
        echo "❌ 密钥生成失败"
        exit 1
    fi

    echo "✅ 密钥文件生成成功"
    echo "生成的私钥文件路径：${keyPath}"
    echo "生成的公钥文件路径：${keyPath}.pub"
 
    configPath="${sshDirPath}$(getFileSeparator)config"
    configKeyPath="${keyPath//\\//}"
    if [[ "${keyName}" != "${defaultName}" ]]; then
        if [[ -f "${configPath}" ]]; then
            if ! grep -Fq "IdentityFile ${configKeyPath}" "${configPath}"; then
                printf "Host *\n  IdentityFile %s\n  IdentitiesOnly yes\n" "${configKeyPath}" >> "${configPath}"
                echo "✅ 已追加到配置文件：${configPath}"
            else
                echo "✅ 配置文件已存在指定密钥规则：${configPath}"
            fi
        else
            printf "Host *\n  IdentityFile %s\n  IdentitiesOnly yes\n" "${configKeyPath}" > "${configPath}"
            echo "✅ 已创建配置文件并写入规则：${configPath}"
        fi
    fi
    chmod 600 "${keyPath}"
    if [[ -f "${configPath}" ]]; then
        chmod 600 "${configPath}"
        echo "✅ 已设置权限 600：${configPath}"
    fi
    echo "✅ 已设置权限 600：${keyPath}"

    echo "========== ${keyPath}.pub =========="
    cat "${keyPath}.pub"
    echo "========== ${keyPath}.pub =========="

    echo "是否将新创建的公钥内容复制到剪贴板？（y/n）"
    read -r copyConfirm
    if [[ "${copyConfirm}" =~ ^[yY]$ ]]; then
        if copyTextFileToPaste "${keyPath}.pub"; then
            echo "✅ 公钥内容已复制到剪贴板"
        else
            echo "❌ 复制失败，请手动复制"
        fi
    fi
}

main() {
    printCurrentSystemType
    waitUserInputParameter
    createSshKeyFiles
}

clear
main