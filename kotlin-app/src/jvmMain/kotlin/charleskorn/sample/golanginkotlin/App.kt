package charleskorn.sample.golanginkotlin

import jnr.ffi.LibraryLoader
import jnr.ffi.LibraryOption
import jnr.ffi.Platform
import jnr.ffi.annotations.In
import java.nio.file.Files
import java.nio.file.Path

fun main() {
    println("Hello from Kotlin/JVM!")

    val greetingLib = loadGreetingLib()

    // FIXME: this leaks the string pointer returned by Golang
    println("Golang says: " + greetingLib.GenerateGreeting("<your name here>"))
}

@Suppress("FunctionName") // The function names below must match the names exported from Golang, and Golang requires they start with a capital letter.
internal interface GreetingLib {
    fun GenerateGreeting(@In name: String): String
}

internal fun loadGreetingLib(): GreetingLib {
    val libraryDirectory = extractNativeLibrary()

    return LibraryLoader
        .create(GreetingLib::class.java)
        .option(LibraryOption.LoadNow, true)
        .option(LibraryOption.IgnoreError, true)
        .option(LibraryOption.PreferCustomPaths, true)
        .search(libraryDirectory.toString())
        .library("greeting")
        .failImmediately()
        .load()
}

private fun extractNativeLibrary(): Path {
    val classLoader = GreetingLib::class.java.classLoader
    val libraryFileName = "libgreeting.dylib"
    val stream = classLoader.getResourceAsStream(libraryFileName) ?: throw RuntimeException("Could not load '$libraryFileName' from resources.")

    stream.use {
        val outputDirectory = Files.createTempDirectory("golang-in-kotlin-libgreeting")
        outputDirectory.toFile().deleteOnExit()

        val outputFile = outputDirectory.resolve(libraryFileName)
        Files.copy(it, outputFile)
        outputFile.toFile().deleteOnExit()

        return outputDirectory
    }
}
