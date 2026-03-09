#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 设备跳转 Activity 脚本（启动指定组件）
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
    echo "请输入要跳转的 Activity 所在应用的包名（例如 com.tencent.mm）："
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

    echo "请输入要跳转的 Activity 名称（例如 com.tencent.mm.ui.LauncherUI，留空默认跳转入口）："
    read -r activityName
    if [[ -n "${activityName}" ]]; then
        if ! echo "${activityName}" | grep -qiE '^(\.[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*)*|[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*|\$[A-Za-z_][A-Za-z0-9_]*)*)$'; then
            echo "❌ Activity 名称格式不正确，请输入形如 .MainActivity 或 com.example.MainActivity 的名称"
            exit 1
        fi
    fi

    echo "请输入跳转参数（可空，每行一个 key=value，多行请直接粘贴，按下回车键结束：）"
    parameterMap=()
    if IFS= read -r firstLine; then
        parameterText="${firstLine}"
        while IFS= read -r -t 2 nextLine; do
            if [[ -z "${nextLine}" ]]; then
                break
            fi
            parameterText+=$'\n'
            parameterText+="${nextLine}"
        done
    fi
    if [[ -n "${parameterText}" ]]; then
        while IFS= read -r paramLine; do
            if [[ -z "${paramLine}" ]]; then
                continue
            fi
            if [[ "${paramLine}" =~ ^[A-Za-z_][A-Za-z0-9_]*=.*$ ]]; then
                key="${paramLine%%=*}"
                value="${paramLine#*=}"
                parameterMap+=("-e" "${key}" "${value}")
            else
                echo "👻 跳过无效参数：${paramLine}"
            fi
        done <<< "${parameterText}"
    fi
}

resolveLauncherComponentName() {
    local deviceId=$1
    local dumpsysOut
    dumpsysOut=$(adb -s "${deviceId}" shell dumpsys package "${packageName}" < /dev/null 2>/dev/null | tr -d '\r')
    local inMain=0
    local componentName=""
    while IFS= read -r line; do
        if [[ "${line}" =~ ^[[:space:]]*android\.intent\.action\.MAIN: ]]; then
            inMain=1
            componentName=""
            continue
        fi
        if (( inMain == 1 )) && [[ "${line}" =~ ^[[:space:]]*android\.intent\.action\. ]]; then
            inMain=0
            componentName=""
            continue
        fi
        if (( inMain == 1 )); then
            if [[ "${line}" =~ [[:space:]]([A-Za-z0-9_.]+)/(\.?[A-Za-z0-9_.$]+)[[:space:]]+filter ]]; then
                local componentPackageName="${BASH_REMATCH[1]}"
                local componentClassName="${BASH_REMATCH[2]}"
                componentName="${componentPackageName}/${componentClassName}"
                continue
            fi
            if [[ -n "${componentName}" && "${line}" =~ Category:\ \"android\.intent\.category\.LAUNCHER\" ]]; then
                if echo "${componentName}" | grep -qiE 'ResolverActivity|leakcanary|com\.squareup\.leakcanary'; then
                    componentName=""
                    continue
                fi
                if echo "${componentName}" | grep -qE "^${packageName}/"; then
                    echo "${componentName}"
                    return 0
                else
                    componentName=""
                fi
            fi
        fi
    done <<< "${dumpsysOut}"
    echo ""
    return 1
}

jumpActivity() {
    local deviceId=$1
    if [[ -z "${activityName}" ]]; then
        echo "⏳ 正在向 [${deviceId}] 设备启动应用入口：${packageName}"
        local componentName
        componentName=$(resolveLauncherComponentName "${deviceId}")
        local outputPrint
        if [[ -n "${componentName}" ]]; then
            echo "解析到主 Activity 组件：${componentName}"
            outputPrint=$(adb -s "${deviceId}" shell am start -W -n "${componentName}" "${parameterMap[@]}" < /dev/null 2>&1)
            local exitCode=$?
            if (( exitCode == 0 )) && { echo "${outputPrint}" | grep -q -E 'Status:\s*ok'; } && { ! echo "${outputPrint}" | grep -qiE 'unable to resolve Intent|Activity not found|Permission denied|SecurityException|Error:'; }; then
                echo "✅ [${deviceId}] 设备启动应用入口成功"
                return 0
            else
                echo "❌ [${deviceId}] 设备启动应用入口失败，原因如下："
                echo "${outputPrint}"
                return 1
            fi
        else
            if (( ${#parameterMap[@]} > 0 )); then
                echo "👻 未指定 Activity，extras 参数将不会被传递"
            fi
            outputPrint=$(adb -s "${deviceId}" shell monkey -p "${packageName}" -c android.intent.category.LAUNCHER 1 < /dev/null 2>&1)
            local exitCode=$?
            if (( exitCode == 0 )) && { echo "${outputPrint}" | grep -qiE 'Events injected:\s*1'; }; then
                echo "✅ [${deviceId}] 设备启动应用入口成功"
                return 0
            else
                echo "❌ [${deviceId}] 设备启动应用入口失败，原因如下："
                echo "${outputPrint}"
                return 1
            fi
        fi
    else
        local componentName="${packageName}/${activityName}"
        echo "⏳ 正在向 [${deviceId}] 设备发起跳转：${componentName}"
        local outputPrint
        outputPrint=$(adb -s "${deviceId}" shell am start -W -n "${componentName}" "${parameterMap[@]}" < /dev/null 2>&1)
        local exitCode=$?
        if (( exitCode == 0 )) && { echo "${outputPrint}" | grep -q -E 'Status:\s*ok'; } && { ! echo "${outputPrint}" | grep -qiE 'unable to resolve Intent|Activity not found|Permission denied|SecurityException|Error:'; }; then
            echo "✅ [${deviceId}] 设备跳转 Activity 成功"
            return 0
        else
            echo "❌ [${deviceId}] 设备跳转 Activity 失败，原因如下："
            echo "${outputPrint}"
            return 1
        fi
    fi
}

jumpActivityForDevice() {
    local deviceId
    deviceId="$(inputMultipleAdbDevice)"
    if [[ -n "${deviceId}" ]]; then
        jumpActivity "${deviceId}"
    else
        adbDeviceIdsString=$(getAdbDeviceIdsString)
        while read -r adbDeviceId; do
            jumpActivity "${adbDeviceId}"
        done < <(echo "${adbDeviceIdsString}" | tr -d '\r' | grep -v '^$')
    fi
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    waitUserInputParameter
    jumpActivityForDevice
}

clear
main