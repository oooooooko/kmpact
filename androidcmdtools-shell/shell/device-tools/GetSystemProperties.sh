#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 系统属性获取脚本（读取 getprop）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "/../business/DevicesSelector.sh"
source "${scriptDirPath}/../business/DevicesSelector.sh"

printSystemPropertiesForDevice() {
    local deviceId
    deviceId="$(inputSingleAdbDevice)"
    outputPrint=$(adb -s "${deviceId}" shell getprop < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备获取系统属性失败，原因如下："
        echo "${outputPrint}"
        exit 1
    fi
    echo "✅ [${deviceId}] 设备获取系统属性成功"
    echo "${outputPrint}"
    exit 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    printSystemPropertiesForDevice
}

clear
main