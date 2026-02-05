#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : JD‑GUI 打开脚本
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../common/SystemPlatform.sh"
[ -z "" ] || source "../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../common/FileTools.sh"
source "${scriptDirPath}/../../common/FileTools.sh"

cleanup() {
    for p in "${jarPathList[@]}"; do
        if [[ -f "${p}" ]]; then
            rm -f "${p}"
            echo "🧹 已删除临时 jar 文件：${p}"
        fi
    done
    for d in "${extractedDexList[@]}"; do
        if [[ -f "${d}" ]]; then
            rm -f "${d}"
            echo "🧹 已删除临时 dex 文件：${d}"
        fi
    done
}

getDexOrderNumber() {
    local entryName
    entryName=$(basename "$1")
    if [[ "${entryName}" == "classes.dex" ]]; then
        echo 1; return 0
    fi
    local num
    num=$(echo "${entryName}" | sed -n -E 's/^classes([0-9]+)\.dex$/\1/p')
    if [[ -n "${num}" ]]; then
        echo "${num}"; return 0
    fi
    echo 99999
}

dexToJar() {
    local dexPath="$1"
    local base="${dexPath%.*}"
    local tsStr=""
    tsStr="-$(date "+%Y%m%d%H%M%S")"
    local outputJarPath="${base}${tsStr}.jar"
    echo "输出的 jar 文件路径：${outputJarPath}" >&2
    local outputPrint
    outputPrint="$("${resourcesDirPath}$(getFileSeparator)dex2jar-2.4$(getFileSeparator)d2j-dex2jar.sh" -f -o "${outputJarPath}" "${dexPath}" 2>&1)"
    local exitCode=$?
    if (( exitCode != 0 )); then
        echo "❌ dex 转 jar 失败，原因如下："
        echo "${outputPrint}"
        return 1
    fi
    if [[ ! -f "${outputJarPath}" ]]; then
        echo "❌ 转换失败，请检查 d2j-dex2jar.sh 输出的信息："
        echo "${outputPrint}"
        return 1
    fi
    echo "${outputJarPath}"
}

openWithJdGui() {
    local filePath=$1
    java -jar "${resourcesDirPath}$(getFileSeparator)jd-gui-1.6.6.jar" "$filePath"
}

openDexWithJdGui() {
    local dexFilePath="$1"
    extractedDexList=()
    jarPathList=()
    trap cleanup EXIT
    local jarPath
    jarPath="$(dexToJar "${dexFilePath}")" || { exit 1; }
    jarPathList+=("${jarPath}")
    openWithJdGui "${jarPath}"
}

