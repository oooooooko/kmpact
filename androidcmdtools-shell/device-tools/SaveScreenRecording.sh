#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 录屏保存脚本（录制并保存设备屏幕视频）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../common/SystemPlatform.sh" && \
source "../common/EnvironmentTools.sh" && \
source "../common/FileTools.sh" && \
source "../business/DevicesSelector.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

waitUserInputParameter() {
    workDirPath=$(getWorkDirPath)
    echo "当前工作目录为：${workDirPath}"
    recordFileName="ScreenRecord_$(date "+%Y%m%d%H%M%S").mp4"
    recordSourceFilePath="/sdcard/${recordFileName}"
    echo "请输入录屏导出目录（可空，默认当前目录）："
    read -r  recordTargetDirPath
    recordTargetDirPath=$(parseComputerFilePath "${recordTargetDirPath}")
    if [[ -z "${recordTargetDirPath}" ]]; then
        recordTargetDirPath="${workDirPath}"
    fi
    recordTargetFilePath="${recordTargetDirPath}$(getFileSeparator)${recordFileName}"
}

startRemoteRecord() {
    local deviceId=$1
    local outputPrint
    MSYS_NO_PATHCONV=1 adb -s "${deviceId}" shell rm -f "${recordSourceFilePath}" < /dev/null > /dev/null 2>&1

    local recordPid
    recordPid=$(MSYS_NO_PATHCONV=1 adb -s "${deviceId}" shell "sh -c 'screenrecord \"${recordSourceFilePath}\" < /dev/null > /dev/null 2>&1 & echo \\$!'" 2>/dev/null | tr -d '\r')
    if [[ -z "${recordPid}" ]]; then
        recordPid=$(adb -s "${deviceId}" shell "pidof screenrecord" < /dev/null 2>/dev/null | tr -d '\r')
    fi

    if [[ -z "${recordPid}" ]]; then
        recordPid=$(adb -s "${deviceId}" shell "ps < /dev/null | grep -E 'screenrecord( |$)' | awk '{print \$2}' | head -n 1" < /dev/null 2>/dev/null | tr -d '\r')
    fi

    if [[ -z "${recordPid}" ]]; then
        echo "无法启动录屏进程"
        return 1
    fi

    echo "${recordPid}"
    return 0
}

stopRemoteRecord() {
    local deviceId=$1
    local recordPid=$2

    if [[ -z "${recordPid}" ]]; then
        return
    fi

    outputPrint=$(adb -s "${deviceId}" shell kill -2 "${recordPid}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备无法停止录屏操作，原因如下："
        echo "${outputPrint}"
        exit 1
    fi
    for i in {1..40}; do
        alive=$(adb -s "${deviceId}" shell "[ -d /proc/${recordPid} ] < /dev/null && echo 1 || echo 0" < /dev/null 2>/dev/null | tr -d '\r')
        if [[ "${alive}" == "0" ]]; then
            break
        fi
        printf "."
        sleep 0.25
    done
    # 打印一个空内容以进行换行
    echo ""
}

finishRecord() {
    local deviceId=$1
    echo "为确保设备端文件稳定，等待 1 秒后开始导出..."
    sleep 1
    echo "开始从设备拉取到电脑，较大的文件需要一些时间，请耐心等待..."
    local outputPrint
    outputPrint=$(MSYS_NO_PATHCONV=1 adb -s "${deviceId}" pull "${recordSourceFilePath}" "${recordTargetFilePath}" < /dev/null 2>&1)
    local exitCode=$?
    adb -s "${deviceId}" shell rm -f "${recordSourceFilePath}" < /dev/null > /dev/null 2>&1
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备的录屏导出 ${recordTargetFilePath} 到电脑失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi

    if [[ ! -f "${recordTargetFilePath}" ]]; then
        echo "❌ [${deviceId}] 设备的录屏导出到电脑成功，但是在电脑上面找不到 ${recordTargetFilePath} 文件"
        return 1
    fi

    echo "✅ [${deviceId}] 设备的录屏已保存到电脑，存放路径为：${recordTargetFilePath}"
    return 0
}

doScreenRecordSingleDevice() {
    deviceId=$1
    trap "" INT
    local outputPrint
    outputPrint=$(startRemoteRecord "${deviceId}")
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备无法执行录屏操作，原因如下："
        echo "${outputPrint}"
        exit 1
    fi
    local recordPid=${outputPrint}
    echo "录屏进程 PID：${recordPid}"
    echo "录屏进行中，按回车键结束并保存"
    read -r < /dev/tty
    echo "正在停止录屏并保存文件..."
    stopRemoteRecord "${deviceId}" "${recordPid}"
    trap - INT
    finishRecord "${deviceId}"
}

doScreenshotForDevice() {
    local deviceId
    deviceId="$(inputSingleAdbDevice)"
    doScreenRecordSingleDevice "${deviceId}"
    exit 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    waitUserInputParameter
    doScreenshotForDevice
}

clear
main