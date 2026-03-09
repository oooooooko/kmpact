#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : ANR 文件导出脚本（按设备生成 bugreport 或 traces）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../common/SystemPlatform.sh" && \
source "../common/EnvironmentTools.sh" && \
source "../business/DevicesSelector.sh" && \
source "../common/FileTools.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

waitUserInputParameter() {
    workDirPath=$(getWorkDirPath)
    echo "当前工作目录为：${workDirPath}"
    echo "请输入 ANR 日志的导出目录（可空，默认当前目录）："
    read -r exportDirPath
    exportDirPath=$(parseComputerFilePath "${exportDirPath}")

    if [[ -z "${exportDirPath}" ]]; then
        exportDirPath="${workDirPath}"
    fi
    mkdir -p "${exportDirPath}"
}

exportAnrFileSingleDevice() {
    local deviceId=$1
    local androidVersionCode
    androidVersionCode=$(getAndroidVersionCodeByAdb "${deviceId}")
    local phoneBrand
    phoneBrand=$(getDeviceBrandByAdb "${deviceId}")
    local phoneModel
    phoneModel=$(getDeviceModelByAdb "${deviceId}")

    local baseFileName
    baseFileName="${phoneBrand}_${phoneModel}_$(date "+%Y%m%d%H%M%S")"
    anrZipFileName="Bugreport_${baseFileName}.zip"
    anrTxtFileName="ANRTraces_${baseFileName}.txt"

    echo "⏳ 正在导出 ANR 日志，过程可能会比较慢，请耐心等待 5 ~ 10 分钟"
    if (( androidVersionCode >= 24 )); then
        local anrZipFilePath
        local anrTxtFilePath
        anrZipFilePath="${exportDirPath}$(getFileSeparator)${anrZipFileName}"
        anrTxtFilePath="${exportDirPath}$(getFileSeparator)${anrTxtFileName}"
        anrTargetFilePath="${anrZipFilePath}"
        MSYS_NO_PATHCONV=1 adb -s "${deviceId}" bugreport "${anrZipFilePath}" < /dev/null
        if [[ -f ${anrZipFilePath} ]]; then
            local anrEntryFilePath
            anrEntryFilePath=$(unzip -Z -1 "${anrZipFilePath}" < /dev/null 2>/dev/null | grep -E "/anr/[^/]+$" | tail -n 1)
            if [[ -n "${anrEntryFilePath}" ]]; then
                echo "⏳ 导出成功，正在从 ${anrZipFileName} 解压 ANR 日志文件，请稍候..."
                unzip -p "${anrZipFilePath}" "${anrEntryFilePath}" > "${anrTxtFilePath}"
                if [[ -s "${anrTxtFilePath}" ]]; then
                    echo "🧹 解压成功，正在删除 ${anrZipFileName} 压缩包文件"
                    rm -f "${anrZipFilePath}"
                    anrTargetFilePath="${anrTxtFilePath}"
                else
                    echo "💡 解压失败，保留原始 ${anrZipFileName} 压缩包文件"
                fi
            fi
        fi
    else
        anrTargetFilePath="${exportDirPath}$(getFileSeparator)${anrTxtFileName}"
        MSYS_NO_PATHCONV=1 adb -s "${deviceId}" pull "/data/anr/traces.txt" "${anrTargetFilePath}" < /dev/null
    fi
    if [[ -f ${anrTargetFilePath} ]]; then
        echo "✅ [${deviceId}] 设备 ANR 日志导出成功，存放路径为：${anrTargetFilePath}"
    else
        echo "❌ [${deviceId}] 设备 ANR 日志导出失败"
    fi
}

exportAnrForDevice() {
    local deviceId
    deviceId="$(inputSingleAdbDevice)"
    exportAnrFileSingleDevice "${deviceId}"
    return $?
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    waitUserInputParameter
    exportAnrForDevice
}

clear
main