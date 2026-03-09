#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 截图保存脚本（保存当前设备屏幕截图）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../common/SystemPlatform.sh" && \
source "../common/FileTools.sh" && \
source "../common/PasteTools.sh" && \
source "../business/DevicesSelector.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

waitUserInputParameter() {
    workDirPath=$(getWorkDirPath)
    echo "当前目录为：${workDirPath}"
    screenshotFileName="Screenshot_$(date "+%Y%m%d%H%M%S").png"
    screenshotSourceFilePath="/sdcard/${screenshotFileName}"

    echo "🤔 请选择你的操作："
    echo "1. 复制手机截图到剪贴板"
    echo "2. 保存手机截图到电脑中"
    read -r screenshotActionChoice
    copyScreenshotToPaste="false"
    if [[ ${screenshotActionChoice} == "1" || -z ${screenshotActionChoice} ]]; then
        screenshotTargetFilePath="${workDirPath}$(getFileSeparator)${screenshotFileName}"
        copyScreenshotToPaste="true"
    elif [[ "${screenshotActionChoice}" == "2" ]]; then
        copyScreenshotToPaste="false"
        echo "请输入截图导出目录（可空，默认当前目录）："
        read -r screenshotTargetDirPath
        screenshotTargetDirPath=$(parseComputerFilePath "${screenshotTargetDirPath}")

        if [[ -z "${screenshotTargetDirPath}" ]]; then
            screenshotTargetDirPath="${workDirPath}"
        fi
        mkdir -p "${screenshotTargetDirPath}"
        screenshotTargetFilePath="${screenshotTargetDirPath}$(getFileSeparator)${screenshotFileName}"
        echo "截图保存在电脑上的文件路径：${screenshotTargetFilePath}"
    else
        echo "❌ 无效选择，已取消截图"
        exit 1
    fi
}

doScreenshotSingleDevice() {
    local deviceId=$1
    local outputPrint

    echo "截图保存在手机上面的路径：${screenshotSourceFilePath}"
    outputPrint=$(MSYS_NO_PATHCONV=1 adb -s "${deviceId}" shell screencap -p "${screenshotSourceFilePath}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备截图失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi

    outputPrint=$(MSYS_NO_PATHCONV=1 adb -s "${deviceId}" pull "${screenshotSourceFilePath}" "${screenshotTargetFilePath}" < /dev/null 2>&1)
    exitCode=$?
    adb -s "${deviceId}" shell rm "'${screenshotSourceFilePath}'" < /dev/null > /dev/null 2>&1
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备截图导出到电脑失败：${screenshotTargetFilePath}，原因如下："
        echo "${outputPrint}"
        return 1
    fi

    if [[ ! -f "${screenshotTargetFilePath}" ]]; then
        echo "❌ [${deviceId}] 设备截图导出到电脑成功，但是在电脑上面找不到这个文件：${screenshotTargetFilePath}"
        return 1
    fi

    if [[ ${copyScreenshotToPaste} == "false" ]]; then
        echo "✅ [${deviceId}] 设备截图成功，存放路径为：${screenshotTargetFilePath}"
        return 0
    fi

    copyPictureFileToPaste "${screenshotTargetFilePath}"
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备截图成功，截图内容已复制到剪贴板"
        rm -f "${screenshotTargetFilePath}"
        return 0
    else
        echo "👻 [${deviceId}] 设备截图成功，但是复制到剪贴板失败，你可以手动复制该截图：${screenshotTargetFilePath}"
        return 0
    fi
}

doScreenshotForDevice() {
    local deviceId
    deviceId="$(inputSingleAdbDevice)"
    doScreenshotSingleDevice "${deviceId}"
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