openApkWithJdGui() {
    parentDirPath=$(dirname "${inputFilePath}")
    jarCmd=$(getJarCmd)
    ts=$(date "+%Y%m%d%H%M%S")
    extractedDexList=()
    jarPathList=()
    trap cleanup EXIT
    dexEntryList=()
    while IFS= read -r dexEntry; do
        if [[ -n "${dexEntry}" ]]; then
            dexEntryList+=("${dexEntry}")
        fi
    done < <("${jarCmd}" tf "${inputFilePath}" | grep -E '\.dex$')
    dexCount=${#dexEntryList[@]}
    if (( dexCount == 0 )); then
        echo "❌ 未在 apk 中找到 dex 文件"
        exit 1
    fi

    sizeEntryList=()
    sizeBytesList=()
    while IFS= read -r tvLine; do
        parsedLine=$(echo "${tvLine}" | sed -E 's/^[[:space:]]*([0-9]+).* ([^ ]+)$/\1 \2/')
        bytes=$(echo "${parsedLine}" | awk '{print $1}')
        name=$(echo "${parsedLine}" | awk '{print $2}')
        if [[ -n "${bytes}" ]] && [[ -n "${name}" ]]; then
            sizeEntryList+=("${name}")
            sizeBytesList+=("${bytes}")
        fi
    done < <("${jarCmd}" tvf "${inputFilePath}" | grep -E '\.dex$')

    for ((i=0; i<dexCount; i++)); do
        min=$i
        minKey=$(getDexOrderNumber "${dexEntryList[$min]}")
        for ((j=i+1; j<dexCount; j++)); do
            key=$(getDexOrderNumber "${dexEntryList[$j]}")
            if (( key < minKey )); then
                min=$j
                minKey=$key
            elif (( key == minKey )); then
                baseMin=$(basename "${dexEntryList[$min]}")
                baseJ=$(basename "${dexEntryList[$j]}")
                if [[ "${baseJ}" < "${baseMin}" ]]; then
                    min=$j
                    minKey=$key
                fi
            fi
        done
        if (( min != i )); then
            temp="${dexEntryList[$i]}"
            dexEntryList[i]="${dexEntryList[$min]}"
            dexEntryList[min]="${temp}"
        fi
    done

    selectedDexEntries=()
    if (( dexCount == 1 )); then
        selectedDexEntries=("${dexEntryList[0]}")
        bytes=""
        for k in "${!sizeEntryList[@]}"; do
            if [[ "${sizeEntryList[$k]}" == "${dexEntryList[0]}" ]]; then
                bytes="${sizeBytesList[$k]}"; break
            fi
        done
        if [[ -z "${bytes}" ]]; then bytes=0; fi
        sizeMB=$(awk "BEGIN {printf \"%.2f\", ${bytes}/1024/1024}")
        echo "📝 已自动选择：$(basename "${dexEntryList[0]}")（${sizeMB} MB）"
    else
        echo "检测到多个 dex 文件，请输入序号选择（直接回车表示全部）"
        for i in "${!dexEntryList[@]}"; do
            bytes=""
            for k in "${!sizeEntryList[@]}"; do
                if [[ "${sizeEntryList[$k]}" == "${dexEntryList[$i]}" ]]; then
                    bytes="${sizeBytesList[$k]}"; break
                fi
            done
            if [[ -z "${bytes}" ]]; then bytes=0; fi
            sizeMB=$(awk "BEGIN {printf \"%.2f\", ${bytes}/1024/1024}")
            echo "$((i+1)). $(basename "${dexEntryList[$i]}")（${sizeMB} MB）"
        done
        read -r inputIndex
        if [[ -z "${inputIndex}" ]]; then
            selectedDexEntries=("${dexEntryList[@]}")
        elif [[ "${inputIndex}" =~ ^[0-9]+$ ]]; then
            index=$((inputIndex-1))
            if (( index >= 0 && index < dexCount )); then
                selectedDexEntries=("${dexEntryList[$index]}")
                echo "📝 已选择：$(basename "${dexEntryList[$index]}")"
            else
                echo "👻 无效的序号：${inputIndex}，操作中止"
                kill -SIGTERM $$
                exit 1
            fi
        else
            echo "👻 无效的输入：${inputIndex}，操作中止"
            kill -SIGTERM $$
            exit 1
        fi
    fi

    cd "${parentDirPath}" || {
        echo "❌ 无法进入目录：${parentDirPath}"
        exit 1
    }
    for dexEntry in "${selectedDexEntries[@]}"; do
        outputPrint="$("${jarCmd}" xf "${inputFilePath}" "${dexEntry}" 2>&1)"
        exitCode=$?
        if (( exitCode != 0 )); then
            echo "❌ 解压 dex 失败，原因如下："
            echo "${outputPrint}"
            exit 1
        fi
        baseName="$(basename "${dexEntry}")"
        pureBase="$(basename "${inputFilePath}")-${baseName%.*}"
        newDexFilePath="${parentDirPath}$(getFileSeparator)${pureBase}.dex"
        mv "${parentDirPath}$(getFileSeparator)${baseName}" "${newDexFilePath}"
        extractedDexList+=("${newDexFilePath}")
    done

    for dexPath in "${extractedDexList[@]}"; do
        outputJarPath=$(dexToJar "${dexPath}")
        rm -f "${dexPath}"
        jarPathList+=("${outputJarPath}")
    done

    pids=()
    for jarPath in "${jarPathList[@]}"; do
        openWithJdGui "${jarPath}" &
        pids+=($!)
        sleep 0.1
    done
    for pid in "${pids[@]}"; do
        wait "${pid}"
    done
}

waitUserInputParameter() {
    resourcesDirPath=$(getResourcesDirPath)
    if [[ -z "${resourcesDirPath}" ]]; then
        echo "❌ 未找到 resources 目录，请确保它位于脚本的当前目录或者父目录"
        exit 1
    fi
    echo "资源目录为：${resourcesDirPath}"

    echo "请输入要用 jd-gui 打开的文件路径（支持 apk, dex, aar, class, ear, jar, java, jmod, kar, log, war, zip）"
    read -r inputFilePath
    inputFilePath=$(parseComputerFilePath "${inputFilePath}")

    if [[ ! -f "${inputFilePath}" ]]; then
        echo "❌ 文件不存在，请检查 ${inputFilePath} 文件路径是否正确"
        exit 1
    fi
}

main() {
    printCurrentSystemType
    checkJavaEnvironment
    checkJarEnvironment
    waitUserInputParameter

    if [[ ! "${inputFilePath}" =~ \.(apk|dex|aar|class|ear|jar|java|jmod|kar|log|war|zip)$ ]]; then
        echo "❌ 文件错误，仅支持后缀为 apk, dex, aar, class, ear, jar, java, jmod, kar, log, war, zip 的文件"
        exit 1
    fi

    if [[ "${inputFilePath}" =~ \.(apk)$ ]]; then
        openApkWithJdGui "${inputFilePath}"
    elif [[ "${inputFilePath}" =~ \.(dex)$ ]]; then
        openDexWithJdGui "${inputFilePath}"
    else
        openWithJdGui "${inputFilePath}"
    fi
}

clear
main