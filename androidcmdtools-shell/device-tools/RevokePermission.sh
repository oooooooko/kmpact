#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : adb 撤销权限脚本
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
    echo "请输入要撤销权限的应用包名："
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

    echo "请输入要撤销的权限（空格分隔，回车则撤销在 AndroidManifest.xml 中注册的权限）："
    read -r permissionNameInput
    read -r -a inputPermissionNameList <<< "${permissionNameInput}"
    permissionNameList=()
    for inputPermissionName in "${inputPermissionNameList[@]}"; do
        if [[ -z "${inputPermissionName}" ]]; then continue; fi
        if [[ ! "${inputPermissionName}" =~ ^[A-Za-z0-9_]+\.[A-Za-z0-9_]+(\.[A-Za-z0-9_]+)*$ ]] || [[ ! "${inputPermissionName}" =~ ^[A-Za-z0-9_]+$ ]]; then
            echo "👻 检测到非法权限名：${inputPermissionName}，将跳过撤销此权限"
            continue
        fi
        local repeatInput="false"
        for alreadyHavePermissionName in "${permissionNameList[@]}"; do
            if [[ "${alreadyHavePermissionName}" == "${inputPermissionName}" ]]; then
                repeatInput="true"
                break
            fi
        done
        if [[ "${repeatInput}" == "false" ]]; then
            permissionNameList+=("${inputPermissionName}")
        fi
    done
}

isApplicationInstalled() {
    local deviceId=$1
    local packageName=$2
    if adb -s "${deviceId}" shell pm path "${packageName}" < /dev/null > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

getAndroidManifestPermissions() {
    local deviceId=$1
    local packageName=$2
    local rawPermissions
    local purePermissions
    rawPermissions=$(adb -s "${deviceId}" shell dumpsys package "${packageName}" < /dev/null 2>/dev/null)
    purePermissions=$(echo "${rawPermissions}" | \
    sed -n '/requested permissions:/,/install permissions:/p' | \
    grep -oE "[a-zA-Z0-9_\.]+\.[a-zA-Z0-9_\.]+" | \
    tr -d '\r' | sort -u)
    echo "${purePermissions}"
}

isStandardPermissionName() {
    local name=$1
    if [[ "${name}" =~ ^[A-Za-z0-9_]+\.[A-Za-z0-9_]+(\.[A-Za-z0-9_]+)*$ ]]; then
        return 0
    fi
    return 1
}

isDangerousPermission() {
    local deviceId=$1
    local permissionName=$2
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" shell pm list permissions -g -d < /dev/null 2>/dev/null | tr -d '\r')
    echo "${outputPrint}" | grep -Fq "permission:${permissionName}"
    return $?
}

isGrantDangerousPermission() {
    local deviceId=$1
    local packageName=$2
    local permissionName=$3
    local permissionInfo
    permissionInfo=$(adb -s "${deviceId}" shell dumpsys package "${packageName}" 2>/dev/null | grep -E "^[[:space:]]*${permissionName}: granted=" | tr -d '\r')
    if [[ -n "${permissionInfo}" ]] && echo "${permissionInfo}" | grep -qi "granted=true"; then
        return 0
    fi
    return 1
}

setRevokeDangerousPermission() {
    local deviceId=$1
    local packageName=$2
    local permissionName=$3
    adb -s "${deviceId}" shell pm revoke "${packageName}" "${permissionName}" < /dev/null 2>&1
    return $?
}

runAppOpsCmd() {
    local deviceId=$1
    shift
    local subCmd=("$@")
    if adb -s "${deviceId}" shell cmd -l < /dev/null 2>/dev/null | tr -d '\r' | grep -Eiq '(^|[[:space:]])appops([[:space:]]|$)'; then
        adb -s "${deviceId}" shell cmd appops "${subCmd[@]}" < /dev/null 2>&1
        return $?
    else
        adb -s "${deviceId}" shell appops "${subCmd[@]}" < /dev/null 2>&1
        return $?
    fi
}

resolveAppOpsOpName() {
    local deviceId=$1
    local packageName=$2
    local inputName=$3
    local baseName="${inputName}"
    if isStandardPermissionName "${baseName}"; then
        local derived
        derived=$(echo "${baseName}" | sed -E 's/^android\.permission\.//; s/\./_/g' | tr '[:lower:]' '[:upper:]')
        local outputPrint
        outputPrint=$(runAppOpsCmd "${deviceId}" get "${packageName}" "${derived}")
        if [[ -n "${outputPrint}" ]] && ! echo "${outputPrint}" | grep -qiE 'Unknown operation string|unknown command'; then
            echo "${derived}"
            return 0
        fi
        case "${baseName}" in
            android.permission.PACKAGE_USAGE_STATS) echo "GET_USAGE_STATS"; return 0 ;;
            android.permission.POST_NOTIFICATIONS) echo "POST_NOTIFICATION"; return 0 ;;
        esac
        echo ""
        return 1
    else
        local opName="${baseName}"
        local outputPrint
        outputPrint=$(runAppOpsCmd "${deviceId}" get "${packageName}" "${opName}")
        if echo "${outputPrint}" | grep -qiE 'Unknown operation string|unknown command'; then
            echo ""
            return 1
        fi
        if [[ -n "${outputPrint}" ]]; then
            echo "${opName}"
            return 0
        fi
        echo ""
        return 1
    fi
}

isAppOpPermissionAllowed() {
    local deviceId=$1
    local packageName=$2
    local opName=$3
    local outputPrint
    outputPrint=$(runAppOpsCmd "${deviceId}" get "${packageName}" "${opName}" | tr -d '\r')
    if echo "${outputPrint}" | grep -qiE 'Unknown operation string|unknown command'; then
        return 2
    fi
    if echo "${outputPrint}" | grep -qiE '(allow|allowed)'; then
        return 0
    fi
    return 1
}

