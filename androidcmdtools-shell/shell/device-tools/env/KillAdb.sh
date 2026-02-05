#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : adb 进程杀死脚本（停止服务器）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../common/ProcessTools.sh"
source "${scriptDirPath}/../../common/ProcessTools.sh"

killAdbProcess() {
    echo "确定要杀死 adb 进程? （y/n）"
    while true; do
        read -r killConfirm
        if [[ "${killConfirm}" == "y" || "${killConfirm}" == "Y" ]]; then
            break
        elif [[ "${killConfirm}" == "n" || "${killConfirm}" == "N" ]]; then
            echo "✅ 用户手动取消操作"
            return 0
        else
            echo "👻 输入不正确，请输入正确的选项（y/n）"
            continue
        fi
    done

    if ! isProcessRunning "adb"; then
        echo "❌ 未检测到 adb 进程，已跳过"
        return 1
    fi

    adb kill-server < /dev/null > /dev/null 2>&1
    killProcess "adb"

    sleep 1

    if isProcessRunning "adb"; then
        echo "❌ 杀死失败，adb 仍在运行"
        exit 1
    fi

    echo "✅ 杀死成功，adb 已停止"
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    killAdbProcess
}

clear
main