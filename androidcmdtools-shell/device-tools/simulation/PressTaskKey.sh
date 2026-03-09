#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 模拟多任务键脚本（发送 KEYCODE_APP_SWITCH）
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

pressTaskKey() {
    local deviceId=$1
    adb -s "${deviceId}" shell input keyevent KEYCODE_APP_SWITCH < /dev/null
    echo "✅ [${deviceId}] 设备已模拟按下多任务键"
}

pressTaskKeyForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        pressTaskKey "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            pressTaskKey "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
    exit 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    pressTaskKeyForDevice
}

clear
main