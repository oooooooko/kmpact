package com.okko.kmpact

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform