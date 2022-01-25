package charleskorn.sample.golanginkotlin

import charleskorn.sample.golanginkotlin.native.GenerateGreeting
import kotlinx.cinterop.cstr
import kotlinx.cinterop.toKString

fun main() {
    println("Hello from Kotlin!")
    println("Golang says: " + GenerateGreeting("<your name here>".cstr)!!.toKString())

    // FIXME: the above leaks the string pointer returned by Golang
}
