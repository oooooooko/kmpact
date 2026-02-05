#!/bin/bash
# ----------------------------------------------------------------------
#     author   : Android 轮子哥
#     github   : https://github.com/getActivity/AndroidCmdTools
#      time    : 2026/01/25
#      desc    : Git 属性 key value 脚本
# ----------------------------------------------------------------------
getUserNameKey() {
    echo "user.name"
}

# ----------------------------------------------------------------------

getUserEmailKey() {
    echo "user.email"
}

# ----------------------------------------------------------------------

getQuotePathKey() {
    echo "core.quotepath"
}

getQuotePathEnabledValue() {
    echo "true"
}

getQuotePathDisabledValue() {
    echo "false"
}

# ----------------------------------------------------------------------

getCommitEncodingKey() {
    echo "i18n.commitencoding"
}

getLogOutputEncodingKey() {
    echo "i18n.logoutputencoding"
}

getGuiEncodingKey() {
    echo "gui.encoding"
}

getUtf8EncodingValue() {
    echo "utf-8"
}

# ----------------------------------------------------------------------

getAutoCrlfKey() {
    echo "core.autocrlf"
}

getAutoCrlfInputValue() {
    echo "input"
}

getAutoCrlfEnabledValue() {
    echo "true"
}

getAutoCrlfDisabledValue() {
    echo "false"
}

# ----------------------------------------------------------------------

getSafeCrlfKey() {
    echo "core.safecrlf"
}

getSafeCrlfWarnValue() {
    echo "warn"
}

getSafeCrlfEnabledValue() {
    echo "true"
}

getSafeCrlfDisabledValue() {
    echo "false"
}

# ----------------------------------------------------------------------

getFileModeKey() {
    echo "core.filemode"
}

getFileModeEnabledValue() {
    echo "true"
}

getFileModeDisabledValue() {
    echo "false"
}
