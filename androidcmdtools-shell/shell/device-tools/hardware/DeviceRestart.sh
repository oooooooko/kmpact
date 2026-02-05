#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 设备重启脚本（adb 或 fastboot 重启）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../business/DevicesSelector.sh"
source "${scriptDirPath}/../../business/DevicesSelector.sh"

rebootDeviceByAdb() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" reboot < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备用 adb 执行重启命令成功"
        return 0
    else
        echo "❌ [${deviceId}] 设备用 adb 执行重启命令失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

rebootDeviceByFastboot() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(fastboot -s "${deviceId}" reboot < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备用 fastboot 执行重启命令成功"
        echo "需要注意：某些设备需要在拔掉数据线后会立即退出 fastboot 模式并重启"
        return 0
    else
        echo "❌ [${deviceId}] 设备用 fastboot 执行重启命令失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

rebootDeviceForDevice() {
    local deviceId
    deviceId="$(inputMultipleDevice)"
    echo "你确定要对设备进行重启？（y/n）"
    read -r rebootConfirm
    if [[ ${rebootConfirm} == "y" || ${rebootConfirm} == "Y" ]]; then
        if [[ -n "${deviceId}" ]]; then
            adbDeviceIdsString=$(getAdbDeviceIdsString)
            fastbootDeviceIdsString=$(getFastbootDeviceIdsString)
            if echo "${adbDeviceIdsString}" | grep -xFq "${deviceId}"; then
                rebootDeviceByAdb "${deviceId}"
            elif echo "${fastbootDeviceIdsString}" | grep -xFq "${deviceId}"; then
                rebootDeviceByFastboot "${deviceId}"
            fi
        else
            adbDeviceIdsString=$(getAdbDeviceIdsString)
            while read -r adbDeviceId; do
                rebootDeviceByAdb "${adbDeviceId}"
            done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
            fastbootDeviceIdsString=$(getFastbootDeviceIdsString)
            while read -r fastbootDeviceId; do
                rebootDeviceByFastboot "${fastbootDeviceId}"
            done < <(echo "${fastbootDeviceIdsString}" | tr -d '\r' | grep -v '^$')
        fi
        exit 0
    elif [[ ${rebootConfirm} == "n" || ${rebootConfirm} == "N" ]]; then
        echo "✅ 已取消重启操作"
        exit 0
    else
        echo "❌ 输入错误，取消操作"
        exit 1
    fi
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    checkFastbootEnvironment
    rebootDeviceForDevice
}

clear
main