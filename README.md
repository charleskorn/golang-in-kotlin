# golang-in-kotlin

A sample app showing how to embed a Golang library in a Kotlin app.

:warning: This exists because it's interesting, not because it's a good idea. Please don't do this in your own applications.

## Running

Run the Kotlin/Native app with `./gradlew runNativeApp`.

Run the Kotlin/JVM app with `./gradlew runJvmApp`.

## Caveats

* While this is a Kotlin Multiplatform project, I've deliberately avoided the use of `expect` / `actual` in the interests of keeping the point
  of this sample app clear.
* The Gradle configuration for this project could be improved. Again, this isn't the focus of this sample.
* This project only supports macOS.
* In its current configuration, this project doesn't support cross-compilation - you can only use the produced binaries on the same architecture
  as the machine used to build the project. For example, if you have an Apple Silicon Mac, the binaries you build will not work on an Intel Mac.
