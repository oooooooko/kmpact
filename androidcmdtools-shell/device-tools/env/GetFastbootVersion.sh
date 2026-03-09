#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : fastboot 版本获取脚本（打印版本）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../../common/SystemPlatform.sh" && \
source "../../common/EnvironmentTools.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

printFastbootVersion() {
    local outputPrint
    outputPrint=$(fastboot --version < /dev/null | grep -i "fastboot version" | sed 's/^fastboot version //')
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ 当前 fastboot 版本为：${outputPrint}"
        return 0
    else
        echo "❌ 获取 fastboot 版本成功，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

main() {
    printCurrentSystemType
    checkFastbootEnvironment
    printFastbootVersion
}

clear
main