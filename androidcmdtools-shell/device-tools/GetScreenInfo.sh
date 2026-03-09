#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 屏幕信息获取脚本（分辨率与密度）
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

printDeviceScreenParams() {
    local deviceId=$1
    local screenWidth
    local screenHeight
    local densityDpi
    local pixelDensity
    local targetResources
    local smallestWidth

    local windowSize
    windowSize=$(adb -s "${deviceId}" shell wm size < /dev/null 2>/dev/null | tr -d '\r')
    local physicalLine
    physicalLine=$(echo "${windowSize}" | grep -i "Physical size:" | awk -F': ' '{print $2}')
    if [[ -n "${physicalLine}" ]]; then
        screenWidth=$(echo "${physicalLine}" | awk -Fx '{print $1}')
        screenHeight=$(echo "${physicalLine}" | awk -Fx '{print $2}')
    else
        local init
        init=$(adb -s "${deviceId}" shell dumpsys window < /dev/null 2>/dev/null | tr -d '\r' | grep -m1 -o 'init=[0-9]\+x[0-9]\+' | head -n1)
        if [[ -n "${init}" ]]; then
            screenWidth=$(echo "${init}" | awk -F'[=x]' '{print $2}')
            screenHeight=$(echo "${init}" | awk -F'[=x]' '{print $3}')
        fi
    fi
    if [[ -z "${screenWidth}" || -z "${screenHeight}" ]]; then
        echo "❌ [${deviceId}] 设备获取屏幕尺寸失败"
        return 1
    fi

    local windowDensity
    windowDensity=$(adb -s "${deviceId}" shell wm density < /dev/null 2>/dev/null | tr -d '\r')
    densityDpi=$(echo "${windowDensity}" | grep -i "Physical density:" | awk -F': ' '{print $2}')
    if [[ -z "${densityDpi}" ]]; then
        densityDpi=$(adb -s "${deviceId}" shell getprop ro.sf.lcd_density < /dev/null 2>/dev/null | tr -d '\r')
    fi
    if [[ -z "${densityDpi}" ]]; then
        echo "❌ [${deviceId}] 设备获取屏幕密度失败"
        return 1
    fi

    pixelDensity=$(awk -v d="${densityDpi}" 'BEGIN{printf "%.2f", d/160}')
    targetResources=$(awk -v d="${densityDpi}" 'BEGIN{
        if (d<140) print "ldpi";
        else if (d<200) print "mdpi";
        else if (d<280) print "hdpi";
        else if (d<400) print "xhdpi";
        else if (d<560) print "xxhdpi";
        else print "xxxhdpi";
    }')
    smallestWidth=$(awk -v w="${screenWidth}" -v h="${screenHeight}" -v s="${pixelDensity}" 'BEGIN{
        min=(w<h)?w:h;
        printf "%d", int(min/s + 0.5);
    }')

    echo "✅ [${deviceId}] 设备的屏幕参数如下："
    echo "屏幕宽度：${screenWidth}"
    echo "屏幕高度：${screenHeight}"
    echo "屏幕密度：${densityDpi}"
    echo "密度像素：${pixelDensity}"
    echo "目标资源：${targetResources}"
    echo "最小宽度：${smallestWidth}"
    return 0
}

printScreenParamsForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        printDeviceScreenParams "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            printDeviceScreenParams "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
    exit 0
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    printScreenParamsForDevice
}

clear
main