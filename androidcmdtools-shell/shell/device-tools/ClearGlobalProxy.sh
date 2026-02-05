#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : adb 清除全局代理脚本
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "/../business/DevicesSelector.sh"
source "${scriptDirPath}/../business/DevicesSelector.sh"

getGlobalProxy() {
    local deviceId=$1
    adb -s "${deviceId}" shell settings get global http_proxy < /dev/null 2>/dev/null | tr -d '\r' | tr -d '[:space:]'
}

deleteGlobalProxy() {
    adb -s "${deviceId}" shell settings delete global http_proxy < /dev/null > /dev/null 2>&1
    adb -s "${deviceId}" shell settings delete global global_http_proxy_host < /dev/null > /dev/null 2>&1
    adb -s "${deviceId}" shell settings delete global global_http_proxy_port < /dev/null > /dev/null 2>&1
    adb -s "${deviceId}" shell settings delete global global_http_proxy_exclusion_list < /dev/null > /dev/null 2>&1
    adb -s "${deviceId}" shell settings delete global global_proxy_pac_url < /dev/null > /dev/null 2>&1
    adb -s "${deviceId}" shell settings delete global global_http_proxy_username < /dev/null > /dev/null 2>&1
    adb -s "${deviceId}" shell settings delete global global_http_proxy_password < /dev/null > /dev/null 2>&1
}

isProxyUnset() {
    local proxyAddress=$1
    if [[ -z "${proxyAddress}" ]] || [[ "${proxyAddress}" == "null" ]] || [[ "${proxyAddress}" == "None" ]] || [[ "${proxyAddress}" == "none" ]]; then
        return 0
    fi
    if [[ "${proxyAddress}" == ":0" ]] || [[ "${proxyAddress}" == "0:0" ]]; then
        return 0
    fi
    if ! echo "${proxyAddress}" | grep -qiE '^(([0-9]{1,3}\.){3}[0-9]{1,3}|[A-Za-z0-9.-]+):[0-9]{1,5}$'; then
        return 0
    fi
    return 1
}

clearProxySingleDevice() {
    local deviceId=$1
    local proxyAddress
    proxyAddress=$(getGlobalProxy "${deviceId}")
    if isProxyUnset "${proxyAddress}"; then
        echo "👻 [${deviceId}] 设备没有设置过全局代理，已跳过清除"
        return 0
    fi
    local outputPrint
    outputPrint=$(adb -s "${deviceId}" shell settings put global http_proxy ":0" < /dev/null 2>&1)
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备清除全局代理失败，原因如下："
        echo "${outputPrint}"
        deleteGlobalProxy "${deviceId}"
        return 1
    fi

    deleteGlobalProxy "${deviceId}"
    proxyAddress=$(getGlobalProxy "${deviceId}")
    if ! isProxyUnset "${proxyAddress}"; then
        echo "❌ [${deviceId}] 设备清除全局代理失败，当前代理为：${proxyAddress}"
        return 1
    fi

    echo "✅ [${deviceId}] 设备已清除全局代理"
    return 0
}

clearGolbalProxyForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        clearProxySingleDevice "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            clearProxySingleDevice "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
    exit 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    clearGolbalProxyForDevice
}

clear
main