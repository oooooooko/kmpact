#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : adb 重启脚本（重启服务器进程）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../common/ProcessTools.sh"
source "${scriptDirPath}/../../common/ProcessTools.sh"

restartAdbProcess() {
    echo "确定要重启 adb 进程? （y/n）"
    while true; do
        read -r restartConfirm
        if [[ "${restartConfirm}" == "y" || "${restartConfirm}" == "Y" ]]; then
            break
        elif [[ "${restartConfirm}" == "n" || "${restartConfirm}" == "N" ]]; then
            echo "✅ 用户手动取消操作"
            return 0
        else
            echo "👻 输入不正确，请输入正确的选项（y/n）"
            continue
        fi
    done

    if isProcessRunning "adb"; then
        adb kill-server < /dev/null > /dev/null 2>&1
        killProcess "adb"

        sleep 1

        if isProcessRunning "adb"; then
            echo "❌ 杀死失败，adb 仍在运行"
            return 1
        fi
    fi

    adb start-server < /dev/null > /dev/null 2>&1
    sleep 1

    if ! isProcessRunning "adb"; then
        echo "❌ 重启失败，adb 没有在运行"
        return 1
    fi

    echo "✅ 重启成功，adb 已重新运行"
}

main() {
    printCurrentSystemType
    checkAdbEnvironment
    restartAdbProcess
}

clear
main