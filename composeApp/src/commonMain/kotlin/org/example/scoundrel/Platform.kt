package org.example.scoundrel

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform