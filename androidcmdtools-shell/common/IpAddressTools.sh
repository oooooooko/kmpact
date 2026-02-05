#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 剪贴板工具
# ----------------------------------------------------------------------
[ -z "" ] || source "/SystemPlatform.sh"
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/SystemPlatform.sh"
[ -z "" ] || source "/EnvironmentTools.sh"
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/EnvironmentTools.sh"

isIpV4Format() {
    local ip=$1
    if [[ -z "${ip}" ]]; then
        return 1
    fi

    local re='^([0-9]{1,3}\.){3}[0-9]{1,3}$'
    if [[ ! "${ip}" =~ ${re} ]]; then
        return 1
    fi

    IFS='.' read -ra octets <<< "$ip"
    if [[ ${#octets[@]} -ne 4 ]]; then
        return 1
    fi

    for octet in "${octets[@]}"; do
        if (( 10#${octet} < 0 || 10#${octet} > 255 )); then
            return 1
        fi
    done

    return 0
}

isLocalhostIp() {
    local ip=$1
    if [[ "${ip}" == "localhost" ]]; then
        return 0
    fi

    if [[ "${ip}" =~ ^127\. ]]; then
        return 0
    fi

    return 1
}

getComputerIpV4() {
    local ip=""

    if isMacOs; then

        if existCommand "route" && existCommand "ipconfig"; then
            local outputPrint
            outputPrint=$(route -n get default 2>/dev/null | awk '/interface:/{print $2}')
            if [[ -n "${outputPrint}" ]]; then
                ip=$(ipconfig getifaddr "${outputPrint}" 2>/dev/null | tr -d '\r')
                if isIpV4Format "${ip}" && ! isLocalhostIp "${ip}"; then
                    echo "${ip}"
                    return 0
                fi
            fi
        fi

        if existCommand "ipconfig"; then

            ip=$(ipconfig getifaddr en0 2>/dev/null | tr -d '\r')
            if isIpV4Format "${ip}" && ! isLocalhostIp "${ip}"; then
                echo "${ip}"
                return 0
            fi

            ip=$(ipconfig getifaddr en1 2>/dev/null | tr -d '\r')
            if isIpV4Format "${ip}" && ! isLocalhostIp "${ip}"; then
                echo "${ip}"
                return 0
            fi
        fi

    elif isWindows; then

        if existCommand "ipconfig"; then
            local ipList
            ipList=$(ipconfig | grep -i "IPv4" | awk -F: '{print $2}' | tr -d ' \r')
            for ip in ${ipList}; do
                if isIpV4Format "${ip}" && ! isLocalhostIp "${ip}"; then
                    echo "${ip}"
                    return 0
                fi
            done
        fi

    elif isLinux; then

        if existCommand "hostname"; then
             local ipList
             ipList=$(hostname -I 2>/dev/null)
             for ip in ${ipList}; do
                if isIpV4Format "${ip}" && ! isLocalhostIp "${ip}"; then
                    echo "${ip}"
                    return 0
                fi
            done
        fi
    fi

    if existCommand "ifconfig"; then
        local ipList
        ipList=$(ifconfig | awk '/inet / && $2 != "127.0.0.1"{print $2}' | tr -d '\r')
        for ip in ${ipList}; do
            if isIpV4Format "${ip}" && ! isLocalhostIp "${ip}"; then
                echo "${ip}"
                return 0
            fi
        done
    fi

    if existCommand "ip"; then
        local ipList
        ipList=$(ip addr show | grep 'inet ' | grep -v '127.0.0.1' | awk '{print $2}' | cut -d/ -f1)
        for ip in ${ipList}; do
            if isIpV4Format "${ip}" && ! isLocalhostIp "${ip}"; then
                echo "${ip}"
                return 0
            fi
        done
    fi

    return 1
}