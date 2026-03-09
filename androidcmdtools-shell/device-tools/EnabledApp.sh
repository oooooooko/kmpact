#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 应用状态恢复脚本（恢复禁用应用）
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
    echo "请输入要解冻应用包名（可输入多个，空格分隔）："
    read -r packageNameInput
    read -r -a inputPackageNameList <<< "${packageNameInput}"
    packageNameList=()
    for inputPackageName in "${inputPackageNameList[@]}"; do
        if [[ -z "${inputPackageName}" ]]; then continue; fi
        if [[ ! "${inputPackageName}" =~ ^[A-Za-z0-9_]+(\.[A-Za-z0-9_]+)*$ ]]; then
            echo "👻 检测到非法包名：${inputPackageName}，将跳过解冻此包名"
            continue
        fi
        local repeatInput="false"
        for alreadyHavePackageName in "${packageNameList[@]}"; do
            if [[ "${alreadyHavePackageName}" -eq "${inputPackageName}" ]]; then
                repeatInput="true"
                break
            fi
        done
        if [[ "${repeatInput}" == "false" ]]; then
            packageNameList+=("${inputPackageName}")
        fi
    done

    if (( ${#packageNameList[@]} == 0 )); then
        echo "❌ 未检测到有效包名，解冻中止"
        exit 1
    fi
}

enabledSingleApp() {
    local deviceId=$1
    local packageName=$2
    local installedPath
    installedPath=$(adb -s "${deviceId}" shell pm path "${packageName}" < /dev/null 2>&1 | tr -d '\r')
    if [[ -z "${installedPath}" ]]; then
        echo "💡 [${deviceId}] 设备未安装 ${packageName} 应用，跳过解冻"
        return 2
    fi
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" shell pm enable "${packageName}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备解冻应用 ${packageName} 成功"
        return 0
    else
        echo "❌ [${deviceId}] 设备解冻应用 ${packageName} 解冻失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

enabledMultipleApp() {
    local deviceId=$1
    local successCount=0
    local failCount=0
    local skipCount=0
    for packageName in "${packageNameList[@]}"; do
        enabledSingleApp "${deviceId}" "${packageName}"
        local exitCode=$?
        if (( exitCode == 0 )); then
            ((successCount++))
        elif (( exitCode == 1 )); then
            ((failCount++))
        elif (( exitCode == 2 )); then
            ((skipCount++))
        fi
    done

    if (( ${#packageNameList[@]} > 1 )); then
        echo "📋 [${deviceId}] 设备解冻任务完成，成功 ${successCount} 个，失败 ${failCount} 个，跳过 ${skipCount} 个"
    fi
    return 0
}

enabledAppForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    local pids=()
    if [[ -n "${deviceId}" ]]; then
        enabledMultipleApp "${deviceId}" &
        pids+=($!)
    else
        echo "⏳ 开始并行向多台设备解冻..."
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            enabledMultipleApp "${adbDeviceId}" &
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
    enabledAppForDevice
}

clear
main