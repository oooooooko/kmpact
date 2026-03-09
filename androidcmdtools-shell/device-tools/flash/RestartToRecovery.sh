#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 恢复模式重启脚本（重启到 recovery）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../../common/SystemPlatform.sh" && \
source "../../common/EnvironmentTools.sh" && \
source "../../business/DevicesSelector.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

rebootToRecoveryByAdb() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" reboot recovery < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备重启到 recovery 模式成功"
        return 0
    else
        echo "❌ [${deviceId}] 设备重启到 recovery 模式失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

rebootToRecoveryByFastboot() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(fastboot -s "${deviceId}" reboot recovery < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备在 fastboot 模式下重启到 recovery 模式成功"
        return 0
    else
        echo "❌ [${deviceId}] 设备在 fastboot 模式下重启到 recovery 模式失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

rebootToRecoveryForDevice() {
    local deviceId
    deviceId="$(inputMultipleDevice)"
    echo "你确定要将设备重启到 recovery 模式？（y/n）"
    read -r rebootRecoveryConfirm
    if [[ ${rebootRecoveryConfirm} =~ ^[yY]$ ]]; then
        if [[ -n "${deviceId}" ]]; then
            adbDeviceIdsString=$(getAdbDeviceIdsString)
            fastbootDeviceIdsString=$(getFastbootDeviceIdsString)
            if echo "${adbDeviceIdsString}" | grep -xFq "${deviceId}"; then
                rebootToRecoveryByAdb "${deviceId}"
            elif echo "${fastbootDeviceIdsString}" | grep -xFq "${deviceId}"; then
                rebootToRecoveryByFastboot "${deviceId}"
            fi
        else
            adbDeviceIdsString=$(getAdbDeviceIdsString)
            while read -r adbDeviceId; do
                rebootToRecoveryByAdb "${adbDeviceId}"
            done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
            fastbootDeviceIdsString=$(getFastbootDeviceIdsString)
            while read -r fastbootDeviceId; do
                rebootToRecoveryByFastboot "${fastbootDeviceId}"
            done < <(echo "${fastbootDeviceIdsString}" | tr -d '\r' | grep -v '^$')
        fi
    elif [[ ${rebootRecoveryConfirm} =~ ^[nN]$ ]]; then
        echo "✅ 已取消操作"
    else
        echo "❌ 输入错误，取消操作"
        exit 1
    fi
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    checkFastbootEnvironment
    rebootToRecoveryForDevice
}

clear
main