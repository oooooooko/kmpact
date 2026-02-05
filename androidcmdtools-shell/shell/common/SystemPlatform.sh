#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 系统平台脚本（判断 macOS/Windows/Linux）
# ----------------------------------------------------------------------

getSystemName() {
    uname -s
}

isMacOs() {
    local systemName
    systemName=$(getSystemName)
    if [[ ${systemName} =~ ^Darwin$ ]]; then
        return 0
    fi
    return 1
}

isWindows() {
    local systemName
    systemName=$(getSystemName)
    if [[ ${systemName} =~ ^(MINGW64_NT|MINGW32_NT|CYGWIN_NT|MSYS_NT) ]]; then
        return 0
    fi
    return 1
}

isWindowWsl() {
    local systemName
    systemName=$(getSystemName)
    if [[ ${systemName} =~ ^Linux$ ]]; then
        if grep -qi microsoft /proc/version; then
            return 0
        fi
    fi
    return 1
}

isLinux() {
    local systemName
    systemName=$(getSystemName)
    if [[ ${systemName} =~ ^Linux$ ]]; then
        if ! grep -qi microsoft /proc/version; then
            return 0
        fi
    fi
    return 1
}

getCurrentSystemType() {
    if isMacOs; then
        echo "macOS"
    elif isWindows; then
        echo "Windows (Git Bash/Cygwin/MSYS)"
    elif isWindowWsl; then
        echo "Windows WSL"
    elif isLinux; then
        echo "Linux"
    else
        echo "Unknown"
    fi
}

printCurrentSystemType() {
    echo "当前系统: $(getCurrentSystemType)"
}