package org.example.scoundrel.utils

fun <T>List<T>.addAndGet(item: T): List<T> {
    return this.toMutableList().apply {
        add(item)
    }
}

fun <T>List<T>.addAllAndGet(items: List<T>): List<T> {
    return this.toMutableList().apply {
        addAll(items)
    }
}

fun <T>List<T>.removeAndGet(item: T): List<T> {
    return this.toMutableList().apply {
        remove(item)
    }
}