#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 顶部 Activity 包名获取脚本（查询顶层组件）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "/../business/DevicesSelector.sh"
source "${scriptDirPath}/../business/DevicesSelector.sh"

getTopActivityInfo() {
    local deviceId=$1
    outputPrint=$(adb -s "${deviceId}" shell dumpsys window < /dev/null 2>/dev/null | grep mCurrentFocus | tr -d '\r')
    local exitCode=$?
    if (( exitCode != 0 )) || [[ -z "${outputPrint}" ]]; then
        echo "❌ [${deviceId}] 设备获取栈顶 Activity 包名信息失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
    local braceContent
    braceContent=$(echo "${outputPrint}" | sed -n 's/.*mCurrentFocus=Window{\([^}]*\)}/\1/p' | head -n 1)
    local componentName
    componentName=$(echo "${braceContent}" | awk '{print $NF}')
    if [[ "${componentName}" =~ ^[A-Za-z0-9._]+/[A-Za-z0-9._$]+$ ]]; then
        echo "✅ [${deviceId}] 设备栈顶 Activity 组件信息："
        echo "${componentName}"
        return 0
    fi
    local altOutput
    altOutput=$(adb -s "${deviceId}" shell dumpsys activity activities < /dev/null 2>/dev/null | grep -E 'mResumedActivity|mFocusedActivity' | head -n 1 | tr -d '\r')
    local pair
    pair=$(echo "${altOutput}" | grep -oE '[A-Za-z0-9._]+/[A-Za-z0-9._$]+' | head -n 1)
    if [[ -n "${pair}" ]]; then
        local componentPackageName
        local componentClassName
        componentPackageName=${pair%%/*}
        componentClassName=${pair#*/}
        if [[ "${componentClassName}" =~ ^\. ]]; then
            componentClassName="${componentPackageName}${componentClassName}"
        fi
        local fixedComponentName="${componentPackageName}/${componentClassName}"
        if [[ "${fixedComponentName}" =~ ^[A-Za-z0-9._]+/[A-Za-z0-9._$]+$ ]]; then
            echo "✅ [${deviceId}] 设备栈顶 Activity 组件信息："
            echo "${fixedComponentName}"
            return 0
        fi
    fi
    echo "👻 [${deviceId}] 设备未能识别有效的栈顶 Activity 组件，可能处于锁屏或 AOD 状态。"
    return 0
}

printTopActivityComponentForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"

    if [[ -n "${deviceId}" ]]; then
        getTopActivityInfo "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            getTopActivityInfo "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
    exit 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    printTopActivityComponentForDevice
}

clear
main