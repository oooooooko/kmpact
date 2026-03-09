#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 历史作者重写脚本（filter-branch 改写身份）
# ----------------------------------------------------------------------
scriptDirPath=$(dirname "${BASH_SOURCE[0]}")
originalDirPath=$PWD
cd "${scriptDirPath}" || exit 1
source "../../../common/SystemPlatform.sh" && \
source "../../../common/EnvironmentTools.sh" && \
source "../../../common/FileTools.sh" && \
source "../../../business/GitSelector.sh" || exit 1
cd "${originalDirPath}" || exit 1
unset scriptDirPath
unset originalDirPath

backupOldTags() {
    tagNamesBefore=$(cd "${repositoryDirPath}" && git tag < /dev/null 2>/dev/null || echo "")
    tagsBackupMeta=""
    oldTagsMap=""
    if [[ -n "${tagNamesBefore}" ]]; then
        while IFS= read -r tagName; do
            if [[ -z "${tagName}" ]]; then
                continue
            fi
            tagMetaLine=$(cd "${repositoryDirPath}" && git for-each-ref "refs/tags/${tagName}" --format '%(refname:strip=2)%09%(objectname)%09%(objecttype)%09%(taggername)%09%(taggeremail)%09%(taggerdate:iso8601)%09%(contents)' < /dev/null 2>/dev/null || echo "")
            if [[ -n "${tagMetaLine}" ]]; then
                tagsBackupMeta+="${tagMetaLine}"$'\n'
            fi
            oldCommitSha=$(cd "${repositoryDirPath}" && git rev-list -n 1 "${tagName}" < /dev/null 2>/dev/null || echo "")
            if [[ -n "${oldCommitSha}" ]]; then
                oldTagsMap+="${tagName}"$'\t'"${oldCommitSha}"$'\n'
            fi
        done <<< "${tagNamesBefore}"
        tagCountBefore=$(echo "${tagNamesBefore}" | wc -l | awk '{print $1}')
        echo "✅ 已备份本地旧提交标签，共 ${tagCountBefore} 个"
    else
        echo "💡 未检测到本地标签，无需备份"
    fi
}

backupNewTags() {
    local currentTagNames
    currentTagNames=$(cd "${repositoryDirPath}" && git tag < /dev/null 2>/dev/null || echo "")
    newTagsMap=""
    if [[ -n "${currentTagNames}" ]]; then
        while IFS= read -r tagName; do
            if [[ -z "${tagName}" ]]; then
                continue
            fi
            newCommitSha=$(cd "${repositoryDirPath}" && git rev-list -n 1 "${tagName}" < /dev/null 2>/dev/null || echo "")
            if [[ -n "${newCommitSha}" ]]; then
                newTagsMap+="${tagName}"$'\t'"${newCommitSha}"$'\n'
            fi
        done <<< "${currentTagNames}"
        newTagCount=$(echo "${newTagsMap}" | awk 'NF>0' | wc -l | awk '{print $1}')
        echo "✅ 已记录改写后的新提交标签映射，共 ${newTagCount} 条"
    else
        echo "💡 未检测到本地标签"
    fi
}

swapAllTags() {
    local fromTags="$1"
    local toTags="$2"
    if [[ -z "${fromTags}" ]]; then
        return 0
    fi
    while IFS=$'\t' read -r tagName fromCommitSha; do
        if [[ -z "${tagName}" || -z "${fromCommitSha}" ]]; then
            continue
        fi
        targetCommitSha=$(echo "${toTags}" | awk -F $'\t' -v n="${tagName}" '$1==n {print $2; exit}')
        if [[ -z "${targetCommitSha}" && -n "${rewriteCommitMap}" ]]; then
            targetCommitSha=$(echo "${rewriteCommitMap}" | awk -F $'\t' -v o="${fromCommitSha}" '$1==o {print $2; exit}')
        fi
        if [[ -z "${targetCommitSha}" ]]; then
            targetCommitSha="${fromCommitSha}"
        fi
        tagMetaLine=$(echo "${tagsBackupMeta}" | awk -F $'\t' -v n="${tagName}" '$1==n {print $0; exit}')
        tagObjectType=$(echo "${tagMetaLine}" | awk -F $'\t' '{print $3}')
        tagMessage=$(echo "${tagMetaLine}" | cut -f7-)
        if [[ "${tagObjectType}" == "tag" ]]; then
            (cd "${repositoryDirPath}" && git tag -f -a "${tagName}" -m "${tagMessage}" "${targetCommitSha}") < /dev/null > /dev/null 2>&1
        else
            (cd "${repositoryDirPath}" && git tag -f "${tagName}" "${targetCommitSha}") < /dev/null > /dev/null 2>&1
        fi
    done <<< "${fromTags}"
}