setAppOpPermissionDeny() {
    local deviceId=$1
    local packageName=$2
    local opName=$3
    runAppOpsCmd "${deviceId}" set "${packageName}" "${opName}" deny
    return $?
}

revokePermission() {
    local deviceId=$1
    local successCount=0
    local failCount=0
    local abnormalCount=0

    if ! isApplicationInstalled "${deviceId}" "${packageName}"; then
        echo "❌ [${deviceId}] 设备未安装 [${packageName}] 应用，无法进行撤销操作"
        return 0
    fi

    if (( ${#permissionNameList[@]} == 0 )); then
        while IFS= read -r permissionLine; do
            if [[ -n "${permissionLine}" ]]; then
                permissionNameList+=("${permissionLine}")
            fi
        done < <(getAndroidManifestPermissions "${deviceId}" "${packageName}")
    fi

    if (( ${#permissionNameList[@]} == 0 )); then
        echo "❌ [${deviceId}] 设备 ${packageName} 要撤销的权限列表为空，无法进下一步操作"
        return 0
    fi

    for permissionName in "${permissionNameList[@]}"; do
        revokedPrompt="✅ [${deviceId}] 设备 [${permissionName}] 权限撤销成功"
        duplicateRevokedPrompt="✅ [${deviceId}] 设备 [${permissionName}] 权限没有授权，无需撤销"
        unregisteredPermissionPrompt="❌ [${deviceId}] 设备 [${permissionName}] 权限撤销失败，该权限未在 AndroidManifest.xml 文件中声明"
        unknownPermissionPrompt="👻 [${deviceId}] 设备 [${permissionName}] 权限撤销异常，可能是高版本系统的权限、权限名称拼写错误、无法用 adb 操作该权限"
        notChangeablePermissionPrompt="❌ [${deviceId}] 设备 [${permissionName}] 权限撤销失败，该权限不能通过 adb 命令撤销操作"
        unknownPackagePrompt="❌ [${deviceId}] 设备 [${permissionName}] 权限撤销失败，应用未安装或包名错误"
        otherFailPrompt="❌ [${deviceId}] 设备 [${permissionName}] 权限撤销失败，原因如下："

        if isStandardPermissionName "${permissionName}"; then
            if isDangerousPermission "${deviceId}" "${permissionName}"; then
                if ! isGrantDangerousPermission "${deviceId}" "${packageName}" "${permissionName}"; then
                    echo "${duplicateRevokedPrompt}"
                    ((successCount++))
                    continue
                fi
                local outputPrint
                outputPrint=$(setRevokeDangerousPermission "${deviceId}" "${packageName}" "${permissionName}")
                local exitCode=$?
                if (( exitCode == 0 )); then
                    echo "${revokedPrompt}"
                    ((successCount++))
                elif echo "${outputPrint}" | grep -qi "not granted"; then
                    echo "${duplicateRevokedPrompt}"
                    ((successCount++))
                elif echo "${outputPrint}" | grep -qi "has not requested permission"; then
                    echo "${unregisteredPermissionPrompt}"
                    ((failCount++))
                elif echo "${outputPrint}" | grep -qi "unknown permission"; then
                    echo "${unknownPermissionPrompt}"
                    ((abnormalCount++))
                elif echo "${outputPrint}" | grep -Fqi "not a changeable permission type"; then
                    echo "${notChangeablePermissionPrompt}"
                    ((failCount++))
                elif echo "${outputPrint}" | grep -qi "unknown package"; then
                    echo "${unknownPackagePrompt}"
                    ((failCount++))
                else
                    echo "${otherFailPrompt}"
                    echo "${outputPrint}"
                    ((failCount++))
                fi
                continue
            fi
        fi

        local opName
        opName=$(resolveAppOpsOpName "${deviceId}" "${packageName}" "${permissionName}")
        if [[ -z "${opName}" ]]; then
            opName=$(echo "${permissionName}" | sed -E 's/^android\.permission\.//; s/\./_/g' | tr '[:lower:]' '[:upper:]')
        fi
        isAppOpPermissionAllowed "${deviceId}" "${packageName}" "${opName}"
        local exitCode=$?
        if (( exitCode == 0 )); then
            local outputPrint
            outputPrint=$(setAppOpPermissionDeny "${deviceId}" "${packageName}" "${opName}")
            local exitCode=$?
            if (( exitCode == 0 )); then
                echo "${revokedPrompt}"
                ((successCount++))
            else
                echo "${otherFailPrompt}"
                echo "${outputPrint}"
                ((failCount++))
            fi
        elif (( exitCode == 1 )); then
            echo "${duplicateRevokedPrompt}"
            ((successCount++))
            continue
        elif (( exitCode == 2 )); then
            echo "${unknownPermissionPrompt}"
            ((abnormalCount++))
            continue
        fi
        continue
    done

    if (( ${#permissionNameList[@]} > 1 )); then
        echo "📋 [${deviceId}] 设备撤销权限任务完成，成功 ${successCount} 个，失败 ${failCount} 个，异常 ${abnormalCount} 个"
    fi

    if (( failCount > 1 )); then
        echo "💡 温馨提醒：某些手机用 adb 撤销权限成功后在权限设置页看到的是已授权的状态是正常的，可以忽略此现象"
    fi
    return 0
}

revokePermissionForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    local pids=()
    if [[ -n "${deviceId}" ]]; then
        revokePermission "${deviceId}" &
        pids+=($!)
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            revokePermission "${adbDeviceId}" &
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
    revokePermissionForDevice
}

clear
main