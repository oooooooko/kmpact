#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 开启无线 adb 调试脚本
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../common/SystemPlatform.sh"
source "${scriptDirPath}/../common/SystemPlatform.sh"
[ -z "" ] || source "../common/EnvironmentTools.sh"
source "${scriptDirPath}/../common/EnvironmentTools.sh"
[ -z "" ] || source "/../business/DevicesSelector.sh"
source "${scriptDirPath}/../business/DevicesSelector.sh"

waitUserInputParameter() {
    echo "请输入设备端无线连接的端口号（可空，默认端口号为 5555）："
    read -r port
    if [[ -z "${port}" ]]; then
        port=5555
    fi
}

getWifiIp() {
    local deviceId=$1
    local ip=""
    # adb shell ip route | awk '/wlan0/{for(i=1;i<=NF;i++) if($i=="src"){print $(i+1); exit}}' | tr -d '\r'
    ip=$(adb -s "${deviceId}" shell ip route < /dev/null 2>/dev/null | awk '/wlan0/{for(i=1;i<=NF;i++) if($i=="src"){print $(i+1); exit}}' | tr -d '\r')
    if [[ -z "${ip}" ]]; then
        # adb shell ip -o addr show wlan0 | awk '/inet /{split($4,a,"/"); print a[1]; exit}' | tr -d '\r'
        ip=$(adb -s "${deviceId}" shell ip -o addr show wlan0 < /dev/null 2>/dev/null | awk '/inet /{split($4,a,"/"); print a[1]; exit}' | tr -d '\r')
    fi
    if [[ -z "${ip}" ]]; then
        # adb shell ifconfig wlan0 | awk -F'[: ]+' '/inet addr/{print $4; exit}' | tr -d '\r'
        ip=$(adb -s "${deviceId}" shell ifconfig wlan0 < /dev/null 2>/dev/null | awk -F'[: ]+' '/inet addr/{print $4; exit}' | tr -d '\r')
        if [[ -z "${ip}" ]]; then
            # adb shell ifconfig wlan0 | awk '/inet /{print $2; exit}' | tr -d '\r'
            ip=$(adb -s "${deviceId}" shell ifconfig wlan0 < /dev/null 2>/dev/null | awk '/inet /{print $2; exit}' | tr -d '\r')
        fi
    fi
    if [[ -z "${ip}" ]]; then
        # adb shell getprop dhcp.wlan0.ipaddress | tr -d '\r'
        ip=$(adb -s "${deviceId}" shell getprop dhcp.wlan0.ipaddress < /dev/null 2>/dev/null | tr -d '\r')
    fi
    echo "${ip}"
}

connectWirelessAdb() {
    local deviceId=$1
    local ip
    ip=$(getWifiIp "${deviceId}")
    if [[ -z "${ip}" ]]; then
        echo "👻 未能获取到设备网络 IP 地址，请手动输入（如 192.168.x.x）："
        read -r ip
        if [[ -z "${ip}" ]]; then
            echo "❌ 未提供 IP 地址，已取消操作"
            return 1
        fi
    else
        echo "📝 [${deviceId}] 设备的网络 IP 地址：${ip}"
    fi

    # 打开 TCP/IP 调试模式
    adb -s "${deviceId}" tcpip "${port}" < /dev/null > /dev/null
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ [${deviceId}] 设备切换到 TCP/IP 模式失败"
        return 1
    fi

    # 等待一秒钟以确保设备切换模式完成
    sleep 1

    # 连接到设备的无线调试
    adb connect "${ip}:${port}" < /dev/null > /dev/null
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ 连接失败，请确认设备与电脑在同一网络，且已允许无线调试"
        return 1
    fi

    connected=$(adb devices < /dev/null | awk -v target="${ip}:${port}" '$1==target && $2=="device"{print $0}')
    if [[ -z "${connected}" ]]; then
        echo "👻 连接状态未知，请检查 adb devices 列表"
        return 1
    fi

    echo "✅ [${deviceId}] 设备已通过无线调试连接，连接地址：${ip}:${port}"
    return 0
}

connectWirelessAdbForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice "${ADB_MODE_USB}")"
    if [[ -n "${deviceId}" ]]; then
        connectWirelessAdb "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString "${ADB_MODE_USB}")
        while read -r adbDeviceId; do
            connectWirelessAdb "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
    return 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    waitUserInputParameter
    connectWirelessAdbForDevice
}

clear
main