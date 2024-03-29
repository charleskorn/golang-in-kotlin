import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    kotlin("multiplatform") version "1.8.10"
}

repositories {
    mavenCentral()
}

evaluationDependsOn(":golang-library")

val archiveLibraryBuildTask = project(":golang-library").tasks.named("buildArchiveLib")
val sharedLibraryBuildTask = project(":golang-library").tasks.named("buildSharedLib")

kotlin {
    jvm()

    macosX64 {
        configureNativeTarget()
    }

    macosArm64 {
        configureNativeTarget()
    }

    sourceSets {
        val jvmMain by getting {
            val nativeLibraryDirectory = sharedLibraryBuildTask.get().outputs.files
                .map { it.parentFile }
                .first()

            // FIXME: this copies everything in the output directory to the jar - so we get both the .dylib and the .h file,
            // but we only need the .dylib.
            resources.srcDir(nativeLibraryDirectory)

            dependencies {
                implementation("com.github.jnr:jnr-ffi:2.2.13")
            }
        }

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

tasks.named("jvmProcessResources") {
    dependsOn(sharedLibraryBuildTask)
}

fun KotlinNativeTargetWithHostTests.configureNativeTarget() {
    binaries {
        executable {
            entryPoint = "charleskorn.sample.golanginkotlin.main"
        }
    }

    compilations.named("main") {
        cinterops {
            val nativeLibraryDirectory = archiveLibraryBuildTask.get().outputs.files
                .map { it.parentFile }
                .first()

            val libGreeting by creating {
                defFile(project.file("src/nativeInterop/cinterop/libgreeting.def"))
                includeDirs(nativeLibraryDirectory)
                extraOpts("-libraryPath", nativeLibraryDirectory)
            }

            tasks.named(libGreeting.interopProcessingTaskName) {
                dependsOn(archiveLibraryBuildTask)
                inputs.files(archiveLibraryBuildTask.get().outputs)
            }
        }
    }
}

tasks.register<JavaExec>("runJvmApp") {
    group = "run"
    mainClass.set("charleskorn.sample.golanginkotlin.AppKt")

    val compilation = kotlin.targets.named<KotlinJvmTarget>("jvm").get().compilations.named("main").get()
    classpath = compilation.compileDependencyFiles + compilation.runtimeDependencyFiles + compilation.output.allOutputs
}

tasks.register("runNativeApp") {
    group = "run"

    when (val architecture = System.getProperty("os.arch")) {
        "aarch64" -> dependsOn("runReleaseExecutableMacosArm64")
        "x86_64" -> dependsOn("runReleaseExecutableMacosX64")
        else -> throw UnsupportedOperationException("Unknown architecture $architecture")
    }
}
