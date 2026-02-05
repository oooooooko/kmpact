#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 快速启动模式重启脚本（重启到 fastboot）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../business/DevicesSelector.sh"
source "${scriptDirPath}/../../business/DevicesSelector.sh"

rebootToFastboot() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" reboot bootloader < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备重启到 fastboot 模式成功"
    else
        echo "❌ [${deviceId}] 设备重启到 fastboot 模式失败，原因如下："
        echo "${outputPrint}"
    fi
}

rebootToFastbootForDevices() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    echo "你确定要将设备重启到 fastboot 模式？（y/n）"
    read -r rebootFastbootConfirm
    if [[ ${rebootFastbootConfirm} == "y" || ${rebootFastbootConfirm} == "Y" ]]; then
        if [[ -n "${deviceId}" ]]; then
            rebootToFastboot "${deviceId}"
        else
            adbDeviceIdsString=$(getAdbDeviceIdsString)
            while read -r adbDeviceId; do
                rebootToFastboot "${adbDeviceId}"
            done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
        fi
        exit 0
    elif [[ ${rebootFastbootConfirm} == "n" || ${rebootFastbootConfirm} == "N" ]]; then
        echo "✅ 已取消关机操作"
        exit 0
    else
        echo "❌ 输入错误，取消操作"
        exit 1
    fi
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    rebootToFastbootForDevices
}

clear
main