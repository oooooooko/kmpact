#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 进程管理工具
# ----------------------------------------------------------------------
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/SystemPlatform.sh" || source "SystemPlatform.sh"

isProcessRunning() {
    local processName=$1
    local exitCode
    if isWindows; then
        tasklist | grep -i "${processName}.exe" > /dev/null 2>&1
        exitCode=$?
        if (( exitCode != 0)); then
            tasklist | grep -i "${processName}" > /dev/null 2>&1
            exitCode=$?
        fi
    else
        pgrep -x "${processName}" > /dev/null 2>&1
        exitCode=$?
    fi
    return ${exitCode}
}

killProcess() {
    local processName=$1
    if isWindows; then
        taskkill //F //IM "${processName}.exe" > /dev/null 2>&1
        taskkill //F //IM "${processName}" > /dev/null 2>&1
    else
        pkill -x "${processName}" > /dev/null 2>&1
    fi
}