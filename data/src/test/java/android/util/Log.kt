@file:JvmName("Log")

package android.util

@Suppress("unused")
fun i(tag: String, msg: String): Int {
    println("INFO: $tag: $msg")
    return 0
}

@Suppress("unused")
fun d(tag: String, msg: String): Int {
    println("DEBUG: $tag: $msg")
    return 0
}

@Suppress("unused")
fun e(tag: String, msg: String, t: Throwable): Int {
    println("ERROR: $tag: $msg")
    return 0
}

@Suppress("unused")
fun e(tag: String, msg: String): Int {
    println("ERROR: $tag: $msg")
    return 0
}

@Suppress("unused")
fun w(tag: String, msg: String): Int {
    println("WARN: $tag: $msg")
    return 0
}