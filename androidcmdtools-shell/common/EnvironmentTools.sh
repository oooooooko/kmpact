#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : 环境检查脚本
# ----------------------------------------------------------------------
existCommand() {
    local commandName=$1
    if command -v "${commandName}" < /dev/null > /dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

getJarCmd() {
    if existCommand "jar"; then
        echo "jar"; return 0
    fi
    if [[ -n "${JAVA_HOME}" ]]; then
        if [[ -x "${JAVA_HOME}/bin/jar" ]]; then
            echo "${JAVA_HOME}/bin/jar"; return 0
        fi
        if [[ -x "${JAVA_HOME}/bin/jar.exe" ]]; then
            echo "${JAVA_HOME}/bin/jar.exe"; return 0
        fi
    fi
    echo ""; return 1
}

checkAdbEnvironment() {
    if existCommand "adb"; then
        return 0
    fi
    echo "❌ 找不到 adb 命令，请先配置好电脑上的 android sdk 环境" >&2
    kill -SIGTERM $$
    exit 1
}

checkFastbootEnvironment() {
    if existCommand "fastboot"; then
        return 0
    fi
    echo "❌ 找不到 fastboot 命令，请先配置好电脑上的 android sdk 环境" >&2
    kill -SIGTERM $$
    exit 1
}

checkJavaEnvironment() {
    if existCommand "java"; then
        return 0
    fi
    echo "❌ 找不到 java 命令，请先配置好电脑上的 jdk 环境" >&2
    kill -SIGTERM $$
    exit 1
}

getJavaVersionName() {
    local javaVersionOutput
    local javaVersionLine
    local javaVersionName
    javaVersionOutput=$(java -version 2>&1)
    javaVersionLine=$(echo "${javaVersionOutput}" | head -n 1)
    javaVersionName=$(echo "${javaVersionLine}" | sed -n 's/.*version "\([^"]*\)".*/\1/p')
    if [[ -n "${javaVersionName}" ]]; then
        echo "${javaVersionName}"
        return 0
    fi
    echo ""
    return 1
}

getJavaMajorVersionCode() {
    local javaVersionName
    local javaMajorVersionCode
    javaVersionName=$(getJavaVersionName)
    if [[ -z "${javaVersionName}" ]]; then
        echo ""; return 1
    fi
    if [[ "${javaVersionName}" == 1.* ]]; then
        javaMajorVersionCode=$(echo "${javaVersionName}" | cut -d. -f2 | sed 's/[^0-9].*//')
    else
        javaMajorVersionCode=$(echo "${javaVersionName}" | cut -d. -f1 | sed 's/[^0-9].*//')
    fi
    if [[ -n "${javaMajorVersionCode}" ]]; then
        echo "${javaMajorVersionCode}"
        return 0
    fi
    echo ""
    return 1
}

checkJavaElevenEnvironment() {
    checkJavaEnvironment
    local javaMajorVersionCode
    javaMajorVersionCode=$(getJavaMajorVersionCode)
    if [[ -z "${javaMajorVersionCode}" ]] || (( javaMajorVersionCode < 11 )); then
        if [[ -z "${javaMajorVersionCode}" ]]; then
            echo "❌ 无法解析当前 java 版本，需使用 java 11 版本以上，当前版本信息如下：" >&2
        else
            echo "❌ 当前 java 版本不符合要求，需使用 java 11 版本以上，当前版本信息如下：" >&2
        fi
        echo "----------------------------------------" >&2
        getJavaVersionName 2>&1 >&2
        echo "----------------------------------------" >&2
        kill -SIGTERM $$
        exit 1
    fi
    return 0
}

checkJarEnvironment() {
    local jarCmd
    jarCmd=$(getJarCmd)
    if [[ -n "${jarCmd}" ]]; then
        return 0
    fi
    echo "❌ 找不到 jar 命令，请确保已安装 JDK，并配置好 JAVA_HOME 或将 jar 加入 PATH" >&2
    kill -SIGTERM $$
    exit 1
}

checkGitEnvironment() {
    if existCommand "git"; then
        return 0
    fi
    echo "❌ 找不到 git 命令，请先安装并配置 git" >&2
    kill -SIGTERM $$
    exit 1
}