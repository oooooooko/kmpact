#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 回滚提交脚本（revert 指定 commit）
# ----------------------------------------------------------------------
scriptDirPath=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
[ -z "" ] || source "../../../common/SystemPlatform.sh"
source "${scriptDirPath}/../../../common/SystemPlatform.sh"
[ -z "" ] || source "../../../common/EnvironmentTools.sh"
source "${scriptDirPath}/../../../common/EnvironmentTools.sh"
[ -z "" ] || source "../../../common/FileTools.sh"
source "${scriptDirPath}/../../../common/FileTools.sh"
[ -z "" ] || source "../../../business/GitSelector.sh"
source "${scriptDirPath}/../../../business/GitSelector.sh"

resultConfirmation() {
    echo "🤔 请确认本次修改是否符合你的预期？"
    echo "1. 是的，符合预期"
    echo "2. 不是，给我改回去"
    while true; do
        read -r finalChoice
        if [[ "${finalChoice}" == "1" ]]; then
            (cd "${repositoryDirPath}" && git rev-parse HEAD)
            echo "✅ 成功撤销特定的提交"
            echo "💡 温馨提示：撤销提交本质上并不是抹除原有提交，而是创建一个全新的反向提交，从而抵消原提交的所有变更，相当于打补丁，所以无需进行强制推送"
            break
        elif [[ "${finalChoice}" == "2" ]]; then
            (cd "${repositoryDirPath}" && git reset --hard HEAD^)
            echo "✅ 已经还原到最初的状态"
            break
        else
            echo "👻 请选择正确的选项编号"
        fi
    done
}

