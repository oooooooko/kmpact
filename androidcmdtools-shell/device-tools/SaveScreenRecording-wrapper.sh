#!/bin/bash

# SaveScreenRecording.sh 的包装器
# 用于在非TTY环境下运行，将stdin重定向到/dev/tty

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 创建一个命名管道作为虚拟TTY
FIFO="/tmp/kiro_tty_$$"
mkfifo "$FIFO" 2>/dev/null || true

# 清理函数
cleanup() {
    rm -f "$FIFO"
}
trap cleanup EXIT

# 在后台将stdin重定向到命名管道
cat > "$FIFO" &
CAT_PID=$!

# 运行原始脚本，将命名管道作为/dev/tty
bash "$SCRIPT_DIR/SaveScreenRecording.sh" < "$FIFO"

# 等待cat进程结束
wait $CAT_PID 2>/dev/null || true

cleanup