main() {
    printCurrentSystemType
    checkGitEnvironment

    repositoryDirPath=$(selectLocalRepositoryPath)

    echo "请输入需要匹配的旧用户名（留空表示不按用户名匹配）"
    read -r oldName
    echo "请输入需要匹配的旧邮箱（留空表示不按邮箱匹配）"
    read -r oldEmail
    if [[ -z "${oldName}" && -z "${oldEmail}" ]]; then
        echo "❌ 必须至少提供旧用户名或旧邮箱中的一个"
        exit 1
    fi

    currentBranch=$(cd "${repositoryDirPath}" && git rev-parse --abbrev-ref HEAD < /dev/null 2>/dev/null || echo "")
    targetRef="${currentBranch}"
    if [[ -z "${targetRef}" || "${targetRef}" == "HEAD" ]]; then
        targetRef="HEAD"
    fi
    matchCount=$(cd "${repositoryDirPath}" && git log "${targetRef}" --pretty=format:'%H%x09%an%x09%ae%x09%cn%x09%ce' | awk -F $'\t' -v n="${oldName}" -v e="${oldEmail}" 'BEGIN{c=0} {an=$2; ae=$3; cn=$4; ce=$5; ok=0; if (length(n)>0 && (an==n || cn==n)) ok=1; if (length(e)>0 && (ae==e || ce==e)) ok=1; if (ok) c++} END{print c}')
    if (( matchCount == 0 )); then
        echo "❌ 没有匹配到旧用户名或旧邮箱的提交"
        exit 1
    fi

    echo "请输入新的用户名"
    read -r newName
    echo "请输入新的邮箱"
    read -r newEmail
    if [[ -z "${newName}" || -z "${newEmail}" ]]; then
        echo "❌ 新用户名和新邮箱都不能为空"
        exit 1
    fi

    echo "准备改写历史，仓库：${repositoryDirPath}"
    echo "旧用户名：${oldName:-<未设>} 旧邮箱：${oldEmail:-<未设>}"
    echo "新用户名：${newName} 新邮箱：${newEmail}"
    echo "🤔 共有 ${matchCount} 个提交的作者或提交者信息将被改写为：${newName} <${newEmail}>，请问是否继续？（y/n）"
    read -r rewriteConfirm
    if [[ "${rewriteConfirm}" =~ ^[nN]$ ]]; then
        echo "✅ 用户手动取消操作"
        exit 0
    elif [[ ! "${rewriteConfirm}" =~ ^[yY]$ ]]; then
        echo "❌ 无效选择，已取消操作"
        exit 1
    fi

    timestamp=$(date "+%Y%m%d%H%M%S")
    tempBranch="${currentBranch}_temp_${timestamp}"
    backupBranch="${currentBranch}_backup_${timestamp}"
    if [[ -z "${currentBranch}" || "${currentBranch}" == "HEAD" ]]; then
        echo "❌ 当前处于游离 HEAD，无法创建临时分支，请切换到一个分支后重试"
        exit 1
    fi

    echo "⏳ 正在创建临时分支 ${tempBranch} 并改写历史，请稍候..."
    if ! (cd "${repositoryDirPath}" && git checkout -B "${tempBranch}" "${currentBranch}" < /dev/null > /dev/null 2>&1); then
        echo "❌ 创建临时分支失败：${tempBranch}"
        exit 1
    fi
    backupOldTags

    existingOriginalRefs=$(cd "${repositoryDirPath}" && git for-each-ref refs/original --format '%(refname)' < /dev/null 2>/dev/null || echo "")
    if [[ -n "${existingOriginalRefs}" ]]; then
        while IFS= read -r refname; do
            (cd "${repositoryDirPath}" && git update-ref -d "${refname}") < /dev/null > /dev/null 2>&1 || true
        done <<< "${existingOriginalRefs}"
        (cd "${repositoryDirPath}" && rm -rf .git/logs/refs/original) < /dev/null > /dev/null 2>&1 || true
    fi

    filterBranchOutputPrint=$(cd "${repositoryDirPath}" && OLD_NAME="${oldName}" OLD_EMAIL="${oldEmail}" NEW_NAME="${newName}" NEW_EMAIL="${newEmail}" FILTER_BRANCH_SQUELCH_WARNING=1 git filter-branch -f --env-filter '
        if [[ -n "$OLD_NAME" && "$GIT_AUTHOR_NAME" == "$OLD_NAME" ]]; then
            export GIT_AUTHOR_NAME="$NEW_NAME"
            export GIT_AUTHOR_EMAIL="$NEW_EMAIL"
        fi
        if [[ -n "$OLD_EMAIL" && "$GIT_AUTHOR_EMAIL" == "$OLD_EMAIL" ]]; then
            export GIT_AUTHOR_NAME="$NEW_NAME"
            export GIT_AUTHOR_EMAIL="$NEW_EMAIL"
        fi
        if [[ -n "$OLD_NAME" && "$GIT_COMMITTER_NAME" == "$OLD_NAME" ]]; then
            export GIT_COMMITTER_NAME="$NEW_NAME"
            export GIT_COMMITTER_EMAIL="$NEW_EMAIL"
        fi
        if [[ -n "$OLD_EMAIL" && "$GIT_COMMITTER_EMAIL" == "$OLD_EMAIL" ]]; then
            export GIT_COMMITTER_NAME="$NEW_NAME"
            export GIT_COMMITTER_EMAIL="$NEW_EMAIL"
        fi
    ' --tag-name-filter cat -- --tags "${tempBranch}" 2>&1)

    exitCode=$?
    if (( exitCode != 0 )); then
        echo "${filterBranchOutputPrint}"
        echo "❌ 改写历史失败，请参考以上日志输出获取具体失败原因"
        (cd "${repositoryDirPath}" && git checkout -q "${currentBranch}") 2>&1 || true
        (cd "${repositoryDirPath}" && git branch -D "${tempBranch}") 2>&1 || true
        exit 1
    fi

    rewriteCommitMap=$(echo "${filterBranchOutputPrint}" | sed -nE 's/^Rewrite ([0-9a-f]{40}).* -> ([0-9a-f]{40}).*/\1\t\2/p')
    if [[ -z "${rewriteCommitMap}" ]]; then
        rewriteCommitMap=$(echo "${filterBranchOutputPrint}" | awk '{
            old=""; new="";
            for (i=1; i<=NF; i++) {
                if (length($i)==40 && $i ~ /^[0-9a-f]+$/) {
                    if (old=="") old=$i; else new=$i;
                }
            }
            if (old!="" && new!="") print old "\t" new
        }')
    fi
    remainingCount=$(cd "${repositoryDirPath}" && git log "${tempBranch}" --pretty=format:'%H%x09%an%x09%ae%x09%cn%x09%ce' | awk -F $'\t' -v n="${oldName}" -v e="${oldEmail}" 'BEGIN{c=0} {an=$2; ae=$3; cn=$4; ce=$5; ok=0; if (length(n)>0 && (an==n || cn==n)) ok=1; if (length(e)>0 && (ae==e || ce==e)) ok=1; if (ok) c++} END{print c}')
    newCount=$(cd "${repositoryDirPath}" && git log "${tempBranch}" --pretty=format:'%H%x09%an%x09%ae%x09%cn%x09%ce' | awk -F $'\t' -v n="${newName}" -v e="${newEmail}" 'BEGIN{c=0} {an=$2; ae=$3; cn=$4; ce=$5; if ((an==n && ae==e) || (cn==n && ce==e)) c++} END{print c}')

    if (( remainingCount != 0 )); then
        echo "❌ 改写未完全成功，仍检测到 ${remainingCount} 个提交包含旧用户名或旧邮箱"
        exit 1
    fi

    backupNewTags
    swapAllTags "${oldTagsMap}" "${newTagsMap}"

    localTagsAfter=$(cd "${repositoryDirPath}" && git tag < /dev/null 2>/dev/null || echo "")
    beforeTagCount=$(echo "${tagNamesBefore}" | wc -l | awk '{print $1}')
    afterTagCount=$(echo "${localTagsAfter}" | wc -l | awk '{print $1}')
    if (( beforeTagCount == afterTagCount )); then
        echo "✅ 已将标签重应用到新的提交，共 ${afterTagCount} 个"
    else
        echo "👻 标签数量变动：改写前 ${beforeTagCount} 个，改写后 ${afterTagCount} 个"
    fi
    echo "📝 共 ${newCount} 个提交已更新为 ${newName} <${newEmail}>，请到 ${tempBranch} 分支查看效果"
    echo "🤔 改写完成，请确认是否符合预期？"
    echo "1. 是的，符合预期（备份原分支并应用改写）"
    echo "2. 不是，放弃改写（删除临时分支，保持原分支不变）"
    while true; do
        read -r resultChoice
        if [[ "${resultChoice}" == "1" ]]; then
            echo "⏳ 正在备份原分支并应用改写..."
            (cd "${repositoryDirPath}" && git branch -f "${backupBranch}" "${currentBranch}") 2>&1
            (cd "${repositoryDirPath}" && git branch -f "${currentBranch}" "${tempBranch}") 2>&1
            (cd "${repositoryDirPath}" && git checkout -q "${currentBranch}") 2>&1
            (cd "${repositoryDirPath}" && git branch -D "${tempBranch}") 2>&1
            echo "✅ 本次改动已在 ${currentBranch} 分支上生效，同时原分支已经备份在 ${backupBranch} 分支上"
            upstreamRef=$(cd "${repositoryDirPath}" && git rev-parse --abbrev-ref --symbolic-full-name @{u} < /dev/null 2>/dev/null || echo "")
            currentBranch=$(cd "${repositoryDirPath}" && git rev-parse --abbrev-ref HEAD < /dev/null 2>/dev/null || echo "")
            if [[ -n "${upstreamRef}" && -n "${currentBranch}" ]]; then
                aheadBehind=$(cd "${repositoryDirPath}" && git rev-list --left-right --count HEAD..."${upstreamRef}" < /dev/null 2>/dev/null || echo "")
                if [[ -n "${aheadBehind}" ]]; then
                    upstreamOnly=$(echo "${aheadBehind}" | awk '{print $2}')
                    if (( upstreamOnly > 0 )); then
                        echo "💡 温馨提示：本次修改涉及重写提交历史，普通推送分支将无法成功，请使用强制推送分支"
                    fi
                fi
            fi
            if [[ -n "${oldTagsMap}" ]]; then
                while IFS=$'\t' read -r tagName oldCommitSha; do
                    if [[ -n "${tagName}" && -n "${oldCommitSha}" ]]; then
                        local currentCommitSha
                        currentCommitSha=$(cd "${repositoryDirPath}" && git rev-list -n 1 "${tagName}" < /dev/null 2>/dev/null || echo "")
                        if [[ "${currentCommitSha}" == "${oldCommitSha}" ]]; then
                            continue
                        fi
                        echo "💡 温馨提示：本次修改涉及重写提交历史，需要用本地标签覆盖远端标签，请使用强制推送标签"
                        break
                    fi
                done <<< "${oldTagsMap}"
            fi
            break
        elif [[ "${resultChoice}" == "2" ]]; then
            (cd "${repositoryDirPath}" && git checkout -q "${currentBranch}") 2>&1 || true
            (cd "${repositoryDirPath}" && git branch -D "${tempBranch}") 2>&1 || true
            echo "✅ 已放弃改写，原分支保持不变"
            swapAllTags "${oldTagsMap}" "${oldTagsMap}"
            localTagsAfter=$(cd "${repositoryDirPath}" && git tag < /dev/null 2>/dev/null || echo "")
            beforeTagCount=$(echo "${tagNamesBefore}" | wc -l | awk '{print $1}')
            afterTagCount=$(echo "${localTagsAfter}" | wc -l | awk '{print $1}')
            if (( beforeTagCount == afterTagCount )); then
                echo "✅ 已将标签还原到改写前的状态，共 ${afterTagCount} 个"
            else
                echo "👻 标签数量变动：改写前 ${beforeTagCount} 个，当前 ${afterTagCount} 个"
            fi
            break
        else
            echo "👻 请选择正确的选项编号"
            continue
        fi
    done

    if [[ "${resultChoice}" == "1" || "${resultChoice}" == "2" ]]; then
        echo "⏳ 正在清理不可达对象..."
        (cd "${repositoryDirPath}" && chflags -R nouchg .git/objects) < /dev/null > /dev/null 2>&1 || true
        (cd "${repositoryDirPath}" && chmod -R u+w .git/objects) < /dev/null > /dev/null 2>&1 || true
        (cd "${repositoryDirPath}" && git reflog expire --expire-unreachable=now --all) 2>&1
        (cd "${repositoryDirPath}" && git gc --prune=now --aggressive) 2>&1
        fsckOutputPrint=$(cd "${repositoryDirPath}" && git fsck --unreachable --no-reflogs --no-progress 2>&1 || true)
        unreachableCount=$(echo "${fsckOutputPrint}" | grep -c -E 'unreachable (blob|tree|commit|tag)' | awk '{print $1}')
        if (( unreachableCount > 0 )); then
            echo "👻 清理完成，但仍检测到 ${unreachableCount} 个不可达对象，建议再次执行或手动检查。"
        else
            echo "✅ 清理完成，未检测到不可达对象"
        fi
    fi
    exit 0
}

clear
main