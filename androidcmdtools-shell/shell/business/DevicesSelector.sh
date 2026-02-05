#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 设备选择器脚本（选择单设备或多设备）
# ----------------------------------------------------------------------

DEVICE_MODE_ADB="adb"
DEVICE_MODE_FASTBOOT="fastboot"
DEVICE_MODE_ALL="all"

ADB_MODE_USB="usb"
ADB_MODE_TCP="tcp"
ADB_MODE_ALL="all"

getDeviceBrandByAdb() {
    local deviceId=$1
    adb -s "${deviceId}" shell getprop ro.product.brand < /dev/null 2>/dev/null | tr -d '\r'
}

getDeviceModelByAdb() {
    local deviceId=$1
    adb -s "${deviceId}" shell getprop ro.product.model < /dev/null 2>/dev/null | tr -d '\r'
}

getAndroidVersionNameByAdb() {
    local deviceId=$1
    adb -s "${deviceId}" shell getprop ro.build.version.release < /dev/null 2>/dev/null | tr -d '\r'
}

getAndroidVersionCodeByAdb() {
    local deviceId=$1
    adb -s "${deviceId}" shell getprop ro.build.version.sdk < /dev/null 2>/dev/null | tr -d '\r'
}

getAdbDeviceInfo() {
    local deviceId=$1
    echo "${deviceId}	$(getDeviceBrandByAdb "${deviceId}") $(getDeviceModelByAdb "${deviceId}") API $(getAndroidVersionCodeByAdb "${deviceId}") Android $(getAndroidVersionNameByAdb "${deviceId}")"
}

getDeviceCodeByFastboot() {
    local deviceId=$1
    local outputPrint
    outputPrint=$(fastboot -s "${deviceId}" getvar product < /dev/null 2>&1 | tr -d '\r')
    echo "${outputPrint}" | awk -F': ' '/^[Pp]roduct:/ {print $2; exit}'
}

getFastbootDeviceInfo() {
    local deviceId=$1
    echo "${deviceId}	$(getDeviceCodeByFastboot "${deviceId}")"
}

getAdbDeviceIdsString() {
    local adbMode=${1:-"${ADB_MODE_ALL}"}
    local list
    # 增加正则过滤，只提取由字母、数字、下划线、连字符、点号、冒号组成的序列号，增强健壮性
    list=$(adb devices < /dev/null 2>/dev/null | awk 'NR>1 && NF>0 && $2 == "device" {print $1}' | grep -E '^[a-zA-Z0-9._:-]+$')
    if [[ "${adbMode}" == "${ADB_MODE_USB}" ]]; then
        echo "${list}" | tr -d '\r' | grep -v '.*:.*' || true
    elif [[ "${adbMode}" == "${ADB_MODE_TCP}" ]]; then
        echo "${list}" | tr -d '\r' | grep -E '.*:.*' || true
    else
        echo "${list}" | tr -d '\r'
    fi
}

getFastbootDeviceIdsString() {
    fastboot devices < /dev/null 2>/dev/null | grep -E '^[a-zA-Z0-9._:-]+\s+fastboot\s*$' | awk '{print $1}'
}

inputMultipleAdbDevice() {
   local adbMode=${1:-"${ADB_MODE_ALL}"}
   inputTargetDevice "${DEVICE_MODE_ADB}" "true" "${adbMode}"
}

inputSingleAdbDevice() {
   local adbMode=${1:-"${ADB_MODE_ALL}"}
   inputTargetDevice "${DEVICE_MODE_ADB}" "false" "${adbMode}"
}

inputMultipleFastbootDevice() {
   inputTargetDevice "${DEVICE_MODE_FASTBOOT}" "true"
}

inputSingleFastbootDevice() {
   inputTargetDevice "${DEVICE_MODE_FASTBOOT}" "false"
}

inputMultipleDevice() {
   inputTargetDevice "${DEVICE_MODE_ALL}" "true"
}

inputSingleDevice() {
   inputTargetDevice "${DEVICE_MODE_ALL}" "false"
}

