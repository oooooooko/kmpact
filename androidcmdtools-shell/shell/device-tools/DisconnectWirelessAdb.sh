#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 关闭无线 adb 调试脚本
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "/../business/DevicesSelector.sh"
source "${scriptDirPath}/../business/DevicesSelector.sh"

disconnectWirelessAdb() {
    local deviceId=$1
    adb disconnect "${deviceId}" < /dev/null > /dev/null
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备无法断开无线调试"
        return 1
    fi
    sleep 1
    connected=$(adb devices < /dev/null | awk -v target="${deviceId}" '$1==target && $2=="device"{print $0}')
    if [[ -z "${connected}" ]]; then
        echo "✅ [${deviceId}] 设备已断开无线调试"
        return 0
    else
        echo "❌ [${deviceId}] 设备断开无线调试失败 "
        return 1
    fi
}

disconnectWirelessAdbForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice "${ADB_MODE_TCP}")"
    if [[ -n "${deviceId}" ]]; then
        disconnectWirelessAdb "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString "${ADB_MODE_TCP}")
        while read -r adbDeviceId; do
            disconnectWirelessAdb "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
    exit 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    disconnectWirelessAdbForDevice
}

clear
main