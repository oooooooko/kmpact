#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 模拟返回键脚本（发送 KEYCODE_BACK）
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

pressBackKey() {
    local deviceId=$1
    adb -s "${deviceId}" shell input keyevent KEYCODE_BACK < /dev/null
    echo "✅ [${deviceId}] 设备已模拟按下返回键"
}

pressBackKeyForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        pressBackKey "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            pressBackKey "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
    exit 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    pressBackKeyForDevice
}

clear
main