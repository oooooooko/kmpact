#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 文件路径工具脚本
# ----------------------------------------------------------------------
[ -z "" ] || source "/SystemPlatform.sh"
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/SystemPlatform.sh"
[ -z "" ] || source "/EnvironmentTools.sh"
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/EnvironmentTools.sh"

getFileSeparator() {
    if isWindows; then
        # shellcheck disable=SC2288
        echo "\\"
    else
        echo "/"
    fi
}

getWorkDirPath() {
    pwd
}

getResourcesDirPath() {
    local resourcesDirPath
    tempDirPath="$(getWorkDirPath)"
    while [[ "${tempDirPath}" != "$(getFileSeparator)" ]]; do
        if [[ -d "${tempDirPath}$(getFileSeparator)resources" ]]; then
            resourcesDirPath="${tempDirPath}$(getFileSeparator)resources"
            break
        fi
        tempDirPath=$(dirname "${tempDirPath}")
    done
    echo "${resourcesDirPath}"
}

parseComputerFilePath() {
    local filePath=$1
    # 去除首尾的双引号
    filePath=${filePath%\"}
    filePath=${filePath#\"}
    # 去除首尾的单引号
    filePath=${filePath%\'}
    filePath=${filePath#\'}
    # 将转义的空格 "\ " 替换为 " "
    filePath=${filePath//\\ / }
    echo "${filePath}"
}

openFile() {
    local filePath=$1
    if isMacOs; then
        open "${filePath}" < /dev/null > /dev/null
    elif isWindows; then
        if existCommand "powershell"; then
            powershell -NoProfile -Command "Start-Process -FilePath \"$(cygpath -w "${filePath}")\"" < /dev/null > /dev/null
        else
            start "" "${filePath}" < /dev/null > /dev/null
        fi
    else
        xdg-open "${filePath}" < /dev/null > /dev/null
    fi
    return $?
}

openTextFile() {
    local filePath=$1
    if isMacOs; then
        open "${filePath}" < /dev/null > /dev/null
    elif isWindows; then
        if existCommand "notepad"; then
            # 优先使用记事本打开
            notepad "$(cygpath -w "${filePath}")" < /dev/null > /dev/null
        else
            openFile "${filePath}"
        fi
    else
        xdg-open "${filePath}" < /dev/null > /dev/null
    fi
    return $?
}

openDir() {
    local dirPath=$1
    if isMacOs; then
        open "${dirPath}" < /dev/null > /dev/null
    elif isWindows; then
        start "" "${dirPath}" < /dev/null > /dev/null
    else
        xdg-open "${dirPath}" < /dev/null > /dev/null
    fi
    return $?
}

getFileSha256() {
    local filePath="$1"
    local sha256sum

    # 亲测在 macOs 支持 openssl、shasum、
    # 亲测在 Windows Git Bash 支持 openssl、sha256sum、certutil

    if existCommand "openssl"; then
        sha256sum=$(openssl dgst -sha256 "${filePath}" 2>/dev/null | awk -F '= ' '{print tolower($2)}' | grep -ioE '[0-9a-f]{64}')
        if [[ -n "${sha256sum}" ]]; then
            echo "${sha256sum}"
            return 0
        fi
    fi

    if existCommand "shasum"; then
        sha256sum=$(shasum -a 256 "${filePath}" 2>/dev/null | awk '{print $1}' | grep -ioE '[0-9a-f]{64}')
        if [[ -n "${sha256sum}" ]]; then
            echo "${sha256sum}"
            return 0
        fi
    fi

    if existCommand "sha256sum"; then
        sha256sum=$(sha256sum "${filePath}" 2>/dev/null | awk '{print $1}' | grep -ioE '[0-9a-f]{64}')
        if [[ -n "${sha256sum}" ]]; then
            echo "${sha256sum}"
            return 0
        fi
    fi

    if existCommand "certutil"; then
        local hashLine
        sha256sum=$(certutil -hashfile "${filePath}" SHA256 2>/dev/null | sed -n '2p' | grep -ioE '[0-9a-f]{64}')
        if [[ -n "${sha256sum}" ]]; then
            echo "${sha256sum}"
            return 0
        fi
    fi

    return 1
}