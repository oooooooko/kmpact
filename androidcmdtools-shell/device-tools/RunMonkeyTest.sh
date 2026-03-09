#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Monkey 压测脚本（执行随机事件）
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
    echo "请输入要测试的应用包名："
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

    echo "请输入 MonkeyTest 测试时长（单位分钟，留空则测试 5 分钟）："
    read -r testMinutes
    if [[ -z "${testMinutes}" ]]; then
        testMinutes=5
    fi

    if [[ ! "${testMinutes}" =~ ^[0-9]+$ ]]; then
        echo "❌ 测试时长必须为纯数字"
        exit 1
    fi

    if ((testMinutes < 1)); then
        echo "❌ 测试时长必须大于 1 分钟"
        exit 1
    fi
}

startMonkeyTest() {
    local deviceId=$1
    local throttleMs=100
    local eventCount=$((testMinutes * 60 * 1000 / throttleMs))

    local outputPrint
    outputPrint=$(adb -s "${deviceId}" shell monkey -v -p "${packageName}" --throttle "${throttleMs}" "${eventCount}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备 ${packageName} MonkeyTest 任务完成"
        return 0
    else
        echo "❌ [${deviceId}] 设备 ${packageName} MonkeyTest 任务失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

stopMonkeyTest() {
    local deviceId=$1
    adb -s "${deviceId}" shell killall -9 com.android.commands.monkey < /dev/null > /dev/null 2>&1 || true
    adb -s "${deviceId}" shell killall -9 monkey < /dev/null > /dev/null 2>&1 || true

    remotePs=$(adb -s "${deviceId}" shell ps -A < /dev/null 2>/dev/null)
    if [[ -z "${remotePs}" ]]; then
        remotePs=$(adb -s "${deviceId}" shell ps -ef < /dev/null 2>/dev/null)
    fi
    if [[ -z "${remotePs}" ]]; then
        remotePs=$(adb -s "${deviceId}" shell ps < /dev/null 2>/dev/null)
    fi
    if [[ -z "${remotePs}" ]]; then
        return 0
    fi
    killPids=$(echo "${remotePs}" | tr -d '\r' | tr -s ' ' | awk '$NF ~ /com\.android\.commands\.monkey|monkey/ {pid=$2; if(pid !~ /^[0-9]+$/) pid=$1; if(pid ~ /^[0-9]+$/) print pid}')
    for remotePid in ${killPids}; do
        adb -s "${deviceId}" shell kill -9 "${remotePid}" < /dev/null > /dev/null 2>&1 || true
    done
}

runMonkeyTestForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    local adbDeviceList=()
    local pids=()
    if [[ -n "${deviceId}" ]]; then
        adbDeviceList+=("${deviceId}")
        startMonkeyTest "${deviceId}" &
        pids+=($!)
    else
        for adbDeviceId in $(getAdbDeviceIdsString); do
            if [[ -z "${adbDeviceId}" ]]; then
                continue
            fi
            adbDeviceList+=("${adbDeviceId}")
            startMonkeyTest "${adbDeviceId}" &
            pids+=($!)
        done
    fi

    echo "📋 按下回车键可提前结束所有 MonkeyTest 任务"
    local interrupted="false"
    while true; do
        local anyRunning="false"
        for pid in "${pids[@]}"; do
            if kill -0 "${pid}" 2>/dev/null; then
                anyRunning="true"
            fi
        done
        if [[ "${anyRunning}" == "false" ]]; then
            break
        fi
        if read -r -t 1 _; then
            interrupted="true"
            break
        fi
    done

    if [[ "${interrupted}" == "true" ]]; then
        for pid in "${pids[@]}"; do
            kill "${pid}" 2>/dev/null || true
        done
        for adbDeviceId in "${adbDeviceList[@]}"; do
            stopMonkeyTest "${adbDeviceId}"
        done
        echo "✅ 已提前结束 MonkeyTest 任务"
    else
        echo "✅ 所有设备 ${packageName} MonkeyTest 任务完成"
    fi
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    waitUserInputParameter
    runMonkeyTestForDevice
}

clear
main