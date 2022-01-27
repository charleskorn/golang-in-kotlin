package charleskorn.sample.golanginkotlin

fun main() {
    println("Hello from Kotlin/JVM!")

    val greetingLib = loadGreetingLib()

    // FIXME: this leaks the string pointer returned by Golang
    println("Golang says: " + greetingLib.GenerateGreeting("<your name here>"))
}
