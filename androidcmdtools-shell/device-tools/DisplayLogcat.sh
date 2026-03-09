#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Logcat 查看脚本（支持按包筛选 UID，全版本兼容）
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
        return
    fi

    local androidVersionCode
    androidVersionCode=$(getAndroidVersionCodeByAdb "${deviceId}")

    if (( androidVersionCode >= 24 )); then
        local uid
        if (( androidVersionCode >= 26 )); then
            uid=$(adb -s "${deviceId}" shell pm list packages -U < /dev/null 2>/dev/null | grep "${packageName}" | awk -F 'uid:' '{print $2}')
        fi

        if [[ -z "${uid}" || ! "${uid}" =~ ^[0-9]+$ ]]; then
            local uidKey
            if (( androidVersionCode >= 34 )); then
                uidKey="appId"
            else
                uidKey="userId"
            fi
            uid=$(adb -s "${deviceId}" shell dumpsys package "${packageName}" < /dev/null 2>/dev/null | awk -F'=' -v key="${uidKey}" '$0 ~ key {print $2; exit}' | awk '{print $1}' | tr -d '\r')
        fi

        if [[ -z "${uid}" || ! "${uid}" =~ ^[0-9]+$ ]]; then
            echo "❌ 无法解析该包的 UID，请检查包名或设备状态"
            return 1
        fi

        if (( androidVersionCode >= 31 )); then
            echo "📝 设备支持 uid 过滤，使用原生过滤的方式（UID: ${uid}）"
            adb -s "${deviceId}" logcat --uid "${uid}" < /dev/null
        else
            echo "📝 设备不支持 uid 过滤，使用文本过滤的方式（UID: ${uid}）"
            adb -s "${deviceId}" logcat -v uid < /dev/null | grep -F " ${uid} "
        fi
        return
    fi

    local pid
    pid=$(adb -s "${deviceId}" shell ps < /dev/null 2>/dev/null | tr -d '\r' | awk -v pkg="${packageName}" '$NF ~ ("^" pkg "(:.*)?$") {print $2; exit}')
    if [[ -z "${pid}" || ! "${pid}" =~ ^[0-9]+$ ]]; then
        pid=$(adb -s "${deviceId}" shell ps -A < /dev/null 2>/dev/null | tr -d '\r' | awk -v pkg="${packageName}" '$NF ~ ("^" pkg "(:.*)?$") {print $2; exit}')
    fi
    if [[ -z "${pid}" || ! "${pid}" =~ ^[0-9]+$ ]]; then
        echo "❌ 无法解析该应用的 PID，请检查 ${packageName} 应用是否正在运行"
        return 1
    fi
    echo "📝 低版本设备，使用 pid 文本过滤的方式（PID: ${pid}）"
    adb -s "${deviceId}" logcat -v threadtime < /dev/null | awk -v pid="${pid}" '$3==pid'
}

displayLogcatForDevice() {
    local deviceId
    deviceId="$(inputSingleAdbDevice)"
    echo "是否在显示 Logcat 前清除日志以避免输出过多？（y/n），留空则不清除"
    while true; do
        read -r cleanConfirm
        if [[ -z "${cleanConfirm}" ]]; then
            break
        elif [[ "${cleanConfirm}" =~ ^[yY]$ ]]; then
            adb -s "${deviceId}" logcat -c < /dev/null
            break
        elif [[ "${cleanConfirm}" =~ ^[nN]$ ]]; then
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