#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 剪贴板工具
# ----------------------------------------------------------------------
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/SystemPlatform.sh" || source "SystemPlatform.sh"
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/EnvironmentTools.sh" || source "EnvironmentTools.sh"

copyTextToPaste() {
    local copyText
    # 利用命令替换 $(...) 自动去除尾部换行符的特性，对输入文本进行标准化处理
    copyText=$(printf "%s" "$1")

    if [[ -z "${copyText}" ]]; then
        return 1
    fi

    if isMacOs; then
        if existCommand "pbcopy"; then
            printf "%s" "${copyText}" | pbcopy
        fi
    elif isWindows; then
        if existCommand "clip"; then
            printf "%s" "${copyText}" | clip
        fi
    elif isWindowWsl; then
        if existCommand "clip.exe"; then
            printf "%s" "${copyText}" | clip.exe
        fi
    elif isLinux; then
        if existCommand "wl-copy"; then
            printf "%s" "${copyText}" | wl-copy
        elif existCommand "xclip"; then
            printf "%s" "${copyText}" | xclip -selection clipboard
        elif existCommand "xsel"; then
            printf "%s" "${copyText}" | xsel --clipboard --input
        fi
    fi

    local pasteText
    pasteText=$(readTextForPaste)

    if [[ "${pasteText}" != "${copyText}" ]]; then
        return 1
    fi
    return 0
}

readTextForPaste() {
    local inputText=""
    if isMacOs; then
        if existCommand "pbpaste"; then
            inputText="$(pbpaste)"
        fi
    elif isWindows; then
        if existCommand "powershell"; then
            inputText=$(powershell -Command "Get-Clipboard" < /dev/null 2>/dev/null | sed 's/\r$//')
        fi
    elif isWindowWsl; then
        if existCommand "powershell.exe"; then
            inputText=$(powershell.exe -NoProfile -Command "Get-Clipboard" < /dev/null 2>/dev/null | sed 's/\r$//')
        fi
    elif isLinux; then
        if existCommand "wl-paste"; then
            inputText="$(wl-paste)"
        elif existCommand "xclip"; then
            inputText="$(xclip -selection clipboard -o)"
        elif existCommand "xsel"; then
            inputText="$(xsel --clipboard --output)"
        fi
    fi

    echo "${inputText}"
}

copyTextFileToPaste() {
    local txtFilePath=$1
    if [[ ! -f "${txtFilePath}" ]]; then
        return 1
    fi

    local copyText
    copyText=$(cat "${txtFilePath}")
    copyTextToPaste "${copyText}"
    return $?
}

copyPictureFileToPaste() {
    local pictureFilePath=$1
    if isMacOs; then
        if existCommand "osascript"; then
            osascript -e 'set the clipboard to (read (POSIX file "'"${pictureFilePath}"'") as TIFF picture)'
            return $?
        fi
    elif isWindows; then
        if existCommand "powershell"; then
            powershell -Command "Add-Type -AssemblyName System.Windows.Forms;\$img = [System.Drawing.Image]::FromFile('${pictureFilePath}');[System.Windows.Forms.Clipboard]::SetImage(\$img);\$img.Dispose();"
            return $?
        fi
    elif isWindowWsl; then
        if existCommand "powershell.exe"; then
            local windowsPath
            if existCommand "wslpath"; then
                windowsPath=$(wslpath -w "${pictureFilePath}")
            else
                windowsPath="${pictureFilePath}"
            fi
            powershell.exe -NoProfile -Command "Add-Type -AssemblyName System.Windows.Forms;Add-Type -AssemblyName System.Drawing;\$img = [System.Drawing.Image]::FromFile('${windowsPath}');[System.Windows.Forms.Clipboard]::SetImage(\$img);\$img.Dispose();"
            return $?
        fi
    elif isLinux; then
        if existCommand "wl-copy"; then
            wl-copy < "${pictureFilePath}"
            return $?
        elif existCommand "xclip"; then
            xclip -selection clipboard -t image/png -i "${pictureFilePath}"
            return $?
        fi
    fi

    return 1
}