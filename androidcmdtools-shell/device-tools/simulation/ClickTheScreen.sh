#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 模拟点击屏幕脚本（指定坐标点击）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../business/DevicesSelector.sh"
source "${scriptDirPath}/../../business/DevicesSelector.sh"

waitUserInputParameter() {
    echo "请输入要点击的 X 坐标"
    read -r xCoordinate
    echo "请输入要点击的 Y 坐标"
    read -r yCoordinate
    if [[ -z "${xCoordinate}" || -z "${yCoordinate}" ]]; then
        echo "❌ 坐标不能为空"
        exit 1
    fi
    if [[ ! "${xCoordinate}" =~ ^[0-9]+$ ]] || [[ ! "${yCoordinate}" =~ ^[0-9]+$ ]]; then
        echo "❌ 坐标必须为整数"
        exit 1
    fi
}

clickScreen() {
    local deviceId=$1
    adb -s "${deviceId}" shell input tap "${xCoordinate}" "${yCoordinate}" < /dev/null
    echo "✅ [${deviceId}] 设备已模拟点击屏幕，坐标：(${xCoordinate}, ${yCoordinate})"
}

clickScreenForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        clickScreen "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            clickScreen "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
    exit 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    waitUserInputParameter
    clickScreenForDevice
}

clear
main