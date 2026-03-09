#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 设备序列号获取脚本（读取 serial）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../common/SystemPlatform.sh" && \
source "../common/EnvironmentTools.sh" && \
source "../business/DevicesSelector.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

getDeviceSerialNo() {
    local deviceId=$1
    local serialNo
    serialNo=$(adb -s "${deviceId}" get-serialno < /dev/null | tr -d '\r')
    echo "${serialNo}"
}

printDeviceSerialNoForDevice() {
    local adbDeviceList=()
    adbDeviceIdsString=$(getAdbDeviceIdsString)
    while read -r adbDeviceId; do
        adbDeviceList+=("${adbDeviceId}")
    done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    local adbDeviceCount=${#adbDeviceList[@]}

    if (( adbDeviceCount == 0 )); then
        echo "❌ 连接失败，当前没有检测到有设备和电脑建立了连接"
        exit 1
    fi

    for ((i = 0; i < adbDeviceCount; i++)); do
        deviceId="${adbDeviceList[${i}]}"
        deviceSerialNo=$(getDeviceSerialNo "${deviceId}" 2>&1)
        if [[ -z "${deviceSerialNo}" ]]; then
            echo "❌ [${deviceId}] 设备获取序列号失败"
        else
            echo "✅ [${deviceId}] 设备的序列号为：${deviceSerialNo}"
        fi
    done
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    printDeviceSerialNoForDevice
}

clear
main