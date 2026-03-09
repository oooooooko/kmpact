#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/27
#      desc    : 管理文件
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../common/SystemPlatform.sh" && \
source "../common/EnvironmentTools.sh" && \
source "../common/FileTools.sh" && \
source "../business/DevicesSelector.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

DIVIDING_LINE="-------------------------------------------------------------------------------"

DIR_ICON_EMOJI="📁"
FILE_ICON_EMOJI="📄"

SDCARD_ROOT_PATH="/sdcard/"
ANDROID_TEXT_EXTENSIONS=(
    "txt" "log" "ini" "conf" "props" "properties" "env" "sh" "bash" "bat"
    "json" "xml" "yaml" "yml" "toml" "md" "markdown" "csv" "tsv"
    "rc" "mk" "h" "c" "cpp" "java" "kt" "gradle" "groovy"
    "vcf" "ics" "desktop" "prefs" "dbus" "xaml" "plist" "info"
    "trace" "dump" "logcat" "out" "err" "stdout" "stderr"
)
ANDROID_PROTECTED_DIRECTORIES=(
    "/" "/sdcard/" "/data/" "/system/" "/bin/" "/sys/" "/proc/" "/etc/"
    "/dev/" "/sbin/" "/vendor/" "/product/" "/apex/" "/boot/" "/storage/"
    "/mnt/" "/oem/" "/odm/" "/cache/" "/root/"
    "/system/bin/" "/system/xbin/" "/system/lib/" "/system/lib64/"
    "/system/app/" "/system/priv-app/" "/system/framework/"
)
currentDeviceId=""
currentDevicePath=""
deviceRooted=false
clipboardPath=""
clipboardOperation=""
clipboardName=""

colorGreen="\033[32m"
colorRed="\033[31m"
colorYellow="\033[33m"
colorReset="\033[0m"

getCurrentTimestamp() {
    (date +%s)
}

runAdbShell() {
    local cmd="$1"
    if [[ "${deviceRooted}" == "true" ]]; then
        if [[ "${cmd}" == *"'"* ]]; then
            # 策略优化：如果命令包含单引号，改用双引号包裹 su -c "..."
            # 避免单引号包裹模式下 ('...') 破坏命令内部已有的双引号结构 (如 "Boy'z")
            # 需对 cmd 中的 \ " $ ` 进行二次转义，以确保在双引号中原样传递
            local escapedCmd="${cmd//\\/\\\\}"
            escapedCmd="${escapedCmd//\"/\\\"}"
            escapedCmd="${escapedCmd//\$/\\\$}"
            escapedCmd="${escapedCmd//\`/\\\`}"
            MSYS_NO_PATHCONV=1 adb -s "${currentDeviceId}" shell "su -c \"${escapedCmd}\"" < /dev/null 2>&1
        else
            # 默认策略：使用单引号包裹 su -c '...'
            # 仅需转义内部的单引号
            local escapedCmd="${cmd//\'/\'\\\'\'}"
            MSYS_NO_PATHCONV=1 adb -s "${currentDeviceId}" shell "su -c '${escapedCmd}'" < /dev/null 2>&1
        fi
    else
        MSYS_NO_PATHCONV=1 adb -s "${currentDeviceId}" shell "${cmd}" < /dev/null 2>&1
    fi
}

checkDeviceRootStatus() {
    echo -e "${colorYellow}⏳ 正在检测 Root 权限...${colorReset}"
    # 尝试执行 su -c id
    # 注意：某些设备可能会弹出Root授权框，需提示用户留意
    # 增加 10 秒超时限制，防止因未授权而无限等待
    local checkResult
    if existCommand "timeout"; then
        checkResult=$(timeout 10s adb -s "${currentDeviceId}" shell "su -c id" < /dev/null 2>&1)
    elif existCommand "gtimeout"; then
        checkResult=$(gtimeout 10s adb -s "${currentDeviceId}" shell "su -c id" < /dev/null 2>&1)
    else
        # 如果没有 timeout 命令，只能回退到无超时机制
        checkResult=$(adb -s "${currentDeviceId}" shell "su -c id" < /dev/null 2>&1)
    fi
    
    if [[ "${checkResult}" =~ uid=0\(root\) ]]; then
        deviceRooted=true
        echo -e "${colorGreen}⚡ 检测到 Root 权限，已自动启用超级用户模式${colorReset}"
    else
        deviceRooted=false
        # 尝试检测su命令是否存在但被拒绝或需要授权
        local suCheck
        suCheck=$(adb -s "${currentDeviceId}" shell "which su" < /dev/null 2>&1)
        if [[ -n "${suCheck}" && ! "${suCheck}" =~ "not found" ]]; then
             echo -e "${colorYellow}💡 检测到 su 命令但无法获取 Root 权限（可能需要手动授权或设备未 Root）${colorReset}"
        else
             echo -e "${colorGreen}💡 未检测到 Root 权限，已使用普通用户模式${colorReset}"
        fi
    fi
}

escapeDevicePath() {
    local rawPath="$1"
    local escaped="${rawPath//\\/\\\\}"
    escaped="${escaped//\"/\\\"}"
    escaped="${escaped//\`/\\\`}"
    escaped="${escaped//\$/\\\$}"
    echo "\"${escaped}\""
}

