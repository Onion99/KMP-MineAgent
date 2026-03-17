package org.onion.agent

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val isMacOS: Boolean = System.getProperty("os.name").lowercase().contains("mac")
}

actual fun getPlatform(): Platform = JVMPlatform()