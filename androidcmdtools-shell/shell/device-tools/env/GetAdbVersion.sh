#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : adb 版本获取脚本（打印版本）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"

printAdbVersion() {
    local outputPrint
    outputPrint=$(adb --version < /dev/null | grep -i "^Version" | sed 's/^Version //')
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ 当前 adb 版本为：${outputPrint}"
        return 0
    else
        echo "❌ 获取 adb 版本成功，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    printAdbVersion
}

clear
main