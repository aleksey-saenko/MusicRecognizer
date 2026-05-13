package com.mrsep.musicrecognizer

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.LocalState
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

@CacheableTask
abstract class CargoNdkBuildTask @Inject constructor(
    private val execOperations: ExecOperations,
) : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val crateDir: DirectoryProperty

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val buildProfile: Property<String>

    @get:Input
    abstract val minSdk: Property<Int>

    @get:Input
    abstract val abis: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:LocalState
    abstract val cargoTargetDir: DirectoryProperty

    @get:Input
    abstract val ndkPath: Property<String>

    @TaskAction
    fun build() {
        val ndk = File(ndkPath.get())
        check(ndk.exists()) { "Required NDK not installed: ${ndk.absolutePath}" }

        val args = mutableListOf(
            "cargo",
            "ndk",
        )

        abis.get().forEach { abi ->
            args += listOf("-t", abi)
        }

        args += listOf(
            "--platform", minSdk.get().toString(),
            "-o", outputDir.get().asFile.absolutePath,
            "build",
            "-v" // -vv for very verbose
        )

        if (buildProfile.get() == "release") {
            args += "--release"
        }

        execOperations.exec {
            workingDir = crateDir.get().asFile
            environment("ANDROID_NDK_HOME", ndk.absolutePath)
            environment("CARGO_TARGET_DIR", cargoTargetDir.get().asFile.absolutePath)
            commandLine(args)
        }
    }
}
