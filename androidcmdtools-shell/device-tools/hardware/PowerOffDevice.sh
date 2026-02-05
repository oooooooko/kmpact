#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 设备关机脚本（adb 或 fastboot 关机）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../business/DevicesSelector.sh"
source "${scriptDirPath}/../../business/DevicesSelector.sh"

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
    if [[ ${powerOffConfirm} == "y" || ${powerOffConfirm} == "Y" ]]; then
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
    elif [[ ${powerOffConfirm} == "n" || ${powerOffConfirm} == "N" ]]; then
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