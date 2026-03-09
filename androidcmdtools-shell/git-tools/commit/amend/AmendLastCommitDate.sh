#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/31
#      desc    : Git 修改最后一次提交时间脚本（amend author/committer date）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../../../common/SystemPlatform.sh" && \
source "../../../common/EnvironmentTools.sh" && \
source "../../../common/FileTools.sh" && \
source "../../../business/GitTools.sh" && \
source "../../../business/GitSelector.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

main() {
    printCurrentSystemType
    checkGitEnvironment

    repositoryDirPath=$(selectLocalRepositoryPath)

    echo "💡 当前脚本仅支持修改最近一次提交的作者时间与提交者时间"

    if ! (cd "${repositoryDirPath}" && git rev-parse HEAD < /dev/null > /dev/null 2>&1); then
        echo "❌ 当前仓库没有任何提交，无法修改"
        exit 1
    fi

    echo "当前最近一次提交的时间如下："
    currentAuthorIso=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ai)
    currentCommitterIso=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ci)
    currentAuthorDate=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ad)
    currentCommitterDate=$(cd "${repositoryDirPath}" && git log -1 --pretty=%cd)
    if [[ "${currentAuthorIso}" == "${currentCommitterIso}" ]]; then
        echo "${currentAuthorIso}"
    else
        echo "作者时间：${currentAuthorIso}"
        echo "提交者时间：${currentCommitterIso}"
    fi

    echo "请输入新的年份"
    while true; do
        read -r inputYear
        if [[ -z "${inputYear}" ]]; then
            echo "👻 年份不能为空，请重新输入"
            continue
        elif [[ ! "${inputYear}" =~ ^[0-9]{4}$ ]]; then
            echo "👻 年份必须是 4 位数字，请重新输入"
            continue
        else
            break
        fi
    done

    echo "请输入新的月份"
    while true; do
        read -r inputMonth
        if [[ -z "${inputMonth}" ]]; then
            echo "👻 月份不能为空，请重新输入"
            continue
        elif [[ ! "${inputMonth}" =~ ^([1-9]|1[0-2])$ ]]; then
            echo "👻 月份必须是 1 ~ 12 的数字，请重新输入"
            continue
        else
            break
        fi
    done

    echo "请输入新的日期"
    while true; do
        read -r inputDay
        if [[ -z "${inputDay}" ]]; then
            echo "👻 日期不能为空，请重新输入"
            continue
        elif [[ ! "${inputDay}" =~ ^([1-9]|[12][0-9]|3[01])$ ]]; then
            echo "👻 月份必须是 1 ~ 12 的数字，请重新输入"
            continue
        else
            yearNum=$((10#${inputYear}))
            monthNum=$((10#${inputMonth}))
            dayNum=$((10#${inputDay}))
            leap=0
            if (( (yearNum % 4 == 0 && yearNum % 100 != 0) || (yearNum % 400 == 0) )); then
                leap=1
            fi
            maxDay=31
            if (( monthNum == 4 || monthNum == 6 || monthNum == 9 || monthNum == 11 )); then
                maxDay=30
            elif (( monthNum == 2 )); then
                if (( leap == 1 )); then
                    maxDay=29
                else
                    maxDay=28
                fi
            fi
            if (( dayNum > maxDay )); then
                echo "👻 当前月份最大天数为 ${maxDay} 天，请重新输入"
                continue
            fi
            break
        fi
    done

    echo "请输入新的小时"
    while true; do
        read -r inputHour
        if [[ -z "${inputHour}" ]]; then
            echo "👻 小时不能为空，请重新输入"
            continue
        elif [[ ! "${inputHour}" =~ ^([0-9]|1[0-9]|2[0-3])$ ]]; then
            echo "👻 小时必须是 0 ~ 23 的数字，请重新输入"
            continue
        else
            break
        fi
    done

    echo "请输入新的分钟"
    while true; do
        read -r inputMinute
        if [[ -z "${inputMinute}" ]]; then
            echo "👻 分钟不能为空，请重新输入"
            continue
        elif [[ ! "${inputMinute}" =~ ^([0-9]|[1-5][0-9])$ ]]; then
            echo "👻 分钟必须是 0 ~ 59 的数字，请重新输入"
            continue
        else
            break
        fi
    done

    echo "请输入新的秒数"
    while true; do
        read -r inputSecond
        if [[ -z "${inputSecond}" ]]; then
            echo "👻 秒数不能为空，请重新输入"
            continue
        elif [[ ! "${inputSecond}" =~ ^([0-9]|[1-5][0-9])$ ]]; then
            echo "👻 秒数必须是 0 ~ 59 的数字，请重新输入"
            continue
        else
            break
        fi
    done

    fmtMonth=$(printf "%02d" "${inputMonth}")
    fmtDay=$(printf "%02d" "${inputDay}")
    fmtHour=$(printf "%02d" "${inputHour}")
    fmtMinute=$(printf "%02d" "${inputMinute}")
    fmtSecond=$(printf "%02d" "${inputSecond}")
    newDate="${inputYear}-${fmtMonth}-${fmtDay} ${fmtHour}:${fmtMinute}:${fmtSecond}"

    sysYear=$(date "+%Y")
    sysMonth=$(date "+%m")
    yearDiff=$((10#${inputYear} - 10#${sysYear}))
    monthDiff=$((10#${fmtMonth} - 10#${sysMonth}))
    if [[ ${yearDiff} -ne 0 ]]; then
        echo "📝 当前年份与目标年份的差距：${yearDiff} 年"
    fi
    if [[ ${monthDiff} -ne 0 ]]; then
        echo "📝 当前月份与目标月份的差距：${monthDiff} 个月"
    fi

    if [[ "${currentAuthorIso}" == "${newDate}"* && "${currentCommitterIso}" == "${newDate}"* ]]; then
        echo "❌ 新的时间与当前一致，未执行修改"
        exit 1
    fi

    prevCommit=$(convertShortHashToLong "${repositoryDirPath}" "HEAD")
    (cd "${repositoryDirPath}" && GIT_COMMITTER_DATE="${newDate}" git commit --amend --no-edit --date="${newDate}")

    latestAuthorIso=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ai)
    latestCommitterIso=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ci)

    echo "最新提交的时间为："
    latestAd=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ad)
    latestCd=$(cd "${repositoryDirPath}" && git log -1 --pretty=%cd)
    if [[ "${latestAd}" == "${latestCd}" ]]; then
        echo "${latestAd}"
    else
        echo "作者时间：${latestAd}"
        echo "提交者时间：${latestCd}"
    fi

    if [[ "${latestAuthorIso}" != "${newDate}"* || "${latestCommitterIso}" != "${newDate}"* ]]; then
        echo "❌ 修改最后一次提交的时间失败"
        exit 1
    fi

    echo "🤔 提交的时间修改完成，请确认本次修改是否符合你的预期？"
    echo "1. 是的，符合预期"
    echo "2. 不是，给我改回去"
    while true; do
        read -r resultChoice
        if [[ "${resultChoice}" == "1" ]]; then
            echo "✅ 修改最后一次提交的时间成功，如遇到无法推送分支，则应使用强制推送分支"
            exit 0
        elif [[ "${resultChoice}" == "2" ]]; then
            (cd "${repositoryDirPath}" && git reset --hard "${prevCommit}")
            restoredAuthorDate=$(cd "${repositoryDirPath}" && git log -1 --pretty=%ad)
            restoredCommitterDate=$(cd "${repositoryDirPath}" && git log -1 --pretty=%cd)
            if [[ "${restoredAuthorDate}" == "${currentAuthorDate}" && "${restoredCommitterDate}" == "${currentCommitterDate}" ]]; then
                echo "✅ 还原成功，已回到最初的提交时间"
                exit 0
            else
                echo "❌ 还原失败，提交时间与最初不一致"
                exit 1
            fi
        else
            echo "👻 请选择正确的选项编号"
            continue
        fi
    done
}

clear
main