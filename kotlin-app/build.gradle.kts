import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests

plugins {
    kotlin("multiplatform") version "1.7.10"
}

repositories {
    mavenCentral()
}

evaluationDependsOn(":golang-library")

val archiveLibraryBuildTask = project(":golang-library").tasks.named("buildArchiveLib")
val sharedLibraryBuildTask = project(":golang-library").tasks.named("buildSharedLib")

kotlin {
    jvm {
        compilations {
            val main by getting {
                tasks.register<JavaExec>("runJvmApp") {
                    group = "run"

                    mainClass.set("charleskorn.sample.golanginkotlin.AppKt")
                    classpath = compileDependencyFiles + runtimeDependencyFiles + output.allOutputs
                }
            }
        }
    }

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
                implementation("com.github.jnr:jnr-ffi:2.2.11")
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

tasks.register("runNativeApp") {
    group = "run"

    when (val architecture = System.getProperty("os.arch")) {
        "aarch64" -> dependsOn("runReleaseExecutableMacosArm64")
        "x86_64" -> dependsOn("runReleaseExecutableMacosX64")
        else -> throw UnsupportedOperationException("Unknown architecture $architecture")
    }
}
