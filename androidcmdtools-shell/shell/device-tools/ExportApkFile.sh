#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 应用 APK 导出脚本（从设备导出已安装应用）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "/../business/DevicesSelector.sh"
source "${scriptDirPath}/../business/DevicesSelector.sh"
[ -z "" ] || source "../common/FileTools.sh"
source "${scriptDirPath}/../common/FileTools.sh"

waitUserInputParameter() {
    workDirPath=$(getWorkDirPath)
    echo "当前工作目录为：${workDirPath}"
    echo "请输入要导出的应用包名（留空将导出所有已安装应用）："
    read -r targetPackageName
    if [[ -z "${targetPackageName}" ]]; then
        echo "是否导出系统应用？（y/n）："
        read -r includeSystemConfirm
        if [[ "${includeSystemConfirm}" == "y" || "${includeSystemConfirm}" == "Y" ]]; then
            includeSystemApps="true"
        elif [[ "${includeSystemConfirm}" == "n" || "${includeSystemConfirm}" == "N" ]]; then
            includeSystemApps="false"
        else
            echo "❌ 无效选择，已取消操作"
            exit 1
        fi
    fi
    echo "请输入 apk 导出目录（可空，默认当前目录）："
    read -r exportDirPath
    exportDirPath=$(parseComputerFilePath "${exportDirPath}")

    if [[ -z "${exportDirPath}" ]]; then
        exportDirPath="${workDirPath}"
    fi
    mkdir -p "${exportDirPath}"
}

isPackageInstalled() {
    local deviceId=$1
    local packageName=$2
    local result
    result=$(adb -s "${deviceId}" shell pm path "${packageName}" < /dev/null 2>&1)
    echo "${result}" | grep -q "^package:"
}

exportSingleApk() {
    local deviceId=$1
    local packageName=$2
    if ! isPackageInstalled "${deviceId}" "${packageName}"; then
        echo "👻 [${deviceId}] 设备未安装 [${packageName}] 应用，已跳过"
        return 2
    fi
    local apkPathResult
    apkPathResult=$(adb -s "${deviceId}" shell pm path "${packageName}" < /dev/null 2>&1)
    if ! echo "${apkPathResult}" | grep -q "^package:"; then
        echo "❌ [${deviceId}] 设备获取 [${packageName}] 安装包路径失败"
        echo "${apkPathResult}"
        return 1
    fi
    local apkSourceFilePath=${apkPathResult//package:/}
    local versionName
    versionName=$(adb -s "${deviceId}" shell dumpsys package "${packageName}" < /dev/null 2>&1 | tr -d '\r' | sed -n 's/.*versionName=\([^ ]*\).*/\1/p' | head -n 1)
    local tempApkFilePath
    tempApkFilePath="${exportDirPath}$(getFileSeparator)${packageName}-${versionName}.apk"
    local outputPrint
    outputPrint=$(MSYS_NO_PATHCONV=1 adb -s "${deviceId}" pull "${apkSourceFilePath}" "${tempApkFilePath}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备导出 [${packageName}] 失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
    if [[ -f ${tempApkFilePath} ]]; then
        local appLabel=""
        if existCommand "aapt"; then
            local localeKeys=("application-label-zh-CN" "application-label-zh" "application-label-zh-Hans" "application-label-zh-Hant" "application-label-zh-rCN" "application-label" "application-label-en")
            for key in "${localeKeys[@]}"; do
                local label
                label=$(aapt dump badging "${tempApkFilePath}" < /dev/null 2>/dev/null | sed -n "s/.*${key}:'\\([^']*\\)'.*/\\1/p" | head -n 1)
                if [[ -n "${label}" ]]; then
                    appLabel="${label}"
                    break
                fi
            done
        fi
        if [[ -z "${appLabel}" ]]; then
            appLabel="${packageName}"
        fi
        local safeLabel
        safeLabel=$(echo "${appLabel}" | tr -d '\r' | sed 's/[\\/:*?"<>|]/_/g' | sed 's/[[:space:]]\{1,\}/ /g' | sed 's/^ *//;s/ *$//')
        if [[ -n "${versionName}" ]]; then
            apkTargetFilePath="${exportDirPath}$(getFileSeparator)${safeLabel} ${versionName}.apk"
        else
            apkTargetFilePath="${exportDirPath}$(getFileSeparator)${safeLabel}.apk"
        fi
        mv -f "${tempApkFilePath}" "${apkTargetFilePath}"
        echo "✅ [${deviceId}] 设备导出 [${safeLabel}] 成功，保存至：${apkTargetFilePath}"
        return 0
    else
        echo "❌ [${deviceId}] 设备已拉取 [${packageName}]，但保存到电脑失败：${tempApkFilePath}"
        return 1
    fi
}

exportMultipleApk() {
    local deviceId=$1
    local exportPackagesNameList=()
    if [[ -n "${targetPackageName}" ]]; then
        exportPackagesNameList+=("${targetPackageName}")
    else
        if [[ "${includeSystemApps}" == "true" ]]; then
            while IFS= read -r packageName; do
                [[ -n "${packageName}" ]] && exportPackagesNameList+=("${packageName}")
            done < <(adb -s "${deviceId}" shell pm list packages < /dev/null 2>/dev/null | tr -d '\r' | sed 's/^package://')
        else
            while IFS= read -r packageName; do
                [[ -n "${packageName}" ]] && exportPackagesNameList+=("${packageName}")
            done < <(adb -s "${deviceId}" shell pm list packages -3 < /dev/null 2>/dev/null | tr -d '\r' | sed 's/^package://')
        fi
    fi
    if (( ${#exportPackagesNameList[@]} == 0 )); then
        echo "❌ [${deviceId}] 设备未找到可导出的应用"
        return 1
    fi
    local successCount=0
    local failCount=0
    local skipCount=0
    for packageName in "${exportPackagesNameList[@]}"; do
        exportSingleApk "${deviceId}" "${packageName}"
        local exitCode=$?
        if (( exitCode == 0 )); then
            ((successCount++))
        elif (( exitCode == 2 )); then
            ((skipCount++))
        else
            ((failCount++))
        fi
    done
    if (( ${#exportPackagesNameList[@]} > 1 )); then
        echo "📋 [${deviceId}] 设备导出任务完成，成功 ${successCount} 个，跳过 ${skipCount} 个，失败 ${failCount} 个"
    fi
    return 0
}

exportApkToDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    local pids=()
    if [[ -n "${deviceId}" ]]; then
        exportMultipleApk "${deviceId}" &
        pids+=($!)
    else
        echo "⏳ 正在并行向多台设备导出..."
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            exportMultipleApk "${adbDeviceId}" &
            pids+=($!)
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
    for pid in "${pids[@]}"; do
        wait "${pid}"
    done
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    waitUserInputParameter
    exportApkToDevice
}

clear
main