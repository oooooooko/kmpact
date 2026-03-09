#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 设备关机脚本（adb 或 fastboot 关机）
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

powerOffDeviceByAdb() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" shell reboot -p < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备用 adb 执行关机命令成功"
        return 0
    else
        echo "❌ [${deviceId}] 设备用 adb 执行关机命令失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

powerOffDeviceByFastboot() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(fastboot -s "${deviceId}" oem poweroff < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备用 fastboot 执行关机命令成功"
        echo "需要注意：某些设备需要在拔掉数据线后会立即退出 fastboot 模式并关机"
        return 0
    else
        echo "❌ [${deviceId}] 设备用 fastboot 执行关机命令失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

powerOffDeviceForDevice() {
    local deviceId
    deviceId="$(inputMultipleDevice)"

    echo "你确定要对设备进行关机？（y/n）"
    read -r powerOffConfirm
    if [[ ${powerOffConfirm} =~ ^[yY]$ ]]; then
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        fastbootDeviceIdsString=$(getFastbootDeviceIdsString)
        if [[ -n "${deviceId}" ]]; then
            if echo "${adbDeviceIdsString}" | grep -xFq "${deviceId}"; then
                powerOffDeviceByAdb "${deviceId}"
            elif echo "${fastbootDeviceIdsString}" | grep -xFq "${deviceId}"; then
                powerOffDeviceByFastboot "${deviceId}"
            fi
        else
            while read -r adbDeviceId; do
                powerOffDeviceByAdb "${adbDeviceId}"
            done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')

            while read -r fastbootDeviceId; do
                powerOffDeviceByFastboot "${fastbootDeviceId}"
            done < <(echo "${fastbootDeviceIdsString}" | tr -d '\r' | grep -v '^$')
        fi
        exit 0
    elif [[ ${powerOffConfirm} =~ ^[nN]$ ]]; then
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
    checkFastbootEnvironment
    powerOffDeviceForDevice
}

clear
main