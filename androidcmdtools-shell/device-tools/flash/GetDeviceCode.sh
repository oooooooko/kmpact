#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 设备代码获取脚本（读取机型代号）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../business/DevicesSelector.sh"
source "${scriptDirPath}/../../business/DevicesSelector.sh"

printDeviceCodeByAdb() {
    local deviceId=$1
    name=$(adb -s "${deviceId}" shell getprop ro.product.name < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备的机型代号为：${name}"
        return 0
    else
        echo "❌ [${deviceId}] 设备获取机型代号失败"
        return 1
    fi
}

printDeviceCodeByFastboot() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(fastboot -s "${deviceId}" getvar product < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        name=$(echo "${outputPrint}" | awk -F': ' '/^[Pp]roduct:/ {print $2; exit}')
        if [[ -z "${name}" ]]; then
            name="${outputPrint}"
        fi
        echo "✅ [${deviceId}] 设备的机型代号为：${name}"
        return 0
    else
        echo "❌ [${deviceId}] 设备获取机型代号失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

printDeviceCodeForDevice() {
    local deviceId
    deviceId="$(inputMultipleDevice)"

    adbDeviceIdsString=$(getAdbDeviceIdsString)
    fastbootDeviceIdsString=$(getFastbootDeviceIdsString)
    if [[ -n "${deviceId}" ]]; then
        if echo "${adbDeviceIdsString}" | grep -xFq "${deviceId}"; then
            printDeviceCodeByAdb "${deviceId}"
        elif echo "${fastbootDeviceIdsString}" | grep -xFq "${deviceId}"; then
            printDeviceCodeByFastboot "${deviceId}"
        fi
    else
        while read -r adbDeviceId; do
            printDeviceCodeByAdb "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')

        while read -r fastbootDeviceId; do
            printDeviceCodeByFastboot "${fastbootDeviceId}"
        done < <(echo "${fastbootDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    checkFastbootEnvironment
    printDeviceCodeForDevice
}

clear
main