inputTargetDevice() {
    local deviceMode=$1
    local multipleSelect=$2
    local adbMode=${3:-"${ADB_MODE_ALL}"}

    local adbDeviceList=()

    local deviceId
    if [[ "${deviceMode}" == "${DEVICE_MODE_ALL}" || "${deviceMode}" == "${DEVICE_MODE_ADB}" ]]; then
        for deviceId in $(getAdbDeviceIdsString "${adbMode}"); do
            if [[ -z "${deviceId}" ]]; then
                continue
            fi
            adbDeviceList+=("${deviceId}")
        done
    fi

    local fastbootDeviceList=()
    if [[ "${deviceMode}" == "${DEVICE_MODE_ALL}" || "${deviceMode}" == "${DEVICE_MODE_FASTBOOT}" ]]; then
        for deviceId in $(getFastbootDeviceIdsString); do
            if [[ -z "${deviceId}" ]]; then
                continue
            fi
            fastbootDeviceList+=("${deviceId}")
        done
    fi

    local adbDeviceCount=${#adbDeviceList[@]}
    local fastbootDeviceCount=${#fastbootDeviceList[@]}

    local deviceCount=$((adbDeviceCount+fastbootDeviceCount))
    if (( deviceCount == 0 )); then
        if [[ "${deviceMode}" == "${DEVICE_MODE_ADB}" ]]; then
            if [[ "${adbMode}" == "${ADB_MODE_USB}" ]]; then
                echo "❌ 连接失败，在 adb 有线模式下没有检测到有设备和电脑建立了连接" >&2
            elif [[ "${adbMode}" == "${ADB_MODE_TCP}" ]]; then
                echo "❌ 连接失败，在 adb 无线模式下没有检测到有设备和电脑建立了连接" >&2
            else
                echo "❌ 连接失败，在 adb 模式下没有检测到有设备和电脑建立了连接" >&2
            fi
        elif [[ "${deviceMode}" == "${DEVICE_MODE_FASTBOOT}" ]]; then
            echo "❌ 连接失败，在 fastboot 模式下没有检测到有设备和电脑建立了连接" >&2
        else
            echo "❌ 连接失败，在 adb 或 fastboot 模式下都没有检测到有设备和电脑建立了连接" >&2
        fi
        kill -SIGTERM $$
        exit 1
    fi

    if (( deviceCount == 1 )); then
        if (( adbDeviceCount == 1 )); then
            echo "${adbDeviceList[0]}"
        else
            echo "${fastbootDeviceList[0]}"
        fi
        return 0
    fi

    if [[ ${multipleSelect} == "true" ]]; then
        echo "📱 检测到多台设备，请选择要操作设备（输入编号或设备 ID，留空将对所有设备操作）：" >&2
    else
        echo "📱 检测到多台设备，请选择要操作设备（输入编号或设备 ID）：" >&2
    fi
    local showModeForAdb=""
    local showModeForFastboot=""
    if (( adbDeviceCount >= 1 && fastbootDeviceCount >= 1 )); then
        showModeForAdb="  ${DEVICE_MODE_ADB}   "
        showModeForFastboot="${DEVICE_MODE_FASTBOOT}"
    fi
    local rows=()
    local listIndex=1
    local maxIndexWidth=0
    local maxModeWidth=0
    local maxIdWidth=0
    local maxBrandWidth=0
    local maxModelWidth=0
    local maxAndroidInfoWidth=0
    for deviceId in "${adbDeviceList[@]}"; do
        local brandStr
        local modelStr
        local androidInfoStr
        brandStr=$(getDeviceBrandByAdb "${deviceId}")
        modelStr=$(getDeviceModelByAdb "${deviceId}")
        androidInfoStr="Android $(getAndroidVersionNameByAdb "${deviceId}")（API $(getAndroidVersionCodeByAdb "${deviceId}")）"
        local idxStr="${listIndex}."
        local modeStr="${showModeForAdb}"
        local idStr="${deviceId}"
        local l1=${#idxStr}
        local l2=${#modeStr}
        local l3=${#idStr}
        local l4=${#brandStr}
        local l5=${#modelStr}
        local l6=${#androidInfoStr}
        (( l1 > maxIndexWidth )) && maxIndexWidth=${l1}
        (( l2 > maxModeWidth )) && maxModeWidth=${l2}
        (( l3 > maxIdWidth )) && maxIdWidth=${l3}
        (( l4 > maxBrandWidth )) && maxBrandWidth=${l4}
        (( l5 > maxModelWidth )) && maxModelWidth=${l5}
        (( l6 > maxAndroidInfoWidth )) && maxAndroidInfoWidth=${l6}
        rows+=("${idxStr}|${modeStr}|${idStr}|${brandStr}|${modelStr}|${androidInfoStr}")
        ((listIndex++))
    done
    for deviceId in "${fastbootDeviceList[@]}"; do
        local brandStr
        local modelStr
        local androidInfoStr
        brandStr=$(getDeviceCodeByFastboot "${deviceId}")
        modelStr=""
        androidInfoStr=""
        local idxStr="${listIndex}."
        local modeStr="${showModeForFastboot}"
        local idStr="${deviceId}"
        local l1=${#idxStr}
        local l2=${#modeStr}
        local l3=${#idStr}
        local l4=${#brandStr}
        local l5=${#modelStr}
        local l6=${#androidInfoStr}
        (( l1 > maxIndexWidth )) && maxIndexWidth=${l1}
        (( l2 > maxModeWidth )) && maxModeWidth=${l2}
        (( l3 > maxIdWidth )) && maxIdWidth=${l3}
        (( l4 > maxBrandWidth )) && maxBrandWidth=${l4}
        (( l5 > maxModelWidth )) && maxModelWidth=${l5}
        (( l6 > maxAndroidInfoWidth )) && maxAndroidInfoWidth=${l6}
        rows+=("${idxStr}|${modeStr}|${idStr}|${brandStr}|${modelStr}|${androidInfoStr}")
        ((listIndex++))
    done
    for row in "${rows[@]}"; do
        IFS='|' read -r c1 c2 c3 c4 c5 c6 <<< "${row}"
        printf "%-*s %-*s %-*s %-*s %-*s %-*s\n" \
            "${maxIndexWidth}" "${c1}" \
            "${maxModeWidth}" "${c2}" \
            "${maxIdWidth}" "${c3}" \
            "${maxBrandWidth}" "${c4}" \
            "${maxModelWidth}" "${c5}" \
            "${maxAndroidInfoWidth}" "${c6}" >&2
    done

    read -r deviceChoice
    if [[ -z ${deviceChoice} ]]; then
        if [[ ${multipleSelect} == "true" ]]; then
            echo ""
            return 0
        else
            echo "❌ 必须选择一台设备，已取消操作" >&2
            kill -SIGTERM $$
            exit 1
        fi
    elif [[ "${deviceChoice}" =~ ^[0-9]+$ ]]; then
        local number=$((deviceChoice))
        if (( number >= 1 && number <= adbDeviceCount )); then
            echo "${adbDeviceList[$((number-1))]}"
            return 0
        elif (( number >= (adbDeviceCount+1) && number <= (adbDeviceCount+fastbootDeviceCount) )); then
            echo "${fastbootDeviceList[$((number-1-adbDeviceCount))]}"
            return 0
        fi
    else
        for deviceId in "${adbDeviceList[@]}"; do
            if [[ "${deviceChoice}" != "${deviceId}" ]]; then
                continue
            fi
            echo "${deviceId}"
            return 0
        done
        for deviceId in "${fastbootDeviceList[@]}"; do
            if [[ "${deviceChoice}" != "${deviceId}" ]]; then
                continue
            fi
            echo "${deviceId}"
            return 0
        done
    fi

    echo "❌ 无效选择，已取消操作" >&2
    kill -SIGTERM $$
    exit 1
}