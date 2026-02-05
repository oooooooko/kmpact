#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 应用进程杀死脚本（am force-stop）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "/../business/DevicesSelector.sh"
source "${scriptDirPath}/../business/DevicesSelector.sh"

waitUserInputParameter() {
    echo "请输入要杀死的应用包名："
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

isAppRunning() {
    local deviceId=$1
    local pidOutput
    pidOutput=$(adb -s "${deviceId}" shell "pidof ${packageName}" < /dev/null 2>/dev/null)
    if [[ -n "${pidOutput}" ]]; then
        return 0
    fi
    local processCount
    processCount=$(adb -s "${deviceId}" shell "ps -A | awk '{print \$NF}' | grep -E '^${packageName}(:.*)?$' | wc -l" < /dev/null 2>/dev/null)
    if [[ "${processCount}" =~ ^[0-9]+$ ]] && (( processCount > 0 )); then
        return 0
    fi
    return 1
}

killAppProcessSingleDevice() {
    local deviceId=$1
    if ! isAppRunning "${deviceId}"; then
        echo "💡 [${deviceId}] 设备未检测到 ${packageName} 进程运行，已跳过"
        return 0
    fi
    outputPrint=$(adb -s "${deviceId}" shell am force-stop "${packageName}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备无法杀死 ${packageName} 进程，原因如下："
        echo "${outputPrint}"
        return 1
    fi
    sleep 1
    if isAppRunning "${deviceId}"; then
        echo "❌ [${deviceId}] 设备杀死 ${packageName} 进程失败"
        return 1
    fi
    echo "✅ [${deviceId}] 设备已杀死 ${packageName} 进程"
}

killAppProcessForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        killAppProcessSingleDevice "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            killAppProcessSingleDevice "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
    exit 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    waitUserInputParameter
    killAppProcessForDevice
}

clear
main