#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Logcat 查看脚本（支持按包筛选 UID，全版本兼容）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "/../business/DevicesSelector.sh"
source "${scriptDirPath}/../business/DevicesSelector.sh"

isSupportLogcatUidFilter() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" logcat --help 2>&1)
    if echo "${outputPrint}" | grep -q -- "--uid"; then
        return 0
    else
        return 1
    fi
}

waitUserInputParameter() {
    echo "请输入查看 Logcat 的应用包名（留空则查看所有应用的日志）："
    while true; do
        read -r packageName
        if [[ -z "${packageName}" ]]; then
            break
        elif [[ ! "${packageName}" =~ ^[A-Za-z0-9]+(\.[A-Za-z0-9]+)*$ ]]; then
            echo "👻 包名格式有问题，请重新输入"
            continue
        else
            break
        fi
    done
}

displayLogcatSingleDevice() {
    local deviceId=$1
    if [[ -z ${packageName} ]]; then
        adb -s "${deviceId}" logcat < /dev/null
    else
        uid=$(adb -s "${deviceId}" shell am getuid -n "${packageName}" 2>/dev/null | cut -d: -f2 | xargs)
        if [[ -z "${uid}" || ! "${uid}" =~ ^[0-9]+$ ]]; then
            uid=$(adb -s "${deviceId}" shell dumpsys package "${packageName}" < /dev/null 2>/dev/null | awk -F'=' '/userId/{print $2; exit}' | awk '{print $1}' | tr -d '\r')
        fi

        if [[ -z "${uid}" || ! "${uid}" =~ ^[0-9]+$ ]]; then
            echo "❌ 无法解析该包的 UID，请检查包名或设备状态"
            return 1
        fi

        if isSupportLogcatUidFilter "${deviceId}"; then
            echo "📝 设备支持 uid 过滤，使用原生过滤的方式（UID: ${uid}）"
            adb -s "${deviceId}" logcat --uid "${uid}" < /dev/null
        else
            echo "💡 设备不支持 uid 过滤，使用文本过滤的方式（UID: ${uid}）"
            adb -s "${deviceId}" logcat -v uid < /dev/null | grep -F " ${uid} "
        fi
    fi
}

displayLogcatForDevice() {
    local deviceId
    deviceId="$(inputSingleAdbDevice)"
    echo "是否在显示 Logcat 前清除日志以避免输出过多？（y/n），留空则不清除"
    while true; do
        read -r cleanConfirm
        if [[ -z "${cleanConfirm}" ]]; then
            break
        elif [[ "${cleanConfirm}" == "y" || "${cleanConfirm}" == "Y" ]]; then
            adb -s "${deviceId}" logcat -c < /dev/null
            break
        elif [[ "${cleanConfirm}" == "n" || "${cleanConfirm}" == "N" ]]; then
            break
        else
            echo "👻 输入不正确，请输入正确的选项（y/n）"
            continue
        fi
    done
    displayLogcatSingleDevice "${deviceId}"
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    waitUserInputParameter
    displayLogcatForDevice
}

clear
main