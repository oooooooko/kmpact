#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 设备跳转网址脚本（唤起浏览器打开链接）
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

waitUserInputParameter() {
    echo "请输入要跳转的 URL（支持网页、Scheme URL）："
    read -r inputUrl
    if [[ -z "${inputUrl}" ]]; then
        echo "❌ URL 不能为空"
        exit 1
    fi
    if ! echo "${inputUrl}" | grep -qiE '^[a-z][a-z0-9+.-]*:(//)[^ ]*'; then
        echo "❌ URL 格式不正确，请输入形如 http(s)://、wx://、alipays:// 等格式的 URL"
        exit 1
    fi
}

jumpUrl() {
    local deviceId=$1
    local url=$2
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" shell am start -W -a android.intent.action.VIEW -d "${url}" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode == 0 )) && { echo "${outputPrint}" | grep -q -E 'Status:\s*ok'; } && { ! echo "${outputPrint}" | grep -qiE 'unable to resolve Intent|Activity not found|Permission denied|SecurityException'; }; then
        echo "✅ [${deviceId}] 设备跳转 URL 成功"
        return 0
    else
        echo "❌ [${deviceId}] 设备跳转 URL 失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
}

jumpUrlForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        jumpUrl "${deviceId}" "${inputUrl}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            jumpUrl "${adbDeviceId}" "${inputUrl}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    waitUserInputParameter
    jumpUrlForDevice
}

clear
main