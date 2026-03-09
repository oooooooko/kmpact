#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 跳转关于手机脚本（打开系统关于页面）
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

jumpAboutDevice() {
    local deviceId=$1
    local outputPrint
    local action="android.settings.DEVICE_INFO_SETTINGS"
    outputPrint=$(adb -s "${deviceId}" shell am start -W -a "${action}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )) && { echo "${outputPrint}" | grep -q -E 'Status:\s*ok'; } && { ! echo "${outputPrint}" | grep -qiE 'unable to resolve Intent|Activity not found|Permission denied|SecurityException|Error:'; }; then
        echo "✅ [${deviceId}] 设备跳转到关于本机页面成功"
        return 0
    fi
    echo "❌ [${deviceId}] 设备跳转关于本机页面失败，原因如下："
    echo "${outputPrint}"
    return 1
}

jumpAboutDeviceForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        jumpAboutDevice "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            jumpAboutDevice "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    jumpAboutDeviceForDevice
}

clear
main