normalizeDevicePath() {
    local inputPath="$1"
    # 修复：xargs 会合并空格，改用 sed 去除首尾空格
    inputPath=$(echo "${inputPath}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')

    local fullPath=""
    if [[ -z "${inputPath}" ]]; then
        # 空路径 → 直接返回空
        echo ""
        return
    elif [[ "${inputPath}" =~ ^/ ]]; then
         # 以/开头 → 全路径
        fullPath="${inputPath}"
    else
        # 相对路径 → 拼接当前路径
        fullPath="${currentDevicePath%/}/${inputPath}"
    fi

    # 标准化路径：处理 .. 和 . 以及多余斜杠
    # 1. 替换多重斜杠为单斜杠
    fullPath="${fullPath//\/*(\/)/\/}"

    # 2. 使用数组解析路径组件
    local IFS='/'
    local parts
    read -r -a parts <<< "${fullPath}"

    local resolvedParts=()
    for part in "${parts[@]}"; do
        if [[ -z "${part}" || "${part}" == "." ]]; then
            continue
        elif [[ "${part}" == ".." ]]; then
            if [[ ${#resolvedParts[@]} -gt 0 ]]; then
                resolvedParts=("${resolvedParts[@]:0:${#resolvedParts[@]}-1}")
            fi
        else
            resolvedParts+=("${part}")
        fi
    done

    # 3. 重组路径
    local finalPath=""
    if [[ ${#resolvedParts[@]} -eq 0 ]]; then
        finalPath="/"
    else
        for part in "${resolvedParts[@]}"; do
            finalPath="${finalPath}/${part}"
        done
        finalPath="${finalPath}/"
    fi

    echo "${finalPath}"
}

checkPathAccessibility() {
    local checkPath="$1"
    # -d 目录 / -e 存在性检测，默认为 -d
    local checkFlag="${2:--d}"
    local escapePath
    escapePath=$(escapeDevicePath "${checkPath}")
    local checkResult
    # 修复：escapePath 已包含转义字符，不应再加单引号
    checkResult=$(runAdbShell "[ ${checkFlag} ${escapePath} ] && echo available" | tr -d '\r')
    if [[ "${checkResult}" == "available" ]]; then
        return 0
    else
        echo -e "${colorRed}❌ 路径不存在或无访问权限：${checkPath}${colorReset}"
        return 1
    fi
}

getFileSuffixName() {
    local fileName="$1"
    echo "${fileName##*.}" | tr '[:upper:]' '[:lower:]'
}

isTextFile() {
    local fileName="$1"
    local fileExt
    fileExt=$(getFileSuffixName "${fileName}")
    # 无后缀名的系统文件，默认判定为文本
    if [[ -z "${fileExt}" ]]; then
        return 0
    fi
    # 匹配白名单（修复循环语法）
    for ext in "${ANDROID_TEXT_EXTENSIONS[@]}"; do
        if [[ "${fileExt}" == "${ext}" ]]; then
            return 0
        fi
    done
    return 1
}

isProtectedDir() {
    local dirPath="$1"
    # 确保路径以/结尾（用于匹配白名单）
    if [[ "${dirPath}" != */ ]]; then
        dirPath="${dirPath}/"
    fi
    
    for protected in "${ANDROID_PROTECTED_DIRECTORIES[@]}"; do
        if [[ "${dirPath}" == "${protected}" ]]; then
            return 0
        fi
    done
    return 1
}

getFileSize() {
    local fileFullPath="$1"
    local escapePath
    escapePath=$(escapeDevicePath "${fileFullPath}")
    local lsOutput
    lsOutput=$(runAdbShell "ls -l ${escapePath}" | tr -d '\r')
    local fileSizeBytes
    fileSizeBytes=$(echo "${lsOutput}" | awk '{print $5}')
    # 校验是否为数字（修复语法）
    if [[ ! "${fileSizeBytes}" =~ ^[0-9]+$ ]]; then
        echo -1
        return
    fi
    echo "${fileSizeBytes}"
}

listSimpleContent() {
    local targetPath="$1"
    local pathDescription="$2"
    local escapePath
    escapePath=$(escapeDevicePath "${targetPath}")
    if ! checkPathAccessibility "${targetPath}"; then
        return 1
    fi

    echo -e "\n📋 ${pathDescription} 内容："
    echo "${DIVIDING_LINE}"
    local dirContent
    # 优化：使用 ls -1ap 直接标识目录（带/后缀），避免循环调用 adb 检测
    # -a 显示隐藏文件，-p 给目录加斜杠 (过滤 ./ 和 ../)
    dirContent=$(runAdbShell "ls -1ap ${escapePath}" | tr -d '\r' | grep -v '^$' | grep -v '^\./$' | grep -v '^\.\./$')
    if [[ -z "${dirContent}" ]]; then
        echo -e "  ${colorYellow}💡 当前目录为空${colorReset}"
        return 0
    fi

    local fileList=()
    local dirList=()
    
    while read -r name; do
        if [[ "${name}" == */ ]]; then
            # 是目录，去掉末尾的 /
            dirList+=("${name%/}")
        else
            # 是文件
            fileList+=("${name}")
        fi
    done <<< "${dirContent}"

    for name in "${dirList[@]}"; do
        echo -e " ${DIR_ICON_EMOJI} ${name}"
    done

    for name in "${fileList[@]}"; do
        echo -e " ${FILE_ICON_EMOJI} ${name}"
    done
    return 0
}

listDetailContent() {
    local targetPath="$1"
    local escapePath
    escapePath=$(escapeDevicePath "${targetPath}")
    # ls -l 可能针对文件，所以使用 -e 检测存在性即可
    if ! checkPathAccessibility "${targetPath}" "-e"; then
        return 1
    fi

    # 获取目标本身的详细信息
    local targetSelfInfo
    targetSelfInfo=$(runAdbShell "ls -ld ${escapePath}" | tr -d '\r')
    
    if [[ -z "${targetSelfInfo}" ]]; then
        echo -e "${colorYellow}💡 无法获取文件信息${colorReset}"
        return 1
    fi

    # 解析目标本身的信息
    local targetLine="${targetSelfInfo}"
    local permission
    permission=$(echo "${targetLine}" | awk '{print $1}')
    local sizeBytes
    sizeBytes=$(echo "${targetLine}" | awk '{print $5}')
    local sizeKb=$(( (sizeBytes + 1023) / 1024 ))

    # 智能解析时间
    local modifyTime
    local dateField
    dateField=$(echo "${targetLine}" | awk '{print $6}')
    if [[ "${dateField}" =~ ^[0-9]{4}- ]]; then
        # YYYY-MM-DD 格式 (共2列时间)
        modifyTime=$(echo "${targetLine}" | awk '{print $6" "$7}')
    else
        # Mon DD Time 格式 (共3列时间)
        modifyTime=$(echo "${targetLine}" | awk '{print $6" "$7" "$8}')
    fi
    
    local displayName
    displayName=$(basename "${targetPath}")

    if [[ "${permission:0:1}" != "d" ]]; then
        echo -e "文件名称：${displayName}"
        echo -e "文件大小：${sizeKb} KB"
        echo -e "文件权限：${permission}"
        echo -e "修改时间：${modifyTime}"
        echo -e "存放路径：${targetPath}"
        return 0
    fi

    # 获取终端宽度
    local termCols=""
    # 1. 优先尝试 stty size (macOS/Linux 兼容性较好，且能实时获取窗口大小)
    if command -v stty >/dev/null 2>&1; then
        local sttySize
        sttySize=$(stty size 2>/dev/null)
        if [[ -n "$sttySize" ]]; then
            termCols=$(echo "$sttySize" | awk '{print $2}')
        fi
    fi

    # 2. 其次尝试 tput cols
    if [[ -z "$termCols" ]] && command -v tput >/dev/null 2>&1; then
        termCols=$(tput cols 2>/dev/null)
    fi

    # 3. 尝试环境变量
    if [[ -z "$termCols" ]]; then
        termCols="${COLUMNS}"
    fi

    # 4. 默认兜底 (如果获取失败，默认给宽一点，避免截断太严重)
    if [[ -z "$termCols" || "$termCols" -lt 40 ]]; then
        termCols=120
    fi

    # 定义右侧元数据列宽 (Size:8 + Gap:2 + Perms:10 + Gap:2 + Time:16 = 38)
    local metaWidth=40
    # 计算名称列宽
    local nameWidth=$((termCols - metaWidth))
    if [[ $nameWidth -lt 20 ]]; then nameWidth=20; fi # 最小宽度保护

    # 使用 du -ak 获取递归大小和文件列表，结合 ls -ld 获取详情
    local cmd="cd ${escapePath} && du -ak 2>/dev/null | sort -k2 | while read size path; do echo \"\$size|\$(ls -ld \"\$path\")\"; done"
    local dirContent
    dirContent=$(runAdbShell "${cmd}" | tr -d '\r')

    if [[ -z "${dirContent}" ]]; then
        return 0
    fi

    echo "${dirContent}" | while read -r line; do
        if [[ -z "${line}" ]]; then continue; fi

        # 格式: Size(KB)|drwxrwx--x ...
        local sizeKb="${line%%|*}"
        local lsInfo="${line#*|}"

        # 解析 ls -ld 输出
        local permission
        permission=$(echo "${lsInfo}" | awk '{print $1}')
        local displaySize="${sizeKb}KB"

        # 解析时间
        local dateField
        dateField=$(echo "${lsInfo}" | awk '{print $6}')
        local modifyTime=""
        local nameRaw=""

        if [[ "${dateField}" =~ ^[0-9]{4}- ]]; then
            # YYYY-MM-DD HH:MM
            modifyTime=$(echo "${lsInfo}" | awk '{print $6" "$7}')
            nameRaw=$(echo "${lsInfo}" | awk '{$1=$2=$3=$4=$5=$6=$7=""; print $0}' | sed 's/^ *//g')
        else
            # Mon DD HH:MM
            modifyTime=$(echo "${lsInfo}" | awk '{print $6" "$7" "$8}')
            nameRaw=$(echo "${lsInfo}" | awk '{$1=$2=$3=$4=$5=$6=$7=$8=""; print $0}' | sed 's/^ *//g')
        fi

        # 处理路径和缩进
        local cleanPath="${nameRaw}"
        if [[ "${cleanPath}" == "." || "${cleanPath}" == "./" ]]; then
            # 根节点已在上方打印，此处跳过
            # User requested to skip printing the root node completely for 'ls -l' on a directory
            continue
        else
            # 子节点
            cleanPath="${cleanPath#./}"

            # 计算缩进
            local depth=
            depth=$(echo "${cleanPath}" | awk -F"/" '{print NF-1}')
            local indent=""
            for ((i=0; i<=depth; i++)); do
                indent+="  "
            done

            local subDisplayName
            subDisplayName=$(basename "${cleanPath}")

            local subIcon="${FILE_ICON_EMOJI}"
            if [[ "${permission:0:1}" == "d" ]]; then
                subIcon="${DIR_ICON_EMOJI}"
            fi

            local nameColumn="${indent}${subIcon} ${subDisplayName}"

            # 长度检查与截断 (按显示宽度)
            local realWidth
            realWidth=$(getDisplayWidth "${nameColumn}")

            if [[ ${realWidth} -gt $((nameWidth - 1)) ]]; then
                local cutLen=$((nameWidth - 4))
                local tmpStr="${nameColumn}"
                while [[ $(getDisplayWidth "${tmpStr}") -gt ${cutLen} ]]; do
                    tmpStr="${tmpStr%?}"
                done
                nameColumn="${tmpStr}..."
                realWidth=$(getDisplayWidth "${nameColumn}")
            fi

            # 计算填充空格
            local paddingNumber=$((nameWidth - realWidth))
            local paddingSpaces=""
            if [[ ${paddingNumber} -gt 0 ]]; then
                paddingSpaces=$(printf "%${paddingNumber}s" "")
            fi

            printf "%s%s %-8s %-10s %s\n" "${nameColumn}" "${paddingSpaces}" "${displaySize}" "${permission}" "${modifyTime}"
        fi
    done
    return 0
}

changeDirectory() {
    local pathParameter="$1"
    local targetPath=""
    local startTime
    startTime=$(getCurrentTimestamp)

    echo -e "${colorYellow}⏳ 正在切换目录...${colorReset}"
    targetPath=$(normalizeDevicePath "${pathParameter}")

    # 核心语法修复：将[[ ... && 命令 ]]拆分为两个独立判定
    if [[ -n "${targetPath}" ]] && checkPathAccessibility "${targetPath}"; then
        currentDevicePath="${targetPath}"
        local endTime
        endTime=$(getCurrentTimestamp)
        local costTime=$((endTime - startTime))
        echo -e "${colorGreen}✅ 目录切换成功（耗时：${costTime}s）${colorReset}"
        listSimpleContent "${currentDevicePath}" "当前目录"
        return 0
    else
        local endTime
        endTime=$(getCurrentTimestamp)
        local costTime=$((endTime - startTime))
        echo -e "${colorRed}❌ 目录切换失败（耗时：${costTime}s）${colorReset}"
        return 1
    fi
}

viewTextFile() {
    local fileName="$1"
    local startTime
    startTime=$(getCurrentTimestamp)
    if [[ -z "${fileName}" ]]; then
        echo -e "${colorRed}❌ 命令用法错误：cat 需指定文本文件名，示例：cat build.prop${colorReset}"
        return 1
    fi
    local fileFullPath="${currentDevicePath}${fileName}"
    local escapePath
    escapePath=$(escapeDevicePath "${fileFullPath}")

    echo -e "${colorYellow}⏳ 正在加载文件...${colorReset}"
    local existCheck
    existCheck=$(runAdbShell "[ -e ${escapePath} ] && echo exist" | tr -d '\r')
    if [[ "${existCheck}" != "exist" ]]; then
        echo -e "${colorRed}❌ 设备当前路径无此文件：${fileName}${colorReset}"
        return 1
    fi
    if runAdbShell "[ -d ${escapePath} ]"; then
        echo -e "${colorRed}❌ 命令不支持：cat仅可查看文件，不可查看文件夹${colorReset}"
        return 1
    fi
    local fileSize
    fileSize=$(getFileSize "${fileFullPath}")
    if [[ "${fileSize}" == "-1" ]]; then
        echo -e "${colorRed}❌ 无法获取文件大小：${fileName}（设备不支持或无权限）${colorReset}"
        return 1
    fi
    local maxCatFileSizeInKb=2048
    local maxCatFileSizeInByte=$((maxCatFileSizeInKb * 1024))
    if [[ "${fileSize}" -gt "${maxCatFileSizeInByte}" ]]; then
        echo -e "${colorRed}❌ 文件过大，禁止查看：${fileName}（大小：${fileSize} Byte，最大支持：${maxCatFileSizeInKb} KB）${colorReset}"
        return 1
    fi
    if ! isTextFile "${fileName}"; then
        echo -e "${colorRed}❌ 非合法文本格式，禁止查看：${fileName}${colorReset}"
        echo -e "${colorYellow}💡 支持的格式：${ANDROID_TEXT_EXTENSIONS[*]}${colorReset}"
        return 1
    fi

    local endTime
    endTime=$(getCurrentTimestamp)
    local costTime=$((endTime - startTime))
    echo -e "${colorGreen}✅ 正在查看文件：${fileName}（大小：${fileSize} Byte，加载耗时：${costTime}s）${colorReset}"
    echo "${DIVIDING_LINE}"
    echo -e "\n"
    local fileContent
    fileContent=$(runAdbShell "cat ${escapePath}" | tr -d '\r')
    if [[ -z "${fileContent}" ]]; then
        echo -e "${colorYellow}💡 文件为空，无内容可查看${colorReset}"
    else
        echo "${fileContent}"
    fi
    echo -e "\n"
    echo "${DIVIDING_LINE}"
    echo -e "${colorGreen}✅ 文件查看完成${colorReset}"
    return 0
}

findFileInDirectory() {
    local searchKey="$1"
    local startTime
    startTime=$(getCurrentTimestamp)
    if [[ -z "${searchKey}" ]]; then
        echo -e "${colorRed}❌ 命令用法错误：find 需指定搜索关键词，示例：find build、find log${colorReset}"
        return 1
    fi
    local targetPath="${currentDevicePath}"
    local escapePath
    escapePath=$(escapeDevicePath "${targetPath}")

    echo -e "${colorYellow}⏳ 正在搜索【${searchKey}】...（当前目录：${targetPath}）${colorReset}"
    local searchResult
    searchResult=$(runAdbShell "ls -1 ${escapePath} | grep -i \"${searchKey}\" | grep -v \"^\\$\"" | tr -d '\r')
    local endTime
    endTime=$(getCurrentTimestamp)
    local costTime=$((endTime - startTime))
    echo -e "\n📋 搜索结果（耗时：${costTime}s，关键词：${searchKey}）："
    echo "${DIVIDING_LINE}"
    if [[ -z "${searchResult}" ]]; then
        echo -e "  ${colorYellow}💡 未找到匹配的文件/文件夹${colorReset}"
        return 0
    fi
    local escapedFullPath
    echo "${searchResult}" | while read -r name; do
        local fullPath="${targetPath}${name}"
        escapedFullPath=$(escapeDevicePath "${fullPath}")
        if runAdbShell "[ -d ${escapedFullPath} ]"; then
            echo -e "  ${DIR_ICON_EMOJI}  ${name}"
        fi
    done
    echo "${searchResult}" | while read -r name; do
        local fullPath="${targetPath}${name}"
        local escapedFullPath
        escapedFullPath=$(escapeDevicePath "${fullPath}")
        if runAdbShell "[ -f ${escapedFullPath} ]"; then
            echo "  ${FILE_ICON_EMOJI}  ${name}"
        fi
    done
    return 0
}

removeTarget() {
    local commandParameter="$1"
    local targetName="$2"
    local isRecursive=0
    local targetFullPath=""
    local targetType=""
    local startTime
    startTime=$(getCurrentTimestamp)

    if [[ "${commandParameter}" == "-r" ]]; then
        if [[ -z "${targetName}" ]]; then
            echo -e "${colorRed}❌ 命令用法错误：rm -r 需指定文件夹名，示例：rm -r testDir${colorReset}"
            return 1
        fi
        isRecursive=1
        targetFullPath="${currentDevicePath}${targetName}"
        targetType="文件夹（含所有子内容）"
    else
        if [[ -z "${commandParameter}" ]]; then
            echo -e "${colorRed}❌ 命令用法错误：rm 需指定文件名，示例：rm log.txt${colorReset}"
            return 1
        fi
        targetName="${commandParameter}"
        targetFullPath="${currentDevicePath}${targetName}"
        targetType="文件"
    fi

    local escapePath
    escapePath=$(escapeDevicePath "${targetFullPath}")
    local existCheck
    existCheck=$(runAdbShell "[ -e ${escapePath} ] && echo exist" | tr -d '\r')
    if [[ "${existCheck}" != "exist" ]]; then
        echo -e "${colorRed}❌ 设备当前路径无此目标：${targetName}${colorReset}"
        return 1
    fi

    if [[ ${isRecursive} -eq 0 ]]; then
        if runAdbShell "[ -d ${escapePath} ]"; then
            echo -e "${colorRed}❌ 命令不支持：rm 仅可删除文件，删除文件夹请使用 rm -r ${targetName}${colorReset}"
            return 1
        fi
    else
        if runAdbShell "[ -f ${escapePath} ]"; then
            echo -e "${colorRed}❌ 命令不支持：rm -r 仅可删除文件夹，删除文件请直接使用 rm ${targetName}${colorReset}"
            return 1
        fi
    fi

    # 安全检查：是否为系统保护目录
    local checkPath
    checkPath=$(normalizeDevicePath "${targetName}")
    if isProtectedDir "${checkPath}"; then
        echo -e "${colorRed}❌ 操作被禁止：无法删除系统关键目录 ${checkPath}${colorReset}"
        echo -e "${colorYellow}💡 保护名单：${ANDROID_PROTECTED_DIRECTORIES[*]}${colorReset}"
        return 1
    fi

    local confirmTip="👻 确认删除【${targetType}】${targetName} 吗？(y/n)："
    echo -e "${colorYellow}${confirmTip}${colorReset}"
    read -r confirmInput
    if [[ ! "${confirmInput}" =~ ^[yY]$ ]]; then
        echo -e "\n${colorYellow}💡 已取消删除命令：${targetName}${colorReset}"
        return 1
    fi

    echo -e "${colorYellow}⏳ 正在删除...${colorReset}"
    local removeCommand="rm"
    if [[ ${isRecursive} -eq 1 ]]; then
        removeCommand="rm -r"
    fi
    runAdbShell "${removeCommand} ${escapePath}"
    local exitCode=$?
    local endTime
    endTime=$(getCurrentTimestamp)
    local costTime=$((endTime - startTime))
    if [[ ${exitCode} -eq 0 ]]; then
        echo -e "${colorGreen}✅ 【${targetType}】${targetName} 删除成功（耗时：${costTime}s）${colorReset}"
        listSimpleContent "${currentDevicePath}" "当前目录"
    else
        echo -e "${colorRed}❌ 【${targetType}】${targetName} 删除失败（耗时：${costTime}s）${colorReset}"
    fi
    return ${exitCode}
}

pullFromDevice() {
    local targetName="$1"
    local startTime
    startTime=$(getCurrentTimestamp)
    if [[ -z "${targetName}" ]]; then
        echo -e "${colorRed}❌ 命令用法错误：pull 需指定文件/文件夹名，示例：pull test.apk${colorReset}"
        return 1
    fi
    local deviceFullPath="${currentDevicePath}${targetName}"
    # pull/push 命令不需要 shell 转义，直接使用引用
    local escapeDevicePathForCheck
    escapeDevicePathForCheck=$(escapeDevicePath "${deviceFullPath}")

    echo -e "${colorYellow}⏳ 正在导出...${colorReset}"
    local existCheck
    existCheck=$(runAdbShell "[ -e ${escapeDevicePathForCheck} ] && echo exist" | tr -d '\r')
    if [[ "${existCheck}" != "exist" ]]; then
        echo -e "${colorRed}❌ 设备当前路径无此目标：${targetName}${colorReset}"
        return 1
    fi

    local workDirPath
    workDirPath=$(getWorkDirPath)
    echo -e "📂 请输入导出到本地的文件夹路径（可空，默认当前目录）："
    read -r localTargetDir
    localTargetDir=$(parseComputerFilePath "${localTargetDir}")
    if [[ -z "${localTargetDir}" ]]; then
        localTargetDir="${workDirPath}"
    fi

    # 处理 ~ 路径
    if [[ "${localTargetDir}" == ~* ]]; then
        localTargetDir="${HOME}${localTargetDir:1}"
    fi

    # 创建目标文件夹（如果不存在）
    if [[ ! -d "${localTargetDir}" ]]; then
        mkdir -p "${localTargetDir}"
    fi

    # 注意：adb pull 在非Root模式下可能无法导出受保护文件，即使前面检测到了文件存在（通过Root）
    # 如果 deviceRooted 为true且 adb pull失败，可能需要先拷贝到/sdcard再pull，此处暂时保持原样
    MSYS_NO_PATHCONV=1 adb -s "${currentDeviceId}" pull "${deviceFullPath}" "${localTargetDir}" < /dev/null 2>&1
    local exitCode=$?
    local endTime
    endTime=$(getCurrentTimestamp)
    local costTime=$((endTime - startTime))
    if [[ ${exitCode} -eq 0 ]]; then
        echo -e "${colorGreen}✅ 导出成功：${targetName}（保存路径：${localTargetDir}，耗时：${costTime}s）${colorReset}"
    else
        echo -e "${colorRed}❌ 导出失败：${targetName}（耗时：${costTime}s）${colorReset}"
        if [[ "${deviceRooted}" == "true" ]]; then
             echo -e "${colorYellow}💡 Root 模式下直接 Pull 系统文件可能会失败，建议先复制到 /sdcard${colorReset}"
        fi
    fi
    return ${exitCode}
}

pushToDevice() {
    local localFullPath="$1"
    localFullPath=$(parseComputerFilePath "${localFullPath}")
    local startTime
    startTime=$(getCurrentTimestamp)
    if [[ -z "${localFullPath}" ]]; then
        echo -e "${colorRed}❌ 命令用法错误：push 需指定文件/文件夹名，示例：push app.apk${colorReset}"
        return 1
    fi
    # 获取文件名称
    local baseName
    baseName=$(basename "${localFullPath}")

    local deviceFullPath="${currentDevicePath}${baseName}"
    local escapeDevicePathForCheck
    escapeDevicePathForCheck=$(escapeDevicePath "${deviceFullPath}")

    echo -e "${colorYellow}⏳ 正在导入...${colorReset}"
    if [[ ! -e "${localFullPath}" ]]; then
        echo -e "${colorRed}❌ 找不到此目标：${localFullPath}${colorReset}"
        return 1
    fi

    MSYS_NO_PATHCONV=1 adb -s "${currentDeviceId}" push "${localFullPath}" "${deviceFullPath}" < /dev/null 2>&1
    local exitCode=$?
    local endTime
    endTime=$(getCurrentTimestamp)
    local costTime=$((endTime - startTime))
    if [[ ${exitCode} -eq 0 ]]; then
        echo -e "${colorGreen}✅ 导入成功：${baseName}（设备路径：${deviceFullPath}，耗时：${costTime}s）${colorReset}"
        listSimpleContent "${currentDevicePath}" "当前目录"
    else
        echo -e "${colorRed}❌ 导入失败：${baseName}（耗时：${costTime}s）${colorReset}"
    fi
    return ${exitCode}
}

changeFileMode() {
    local permissionValue="$1"
    local targetName="$2"
    local startTime
    startTime=$(getCurrentTimestamp)
    if [[ -z "${permissionValue}" || -z "${targetName}" ]]; then
        echo -e "${colorRed}❌ 命令用法错误：chmod 需指定3位权限+目标名，示例：chmod 777 test.apk${colorReset}"
        return 1
    fi
    if [[ ! "${permissionValue}" =~ ^[0-7]{3}$ ]]; then
        echo -e "${colorRed}❌ 权限值非法：请输入3位数字（000-777），示例：777/644${colorReset}"
        return 1
    fi
    local deviceFullPath="${currentDevicePath}${targetName}"
    local escapePath
    escapePath=$(escapeDevicePath "${deviceFullPath}")

    echo -e "${colorYellow}⏳ 正在修改权限...${colorReset}"
    local existCheck
    existCheck=$(runAdbShell "[ -e ${escapePath} ] && echo exist" | tr -d '\r')
    if [[ "${existCheck}" != "exist" ]]; then
        echo -e "${colorRed}❌ 设备当前路径无此目标：${targetName}${colorReset}"
        return 1
    fi

    runAdbShell "chmod ${permissionValue} ${escapePath}"
    local exitCode=$?
    local endTime
    endTime=$(getCurrentTimestamp)
    local costTime=$((endTime - startTime))
    if [[ ${exitCode} -eq 0 ]]; then
        echo -e "${colorGreen}✅ 权限修改成功：${targetName} → ${permissionValue}（耗时：${costTime}s）${colorReset}"
    else
        echo -e "${colorRed}❌ 权限修改失败：${targetName}（耗时：${costTime}s）${colorReset}"
    fi
    return ${exitCode}
}

makeDirectory() {
    local directoryName="$1"
    local startTime
    startTime=$(getCurrentTimestamp)
    if [[ -z "${directoryName}" ]]; then
        echo -e "${colorRed}❌ 命令用法错误：mkdir 需指定文件夹名，示例：mkdir newDir${colorReset}"
        return 1
    fi
    if [[ "${directoryName}" =~ / ]]; then
        echo -e "${colorRed}❌ 命令用法错误：mkdir 仅支持在当前路径下创建文件夹，不支持包含路径分隔符${colorReset}"
        return 1
    fi
    local deviceFullPath=""
    local pathDescription=""

    echo -e "${colorYellow}⏳ 正在创建文件夹...${colorReset}"
    deviceFullPath=$(normalizeDevicePath "${directoryName}")
    local escapePath
    escapePath=$(escapeDevicePath "${deviceFullPath}")
    pathDescription="当前路径 ${deviceFullPath}"

    local existCheck
    existCheck=$(runAdbShell "[ -d ${escapePath} ] && echo exist" | tr -d '\r')
    if [[ "${existCheck}" == "exist" ]]; then
        echo -e "${colorYellow}💡 文件夹已存在 ${pathDescription}${colorReset}"
        return 1
    fi

    runAdbShell "mkdir -p ${escapePath}"
    local exitCode=$?
    local endTime
    endTime=$(getCurrentTimestamp)
    local costTime=$((endTime - startTime))
    if [[ ${exitCode} -eq 0 ]]; then
        echo -e "${colorGreen}✅ 文件夹创建成功：${pathDescription}（耗时：${costTime}s）${colorReset}"
        currentDevicePath="${deviceFullPath}"
        echo -e "${colorGreen}📂 已自动切换到新目录：${currentDevicePath}${colorReset}"
        listSimpleContent "${currentDevicePath}" "当前目录"
    else
        echo -e "${colorRed}❌ 文件夹创建失败：${pathDescription}（耗时：${costTime}s）${colorReset}"
    fi
    return ${exitCode}
}

cutTarget() {
    local targetName="$1"
    if [[ -z "${targetName}" ]]; then
        echo -e "${colorRed}❌ 命令用法错误：cut 需指定文件/文件夹名，示例：cut file.txt${colorReset}"
        return 1
    fi
    local targetFullPath="${currentDevicePath}${targetName}"
    
    # 检查目标是否存在
    local escapePath
    escapePath=$(escapeDevicePath "${targetFullPath}")
    local existCheck
    existCheck=$(runAdbShell "[ -e ${escapePath} ] && echo exist" | tr -d '\r')
    if [[ "${existCheck}" != "exist" ]]; then
        echo -e "${colorRed}❌ 设备当前路径无此目标：${targetName}${colorReset}"
        return 1
    fi

    # 检查是否为保护目录
    local checkPath
    checkPath=$(normalizeDevicePath "${targetName}")
    if isProtectedDir "${checkPath}"; then
        echo -e "${colorRed}❌ 操作被禁止：无法剪切系统关键目录 ${checkPath}${colorReset}"
        echo -e "${colorYellow}💡 保护名单：${ANDROID_PROTECTED_DIRECTORIES[*]}${colorReset}"
        return 1
    fi

    clipboardPath="${targetFullPath}"
    clipboardName="${targetName}"
    clipboardOperation="cut"
    echo -e "${colorGreen}✂️  已将【${targetName}】加入剪切板（请切换到目标目录输入 paste 完成移动）${colorReset}"
    return 0
}

copyTarget() {
    local targetName="$1"
    if [[ -z "${targetName}" ]]; then
        echo -e "${colorRed}❌ 命令用法错误：copy 需指定文件/文件夹名，示例：copy file.txt${colorReset}"
        return 1
    fi
    local targetFullPath="${currentDevicePath}${targetName}"
    
    # 检查目标是否存在
    local escapePath
    escapePath=$(escapeDevicePath "${targetFullPath}")
    local existCheck
    existCheck=$(runAdbShell "[ -e ${escapePath} ] && echo exist" | tr -d '\r')
    if [[ "${existCheck}" != "exist" ]]; then
        echo -e "${colorRed}❌ 设备当前路径无此目标：${targetName}${colorReset}"
        return 1
    fi

    clipboardPath="${targetFullPath}"
    clipboardName="${targetName}"
    clipboardOperation="copy"
    echo -e "${colorGreen}📋 已将【${targetName}】加入复制板（请切换到目标目录输入 paste 完成复制）${colorReset}"
    return 0
}

pasteTarget() {
    if [[ -z "${clipboardPath}" ]]; then
        echo -e "${colorRed}❌ 剪切板为空，请先执行 cut 或 copy${colorReset}"
        return 1
    fi

    local startTime
    startTime=$(getCurrentTimestamp)
    local sourcePath="${clipboardPath}"
    local sourceName="${clipboardName}"
    # 目标路径 = 当前目录 + 源文件名
    local targetFullPath="${currentDevicePath}${sourceName}"
    
    if [[ "${sourcePath}" == "${targetFullPath}" ]]; then
         echo -e "${colorRed}❌ 源路径与目标路径相同，无法操作${colorReset}"
         return 1
    fi

    local escapedSource
    escapedSource=$(escapeDevicePath "${sourcePath}")
    local escapedDest
    escapedDest=$(escapeDevicePath "${targetFullPath}")
    
    # 检查源文件是否存在
    local existCheck
    existCheck=$(runAdbShell "[ -e ${escapedSource} ] && echo exist" | tr -d '\r')
    if [[ "${existCheck}" != "exist" ]]; then
        echo -e "${colorRed}❌ 源文件已不存在：${sourcePath}${colorReset}"
        clipboardPath=""
        clipboardOperation=""
        clipboardName=""
        return 1
    fi

    # 检查目标是否存在
    local destCheck
    destCheck=$(runAdbShell "[ -e ${escapedDest} ] && echo exist" | tr -d '\r')
    if [[ "${destCheck}" == "exist" ]]; then
         echo -e "${colorYellow}👻 目标位置已存在同名文件/文件夹：${sourceName}${colorReset}"
         echo -e "是否覆盖/合并？(y/n)："
         read -r confirmInput
         if [[ ! "${confirmInput}" =~ ^[yY]$ ]]; then
             echo -e "${colorYellow}💡 已取消操作${colorReset}"
             return 1
         fi
    fi

    echo -e "${colorYellow}⏳ 正在执行 ${clipboardOperation} 操作...${colorReset}"
    
    if [[ "${clipboardOperation}" == "cut" ]]; then
        runAdbShell "mv ${escapedSource} ${escapedDest}"
        local exitCode=$?
        local endTime
        endTime=$(getCurrentTimestamp)
        local costTime=$((endTime - startTime))
        
        if [[ ${exitCode} -eq 0 ]]; then
             echo -e "${colorGreen}✅ 剪切成功：${sourceName}（耗时：${costTime}s）${colorReset}"
             clipboardPath="" # 剪切完成后清空
             clipboardOperation=""
             clipboardName=""
             listSimpleContent "${currentDevicePath}" "当前目录"
        else
             echo -e "${colorRed}❌ 剪切失败（耗时：${costTime}s）${colorReset}"
        fi
        return ${exitCode}
        
    elif [[ "${clipboardOperation}" == "copy" ]]; then
        # cp -r 
        runAdbShell "cp -r ${escapedSource} ${escapedDest}"
        local exitCode=$?
        local endTime
        endTime=$(getCurrentTimestamp)
        local costTime=$((endTime - startTime))
        
        if [[ ${exitCode} -eq 0 ]]; then
             echo -e "${colorGreen}✅ 复制成功：${sourceName}（耗时：${costTime}s）${colorReset}"
             # 复制后不清空，允许连续粘贴
             listSimpleContent "${currentDevicePath}" "当前目录"
        else
             echo -e "${colorRed}❌ 复制失败（耗时：${costTime}s）${colorReset}"
        fi
        return ${exitCode}
    fi
}

getDisplayWidth() {
    local str="$1"
    if [[ -z "${str}" ]]; then
        echo 0
        return
    fi
    
    # 临时设置 locale 确保 wc -m 正确处理 UTF-8
    # 保存旧的 LC_CTYPE (如果存在)
    local originalLcCtype="${LC_CTYPE}"
    export LC_CTYPE=en_US.UTF-8
    
    # 1. 计算字符总数 (wc -m)
    local charCount
    charCount=$(printf "%s" "${str}" | wc -m | tr -d ' ')
    
    # 2. 计算非ASCII字符数 (移除ASCII后统计)
    # ASCII范围 \000-\177 (八进制)
    local nonAsciiCount
    nonAsciiCount=$(printf "%s" "${str}" | tr -d '\000-\177' | wc -m | tr -d ' ')
    
    # 恢复环境 (虽然 subshell 不影响外部，但保持良好习惯)
    if [[ -n "${originalLcCtype}" ]]; then
        export LC_CTYPE="${originalLcCtype}"
    else
        unset LC_CTYPE
    fi
    
    # 视觉宽度 = 字符数 + 非ASCII字符数
    # (假设非ASCII字符宽度均为2，ASCII为1)
    echo $((charCount + nonAsciiCount))
}

drawInterface() {
    echo "${DIVIDING_LINE}"
    echo " 当前操作路径：${currentDevicePath}"
    echo "---------------------------------- 操作指南 -----------------------------------"
    echo "|   命令   | 介绍                                    |          示例          |"
    echo "|:--------:|:----------------------------------------|:----------------------:|"
    echo "|    cd    | 切换目录 (支持切子路径和返回上一级目录) |  cd Download、cd ../   |"
    echo "|    ls    | 查看目录 (支持看子路径和上一级目录的)   |   ls、ls dir、ls ../   |"
    echo "|  ls -l   | 查看文件/文件夹详细属性                 |         ls -l          |"
    echo "|   cat    | 查看文本文件内容（大小须在 1MB 内)      |      cat test.txt      |"
    echo "|   find   | 模糊搜索当前目录文件/文件夹             |       find build       |"
    echo "|   pull   | 导出设备文件/文件夹到电脑               |     pull test.apk      |"
    echo "|   push   | 导入电脑文件/文件夹到设备               |      push app.apk      |"
    echo "|    rm    | 仅删除文件 (不支持文件夹)               |       rm log.txt       |"
    echo "|  rm -r   | 仅删除文件夹 (含所有子内容)             |     rm -r testDir      |"
    echo "|   cut    | 剪切文件/文件夹 (搭配 paste 命令使用)   |      cut file.txt      |"
    echo "|   copy   | 复制文件/文件夹 (搭配 paste 命令使用)   |     copy file.txt      |"
    echo "|  paste   | 粘贴剪切板内容到当前目录                |         paste          |"
    echo "|  chmod   | 修改文件/文件夹3位权限                  |   chmod 777 test.apk   |"
    echo "|  mkdir   | 创建文件夹 (仅限当前目录)               |      mkdir newDir      |"
    echo "|   exit   | 退出文件管理器                          |          exit          |"
    echo "${DIVIDING_LINE}"
    echo -n "请输入操作命令："
}

parseInputCommand() {
    local inputCommand="$1"
    # 修复：去除首尾空格
    inputCommand=$(echo "${inputCommand}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
    if [[ -z "${inputCommand}" ]]; then
        return 0
    fi

    # 提取主命令（转小写）
    local mainCommand
    mainCommand=$(echo "${inputCommand}" | awk '{print $1}' | tr '[:upper:]' '[:lower:]')
    
    # 提取参数（保留原始大小写和空格）
    local args=""
    if [[ "${inputCommand}" =~ ^[^[:space:]]*[[:space:]]*(.*) ]]; then
        args="${BASH_REMATCH[1]}"
    fi

    case "${mainCommand}" in
        "cd")
            changeDirectory "${args}"
            ;;
        "ls")
            local isDetail=false
            local target=""
            
            # 解析参数：支持 ls -l [path] 或 ls [path]
            if [[ "${args}" =~ ^-l[[:space:]]*(.*) ]]; then
                isDetail=true
                target="${BASH_REMATCH[1]}"
            elif [[ "${args}" == "-l" ]]; then
                isDetail=true
                target=""
            else
                isDetail=false
                target="${args}"
            fi

            # 路径解析
            local targetFullPath=""
            if [[ -z "${target}" ]]; then
                targetFullPath="${currentDevicePath}"
            elif [[ "${target}" =~ ^/ ]]; then
                targetFullPath="${target}"
            else
                targetFullPath="${currentDevicePath}${target}"
            fi

            if [[ "${isDetail}" == "true" ]]; then
                listDetailContent "${targetFullPath}"
            else
                listSimpleContent "${targetFullPath}" "${target:-当前目录}"
            fi
            ;;
        "cat")
            viewTextFile "${args}"
            ;;
        "find")
            findFileInDirectory "${args}"
            ;;
        "pull")
            pullFromDevice "${args}"
            ;;
        "push")
            pushToDevice "${args}"
            ;;
        "rm")
            # 特殊处理 rm -r
            if [[ "${args}" =~ ^-r[[:space:]]*(.*) ]]; then
                local targetDir="${BASH_REMATCH[1]}"
                removeTarget "-r" "${targetDir}"
            elif [[ "${args}" == "-r" ]]; then
                removeTarget "-r" ""
            else
                removeTarget "${args}"
            fi
            ;;
        "chmod")
            # chmod [权限] [文件名]
            local permission
            permission=$(echo "${args}" | awk '{print $1}')
            local file=""
            if [[ "${args}" =~ [[:space:]] ]]; then
                file=$(echo "${args}" | sed -E "s/^${permission}[[:space:]]+//")
            fi
            changeFileMode "${permission}" "${file}"
            ;;
        "cut")
            cutTarget "${args}"
            ;;
        "copy")
            copyTarget "${args}"
            ;;
        "paste")
            pasteTarget
            ;;
        "mkdir")
            makeDirectory "${args}"
            ;;
        "exit")
            echo -e "\n💡 已退出文件管理"
            exit 0
            ;;
        *)
            echo -e "${colorRed}❌ 无效命令，请参考操作指南执行${colorReset}"
            ;;
    esac
}

