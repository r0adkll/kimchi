package com.r0adkll.kimchi.restaurant

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform