#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 跳转开发者选项脚本（打开开发者设置页面）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../business/DevicesSelector.sh"
source "${scriptDirPath}/../../business/DevicesSelector.sh"

jumpDevSettings() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" shell am start -W -a android.settings.APPLICATION_DEVELOPMENT_SETTINGS < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )) && { echo "${outputPrint}" | grep -q -E 'Status:\s*ok'; } && { ! echo "${outputPrint}" | grep -qiE 'unable to resolve Intent|Activity not found|Permission denied|SecurityException|Error:'; }; then
        echo "✅ [${deviceId}] 设备已成功打开开发者选项"
        return 0
    fi
    echo "❌ [${deviceId}] 设备打开开发者选项失败，原因如下："
    echo "${outputPrint}"
    return 1
}

jumpDevSettingsForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        jumpDevSettings "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            jumpDevSettings "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    jumpDevSettingsForDevice
}

clear
main