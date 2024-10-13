plugins {
    kotlin("multiplatform") version "2.0.20"
    `maven-publish`
}

group = "org.demo.crypto"
version = "1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm()

    listOf(iosSimulatorArm64(), iosX64(), iosArm64()).forEach {
        with (it) {
            val platform = when (name) {
                "iosSimulatorArm64" -> "iphonesimulator"
                "iosX64" -> "iphonesimulator"
                "iosArm64" -> "iphoneos"
                else -> error("Unsupported target $name")
            }
            compilations.getByName("main") {
                cinterops.create("SwiftChachaPoly") {
                    val interopTask = tasks[interopProcessingTaskName]
                    interopTask.dependsOn(":SwiftChachaPoly:build${platform.capitalize()}")
                    includeDirs.headerFilterOnly("$rootDir/SwiftChachaPoly/build/Release-$platform/include")
                }
            }
            binaries.all {
                linkerOpts += "-ld64"
            }
        }
    }
    sourceSets {
        getByName("commonTest").dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }

        getByName("jvmTest").dependencies {
            implementation(kotlin("test-junit"))
        }

        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlinx.cinterop.BetaInteropApi")
        }
    }
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}
