#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 应用数据清除脚本（pm clear 清数据）
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

waitUserInputParameter() {
    echo "请输入要清除数据的应用包名："
    while true; do
        read -r packageName
        if [[ -z "${packageName}" ]]; then
            echo "👻 包名不能为空，请重新输入"
            continue
        elif [[ ! "${packageName}" =~ ^[A-Za-z0-9]+(\.[A-Za-z0-9]+)*$ ]]; then
            echo "👻 包名格式有问题，请重新输入"
            continue
        else
            break
        fi
    done
}

clearAppDataSingleDevice() {
    local deviceId=$1
    outputPrint=$(adb -s "${deviceId}" shell pm clear "${packageName}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备清除 ${packageName} 应用数据失败，原因如下："
        echo "${outputPrint}"
        return 0
    fi
    echo "✅ [${deviceId}] 设备已清除 ${packageName} 应用数据"
}

clearAppDataForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        clearAppDataSingleDevice "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            clearAppDataSingleDevice "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
    return 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    waitUserInputParameter
    clearAppDataForDevice
}

clear
main