main() {
    printCurrentSystemType
    checkGitEnvironment

    repositoryDirPath=$(selectLocalRepositoryPath)

    set -e

    hasChanges=$(cd "${repositoryDirPath}" && [[ -n "$(git status --porcelain)" ]] && echo "1" || echo "0")
    if [[ "${hasChanges}" == "1" ]]; then
        echo "❌ 检测到存在未提交的更改，操作中止"
        exit 1
    fi

    echo "请输入要撤销的提交哈希（例如：bd73f02567ecb85ea9e13206e6dcfee5b94b1f91）"
    read -r commitHash
    if [[ -z "${commitHash}" ]]; then
        echo "提交哈希不能为空"
        exit 1
    fi

    targetFullHash=$(cd "${repositoryDirPath}" && git rev-parse --verify "${commitHash}^{commit}" < /dev/null 2>/dev/null || echo "")
    if [[ -z "${targetFullHash}" ]]; then
        echo "❌ 无法解析该提交哈希，请确认输入正确"
        exit 1
    fi

    parentCount=$(cd "${repositoryDirPath}" && git show -s --format=%p "${targetFullHash}" | awk '{print NF}')
    useMainline=""
    if (( parentCount > 1 )); then
        parentCommitIds=$(cd "${repositoryDirPath}" && git show -s --pretty=%P "${targetFullHash}")
        parentCommitId1=$(echo "${parentCommitIds}" | awk '{print $1}')
        parentCommitId2=$(echo "${parentCommitIds}" | awk '{print $2}')

        mergeCommitMessage=$(cd "${repositoryDirPath}" && git show -s --pretty=%s "${targetFullHash}")
        if [[ "${mergeCommitMessage}" =~ ^Merge[[:space:]]branch[[:space:]](.+)[[:space:]]of[[:space:]](.+)$ ]]; then
            targetBranch="${BASH_REMATCH[1]//\'/}"
            sourceBranch="${BASH_REMATCH[2]//\'/}"
            parentCommitName1="[branch ${targetBranch}]"
            parentCommitName2="[branch ${sourceBranch}]"
        elif [[ "${mergeCommitMessage}" =~ ^Merge[[:space:]].*[[:space:]]*branch[[:space:]](.+)[[:space:]]into[[:space:]](.+)$ ]]; then
            targetBranch="${BASH_REMATCH[2]//\'/}"
            sourceBranch="${BASH_REMATCH[1]//\'/}"
            parentCommitName1="[branch ${targetBranch}]"
            parentCommitName2="[branch ${sourceBranch}]"
        elif [[ "${mergeCommitMessage}" =~ ^Merge[[:space:]]pull[[:space:]]request[[:space:]](.+)[[:space:]]from[[:space:]](.+)$ ]]; then
            sourceBranch="${BASH_REMATCH[1]}"
            targetBranch="${BASH_REMATCH[2]}"
            parentCommitName1="[pull request ${targetBranch}]"
            parentCommitName2="[branch ${sourceBranch}]"
        fi

        if [[ "${parentCommitName1}" == "${parentCommitName2}" || -z "${parentCommitName1}" || -z "${parentCommitName2}" ]]; then
            parentCommitName1=$(cd "${repositoryDirPath}" && git name-rev --name-only "${parentCommitId1}" < /dev/null 2>/dev/null | sed 's/~.*//')
            parentCommitName2=$(cd "${repositoryDirPath}" && git name-rev --name-only "${parentCommitId2}" < /dev/null 2>/dev/null | sed 's/~.*//')
        fi
        if [[ "${parentCommitName1}" == "${parentCommitName2}" || -z "${parentCommitName1}" || -z "${parentCommitName2}" ]]; then
            parentCommitName1=$(cd "${repositoryDirPath}" && git rev-parse --short "${parentCommitId1}");
            parentCommitName2=$(cd "${repositoryDirPath}" && git rev-parse --short "${parentCommitId2}");
        fi

        if (( ${#parentCommitName1} >= 20 || ${#parentCommitName2} >= 20 )); then
            echo "📝 A = ${parentCommitName1}"
            echo "📝 B = ${parentCommitName2}"
            echo "📝 A 的内容 = A 和 B 共同的起点 + A 独有的修改 + A 和 B 合并之后的修改"
            echo "📝 B 的内容 = A 和 B 共同的起点 + B 独有的修改 + A 和 B 合并之后的修改"
        else
            echo "📝 ${parentCommitName1} 的内容 = ${parentCommitName1} 和 ${parentCommitName2} 共同的起点 + ${parentCommitName1} 独有的修改 + ${parentCommitName1} 和 ${parentCommitName2} 合并之后的修改"
            echo "📝 ${parentCommitName2} 的内容 = ${parentCommitName1} 和 ${parentCommitName2} 共同的起点 + ${parentCommitName2} 独有的修改 + ${parentCommitName1} 和 ${parentCommitName2} 合并之后的修改"
        fi
        echo "🤔 请选择你的操作："
        echo "1. 保留 ${parentCommitName1} 独有的修改，丢弃 ${parentCommitName2} 独有的修改（推荐）"
        echo "2. 保留 ${parentCommitName2} 独有的修改，丢弃 ${parentCommitName1} 独有的修改（不推荐）"
        read -r choice
        if [[ -z "${choice}" ]]; then
            choice="1"
        fi
        if [[ "${choice}" == "1" ]]; then
            useMainline="1"
        elif [[ "${choice}" == "2" ]]; then
            useMainline="2"
        else
            echo "❌ 输入无效，操作中止"
            exit 1
        fi
    fi

    echo "👻 该操作将撤销指定提交，可能影响历史并引发冲突，是否继续？(y/n)"
    read -r proceedDanger
    if [[ "${proceedDanger}" != "y" && "${proceedDanger}" != "Y" ]]; then
        echo "已取消撤销操作"
        exit 1
    fi

    set +e
    if [[ -n "${useMainline}" ]]; then
        (cd "${repositoryDirPath}" && GIT_EDITOR=: git revert -m "${useMainline}" --no-edit "${targetFullHash}")
    else
        (cd "${repositoryDirPath}" && GIT_EDITOR=: git revert --no-edit "${targetFullHash}")
    fi
    revertStatus=$?
    set -e

    if (( revertStatus == 0 )); then
        resultConfirmation
        return
    fi

    echo "👻 撤销过程中出现冲突，请先在编辑器中解决冲突并保存文件"
    echo "1. 已解决冲突，继续进行"
    echo "2. 不想解决冲突，希望放弃本次撤销"
    echo "请输入选项编号并按下回车键："
    while true; do
        read -r conflictChoice
        if [[ "${conflictChoice}" == "1" ]]; then
            unresolved=$(cd "${repositoryDirPath}" && git diff --name-only --diff-filter=U)
            if [[ -n "${unresolved}" ]]; then
                echo "👻 仍有未解决的冲突："
                echo "${unresolved}"
                echo "👻 请继续处理后再次选择"
                continue
            fi
            set +e
            contMsg=$(cd "${repositoryDirPath}" && git add -A && GIT_EDITOR=: git revert --continue 2>&1)
            contStatus=$?
            set -e
            if (( contStatus != 0 )); then
                if echo "${contMsg}" | grep -qi "no revert in progress"; then
                    echo "💡 当前没有正在进行的撤销，可能已完成或已中止"
                elif echo "${contMsg}" | grep -qi "nothing to commit"; then
                    echo "💡 没有需要提交的更改"
                elif echo "${contMsg}" | grep -qi "hook"; then
                    echo "👻 提交钩子失败，请检查钩子输出"
                else
                    echo "👻 可能还有未解决的冲突，请继续处理后再次选择"
                fi
                continue
            fi
            echo "📝 检测到冲突已经解决"
            resultConfirmation
            break
        elif [[ "${conflictChoice}" == "2" ]]; then
            (cd "${repositoryDirPath}" && git revert --abort)
            echo "✅ 已放弃本次撤销"
            break
        else
            echo "👻 请选择正确的选项编号"
            continue
        fi
    done
}

clear
main