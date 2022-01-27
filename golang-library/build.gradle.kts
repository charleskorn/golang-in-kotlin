val golangSourceDirectory = file("src")

val buildSharedLib = tasks.register<Exec>("buildSharedLib") {
    val outputDirectory = buildDir.toPath().resolve("libs").resolve("shared")
    val outputLibrary = outputDirectory.resolve("libgreeting.dylib")
    val outputHeader = outputDirectory.resolve("libgreeting.h")

    inputs.dir(golangSourceDirectory)
    outputs.file(outputLibrary)
    outputs.file(outputHeader)

    workingDir = golangSourceDirectory
    environment("CGO_ENABLED" to "1")
    commandLine = listOf("go", "build", "-buildmode=c-shared", "-o=$outputLibrary")
}

val buildArchiveLib = tasks.register<Exec>("buildArchiveLib") {
    val outputDirectory = buildDir.toPath().resolve("libs").resolve("archive")
    val outputLibrary = outputDirectory.resolve("libgreeting.a")
    val outputHeader = outputDirectory.resolve("libgreeting.h")

    inputs.dir(golangSourceDirectory)
    outputs.file(outputLibrary)
    outputs.file(outputHeader)

    workingDir = golangSourceDirectory
    environment("CGO_ENABLED" to "1")
    commandLine = listOf("go", "build", "-buildmode=c-archive", "-o=$outputLibrary")
}

tasks.register("build") {
    dependsOn(buildSharedLib)
    dependsOn(buildArchiveLib)
}

tasks.register<Delete>("clean") {
    delete(buildSharedLib)
    delete(buildArchiveLib)
}
