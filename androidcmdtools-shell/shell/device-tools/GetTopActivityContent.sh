#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 顶部 Activity 内容获取脚本（dump 顶层视图）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "/../business/DevicesSelector.sh"
source "${scriptDirPath}/../business/DevicesSelector.sh"

getTopActivityFocusLine() {
    local deviceId=$1
    adb -s "${deviceId}" shell dumpsys window < /dev/null | grep mCurrentFocus
}

parseComponent() {
    local focusLine="$1"
    local componentName
    componentName=$(echo "${focusLine}" | sed -E 's/.*[[:space:]]([[:alnum:]_\.]+\/[[:alnum:]_\.\\$]+).*/\1/')
    echo "${componentName}"
}

printShowActivityDetails() {
    local deviceId="$1"
    local componentName="$2"
    local componentPackageName="${componentName%%/*}"
    local componentClassName="${componentName#*/}"
    local fullComponentClassName="${componentClassName}"
    if [[ "${componentClassName}" == .* ]]; then
        fullComponentClassName="${componentPackageName}${componentClassName}"
    fi
    echo "📝 当前栈顶 activity：${componentPackageName}/${fullComponentClassName}"
    outputPrint=$(adb -s "${deviceId}" shell dumpsys activity "${componentPackageName}/${fullComponentClassName}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备获取栈顶 Activity 内容信息失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
    echo "✅ [${deviceId}] 设备获取栈顶 Activity 内容信息成功，内容如下："
    echo "${outputPrint}"
    return 0
}

printTopActivityContentForDevice() {
    local deviceId
    deviceId="$(inputSingleAdbDevice)"
    focusLine=$(getTopActivityFocusLine "${deviceId}")
    if [[ -z "${focusLine}" ]]; then
        echo "❌ 未能获取到栈顶 Activity 信息，可能当前没有前台窗口或设备状态异常"
        exit 1
    fi
    componentName=$(parseComponent "${focusLine}")
    if [[ -z "${componentName}" || "${componentName}" == "${focusLine}" ]]; then
        echo "❌ 未能解析栈顶 Activity 组件信息：${focusLine}"
        exit 1
    fi
    printShowActivityDetails "${deviceId}" "${componentName}"
    exit 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    printTopActivityContentForDevice
}

clear
main