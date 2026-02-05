#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : adb 冻结脚本（支持多包名冻结和多设备并行冻结）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "/../business/DevicesSelector.sh"
source "${scriptDirPath}/../business/DevicesSelector.sh"

waitUserInputParameter() {
    echo "请输入要冻结应用包名（可输入多个，空格分隔）："
    read -r packageNameInput
    read -r -a inputPackageNameList <<< "${packageNameInput}"
    packageNameList=()
    for inputPackageName in "${inputPackageNameList[@]}"; do
        if [[ -z "${inputPackageName}" ]]; then continue; fi
        if [[ ! "${inputPackageName}" =~ ^[A-Za-z0-9_]+(\.[A-Za-z0-9_]+)*$ ]]; then
            echo "👻 检测到非法包名：${inputPackageName}，将跳过冻结此包名"
            continue
        fi
        local repeatInput="false"
        for alreadyHavePackageName in "${packageNameList[@]}"; do
            if [[ "${alreadyHavePackageName}" == "${inputPackageName}" ]]; then
                repeatInput="true"
                break
            fi
        done
        if [[ "${repeatInput}" == "false" ]]; then
            packageNameList+=("${inputPackageName}")
        fi
    done


    if (( ${#packageNameList[@]} == 0 )); then
        echo "❌ 未检测到有效包名，冻结中止"
        exit 1
    fi
}

disabledSingleApp() {
    local deviceId=$1
    local packageName=$2
    local installedPath
    installedPath=$(adb -s "${deviceId}" shell pm path "${packageName}" < /dev/null 2>&1 | tr -d '\r')
    if [[ -z "${installedPath}" ]]; then
        echo "💡 [${deviceId}] 设备未安装 ${packageName} 应用，跳过冻结"
        return 2
    fi
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" shell pm disable-user "${packageName}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        echo "✅ [${deviceId}] 设备冻结应用 ${packageName} 成功"
        return 0
    else
        echo "❌ [${deviceId}] 设备冻结应用 ${packageName} 冻结失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

disabledMultipleApp() {
    local deviceId=$1
    local successCount=0
    local failCount=0
    local skipCount=0
    for packageName in "${packageNameList[@]}"; do
        disabledSingleApp "${deviceId}" "${packageName}"
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
        echo "📋 [${deviceId}] 设备冻结任务完成，成功 ${successCount} 个，失败 ${failCount} 个，跳过 ${skipCount} 个"
    fi
    return 0
}

disabledAppForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    local pids=()
    if [[ -n "${deviceId}" ]]; then
        disabledMultipleApp "${deviceId}" &
        pids+=($!)
    else
        echo "⏳ 开始并行向多台设备冻结..."
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            disabledMultipleApp "${adbDeviceId}" &
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
    disabledAppForDevice
}

clear
main