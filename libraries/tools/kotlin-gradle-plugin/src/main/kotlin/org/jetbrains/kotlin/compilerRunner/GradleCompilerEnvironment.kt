package org.jetbrains.kotlin.compilerRunner

import org.jetbrains.kotlin.annotation.AnnotationFileUpdater
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.daemon.common.MultiModuleICSettings
import org.jetbrains.kotlin.gradle.tasks.GradleMessageCollector
import org.jetbrains.kotlin.gradle.tasks.findToolsJar
import org.jetbrains.kotlin.incremental.ChangedFiles
import java.io.File
import java.net.URL

internal open class GradleCompilerEnvironment(
        val compilerClasspath: List<File>,
        messageCollector: GradleMessageCollector,
        outputItemsCollector: OutputItemsCollector,
        val compilerArgs: CommonCompilerArguments
) : CompilerEnvironment(Services.EMPTY, messageCollector, outputItemsCollector) {
    val toolsJar: File? by lazy { findToolsJar() }

    val compilerFullClasspath: List<File>
            get() = (compilerClasspath + toolsJar).filterNotNull()

    val compilerClasspathURLs: List<URL>
        get() = compilerFullClasspath.map { it.toURI().toURL() }
}

internal class GradleIncrementalCompilerEnvironment(
        compilerClasspath: List<File>,
        val changedFiles: ChangedFiles,
        val workingDir: File,
        messageCollector: GradleMessageCollector,
        outputItemsCollector: OutputItemsCollector,
        compilerArgs: CommonCompilerArguments,
        val kaptAnnotationsFileUpdater: AnnotationFileUpdater? = null,
        val usePreciseJavaTracking: Boolean = false,
        val localStateDirs: List<File> = emptyList(),
        val multiModuleICSettings: MultiModuleICSettings
) : GradleCompilerEnvironment(compilerClasspath, messageCollector, outputItemsCollector, compilerArgs)