main() {
    printCurrentSystemType
    checkAdbEnvironment

    currentDeviceId="$(inputSingleAdbDevice)"
    if [[ -z "${currentDeviceId}" ]]; then
        echo -e "${colorRed}❌ 未选择有效设备，程序退出${colorReset}"
        exit 1
    fi
    echo -e "${colorGreen}✅ 已选中设备：${currentDeviceId}${colorReset}"

    checkDeviceRootStatus

    echo -e "\n📂 请输入设备初始操作路径（可空，留空则默认切换到 ${SDCARD_ROOT_PATH}）："
    read -r initialPathInput
    if [[ -z "${initialPathInput}" ]]; then
        currentDevicePath="${SDCARD_ROOT_PATH}"
    else
        currentDevicePath=$(normalizeDevicePath "${initialPathInput}")
    fi

    if ! checkPathAccessibility "${currentDevicePath}"; then
        echo -e "${colorYellow}💡 初始路径不可访问，强制切换为 SD 卡根目录：${SDCARD_ROOT_PATH}${colorReset}"
        currentDevicePath="${SDCARD_ROOT_PATH}"
    fi
    listSimpleContent "${currentDevicePath}" "当前目录"

    while true; do
        drawInterface
        read -r userInput
        parseInputCommand "${userInput}"
    done
}

clear
main