#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 设备解锁状态读取脚本（fastboot 查询）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../../common/SystemPlatform.sh" && \
source "../../common/EnvironmentTools.sh" && \
source "../../business/DevicesSelector.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

readLockStatus() {
    local fastbootDeviceId=$1
    local outputPrint
    outputPrint=$(fastboot -s "${fastbootDeviceId}" oem device-info < /dev/null 2>&1)
    deviceUnlocked=$(echo "${outputPrint}" | grep -i "device unlocked" | awk -F': ' '{print $2}')
    echo "--------------------------"
    echo "📝 [${fastbootDeviceId}] 设备原始查询信息："
    echo "${outputPrint}"
    if [[ "${deviceUnlocked}" == "true" ]]; then
        echo "✅ [${fastbootDeviceId}] 设备锁状态：无锁"
    elif [[ "${deviceUnlocked}" == "false" ]]; then
        echo "❌ [${fastbootDeviceId}] 设备锁状态：有锁"
    else
        echo "👻 [${fastbootDeviceId}] 设备锁状态：无法识别"
    fi
}

readLockStatusForDevices() {
    deviceId="$(inputMultipleFastbootDevice)"

    fastbootDeviceIdsString=$(getFastbootDeviceIdsString)
    if [[ -n "${deviceId}" ]]; then
        if echo "${fastbootDeviceIdsString}" | grep -xFq "${deviceId}"; then
            readLockStatus "${deviceId}"
        fi
    else
        while read -r fastbootDeviceId; do
            readLockStatus "${fastbootDeviceId}"
        done < <(echo "${fastbootDeviceIdsString}" | tr -d '\r')
    fi
}

main() {
    printCurrentSystemType
    checkFastbootEnvironment
    readLockStatusForDevices
}

clear
main