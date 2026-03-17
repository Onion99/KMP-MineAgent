package org.onion.agent

interface Platform {
    val name: String
    val isMacOS: Boolean get() = false
    val isIOS: Boolean get() = false
}

expect fun getPlatform(): Platform