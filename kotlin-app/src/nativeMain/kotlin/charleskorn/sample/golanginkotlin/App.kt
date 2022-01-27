package charleskorn.sample.golanginkotlin

import charleskorn.sample.golanginkotlin.native.GenerateGreeting
import kotlinx.cinterop.cstr
import kotlinx.cinterop.toKString

fun main() {
    println("Hello from Kotlin/Native!")

    // FIXME: this leaks the string pointer returned by Golang
    println("Golang says: " + GenerateGreeting("<your name here>".cstr)!!.toKString())
}
