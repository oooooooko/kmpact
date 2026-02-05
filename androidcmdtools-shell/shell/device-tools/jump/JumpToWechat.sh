#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 设备跳转微信脚本（启动微信主界面）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../business/DevicesSelector.sh"
source "${scriptDirPath}/../../business/DevicesSelector.sh"

WECAHT_PACKNAME="com.tencent.mm"

isWeChatInstalled() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" shell pm path com.tencent.mm < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )) && echo "${outputPrint}" | grep -qiE '^package:'; then
        return 0
    fi
    return 1
}

resolveWeChatLauncherComponent() {
    local deviceId=$1
    local componentName
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" shell pm resolve-activity -a android.intent.action.MAIN -c android.intent.category.LAUNCHER "${WECAHT_PACKNAME}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )); then
        componentName=$(echo "${outputPrint}" | tr -d '\r' | grep -E -o "com\.tencent\.mm/[^[:space:]]+" | head -n1)
        if [[ -n "${componentName}" ]]; then
            echo "${componentName}"
            return 0
        fi
        local name
        name=$(echo "${outputPrint}" | tr -d '\r' | grep -iE 'name=' | sed -n 's/.*name=//p' | head -n1)
        if [[ -n "${name}" ]]; then
            if [[ "${name}" == .* ]]; then
                echo "${WECAHT_PACKNAME}/${name}"
            else
                echo "${WECAHT_PACKNAME}/${name}"
            fi
            return 0
        fi
    fi
    return 1
}

jumpWeChatMainActivity() {
    local deviceId=$1
    if ! isWeChatInstalled "${deviceId}"; then
        echo "❌ [${deviceId}] 设备未安装微信（com.tencent.mm），已取消跳转"
        return 1
    fi
    local componentName
    if componentName=$(resolveWeChatLauncherComponent "${deviceId}"); then
        echo "⏳ 正在向 [${deviceId}] 设备发起跳转：${componentName}"
        local outputPrint
        outputPrint=$(adb -s "${deviceId}" shell am start -W -n "${componentName}" < /dev/null 2>&1)
        local exitCode=$?
        if (( exitCode == 0 )) && { echo "${outputPrint}" | grep -q -E 'Status:\s*ok'; } && { ! echo "${outputPrint}" | grep -qiE 'unable to resolve Intent|Activity not found|Permission denied|SecurityException|Error:'; }; then
            echo "✅ [${deviceId}] 设备跳转到微信主界面成功"
            return 0
        else
            echo "❌ [${deviceId}] 设备跳转到微信主界面失败，原因如下："
            echo "${outputPrint}"
            return 1
        fi
    else
        echo "⏳ [${deviceId}] 设备无法解析到微信入口，改用桌面入口拉起应用"
        local outputPrint
        outputPrint=$(adb -s "${deviceId}" shell monkey -p com.tencent.mm -c android.intent.category.LAUNCHER 1 < /dev/null 2>&1)
        local exitCode=$?
        if (( exitCode == 0 )) && { echo "${outputPrint}" | grep -qiE 'Events injected:\s*1'; }; then
            echo "✅ [${deviceId}] 设备已通过桌面入口成功拉起微信"
            return 0
        else
            echo "❌ [${deviceId}] 设备拉起微信失败，原因如下："
            echo "${outputPrint}"
            return 1
        fi
    fi
}

jumpWeChatMainForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        jumpWeChatMainActivity "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            jumpWeChatMainActivity "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    jumpWeChatMainForDevice
}

clear
main