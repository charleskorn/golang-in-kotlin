import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly

plugins {
    kotlin("multiplatform") version "1.6.10"
}

repositories {
    mavenCentral()
}

evaluationDependsOn(":golang-library")

kotlin {
    jvm()

    macosX64 {
        configureNativeTarget()
    }

    macosArm64 {
        configureNativeTarget()
    }

    sourceSets {
        val nativeMain by creating

        val macosMain by creating {
            dependsOn(nativeMain)
        }

        val macosX64Main by getting {
            dependsOn(macosMain)
        }

        val macosArm64Main by getting {
            dependsOn(macosMain)
        }
    }
}

fun KotlinNativeTargetWithHostTests.configureNativeTarget() {
    binaries {
        executable {
            entryPoint = "charleskorn.sample.golanginkotlin.main"
        }
    }

    compilations.named("main") {
        cinterops {
            val libraryBuildTask = project(":golang-library").tasks.named("buildArchiveLib")

            val headerDirectories = libraryBuildTask.get().outputs.files
                .filter { it.isFile && it.name.endsWith(".h") }
                .map { it.parentFile }

            val libraryDirectories = libraryBuildTask.get().outputs.files
                .filter { it.isFile && it.name.endsWith(".a") }
                .map { it.parentFile }

            val libGreeting by creating {
                defFile(project.file("src/nativeInterop/cinterop/libgreeting.def"))
                includeDirs(headerDirectories)
                extraOpts("-libraryPath", libraryDirectories.single())
            }

            tasks.named(libGreeting.interopProcessingTaskName) {
                dependsOn(libraryBuildTask)
                inputs.files(libraryBuildTask.get().outputs)
            }
        }
    }

    val targetName = this.name

    tasks.register<Exec>("run${targetName.capitalizeAsciiOnly()}") {
        group = "run"

        dependsOn("linkReleaseExecutable${targetName.capitalizeAsciiOnly()}")
        commandLine(buildDir.resolve("bin").resolve(targetName).resolve("releaseExecutable").resolve("kotlin-app.kexe"))
    }
}

tasks.register("runNativeApp") {
    group = "run"

    when (System.getProperty("os.arch")) {
        "aarch64" -> dependsOn("runMacosArm64")
        "x86_64" -> dependsOn("runMacosX64")
    }
}
