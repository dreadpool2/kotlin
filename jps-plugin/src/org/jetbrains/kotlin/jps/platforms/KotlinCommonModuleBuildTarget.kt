/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.platforms

import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.builders.storage.BuildDataPaths
import org.jetbrains.jps.incremental.CompileContext
import org.jetbrains.jps.incremental.ModuleBuildTarget
import org.jetbrains.jps.model.library.JpsOrderRootType
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.jps.util.JpsPathUtil
import org.jetbrains.kotlin.build.GeneratedFile
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.compilerRunner.JpsCompilerEnvironment
import org.jetbrains.kotlin.jps.build.FSOperationsHelper
import org.jetbrains.kotlin.jps.build.KotlinChunkDirtySourceFilesHolder
import org.jetbrains.kotlin.jps.incremental.JpsIncrementalCache
import org.jetbrains.kotlin.jps.incremental.JpsIncrementalJvmCache
import org.jetbrains.kotlin.jps.model.k2MetadataCompilerArguments

class KotlinCommonModuleBuildTarget(context: CompileContext, jpsModuleBuildTarget: ModuleBuildTarget) :
    KotlinModuleBuildTarget(context, jpsModuleBuildTarget) {

    override fun compileModuleChunk(
        chunk: ModuleChunk,
        commonArguments: CommonCompilerArguments,
        dirtyFilesHolder: KotlinChunkDirtySourceFilesHolder,
        environment: JpsCompilerEnvironment,
        fsOperations: FSOperationsHelper
    ): Boolean {
//        require(chunk.representativeTarget() == jpsModuleBuildTarget)
//        if (reportAndSkipCircular(chunk, environment)) return false
//
//        if (!IncrementalCompilation.isEnabled()) {
//            // Mark all dependents as dirty if incremental compilation is not enabled
//            FSOperations.markDirtyRecursively(context, CompilationRound.CURRENT, chunk)
//        }

//        JpsKotlinCompilerRunner().runK2MetadataCompiler(
//            commonArguments,
//            module.k2MetadataCompilerArguments,
//            module.kotlinCompilerSettings,
//            environment,
//            dependenciesOutputDirs + libraryFiles,
//            filesToCompile.get(jpsModuleBuildTarget)
//        )

        return true
    }

    private val libraryFiles: List<String>
        get() = mutableListOf<String>().also { result ->
            for (library in allDependencies.libraries) {
                for (root in library.getRoots(JpsOrderRootType.COMPILED)) {
                    result.add(JpsPathUtil.urlToPath(root.url))
                }
            }
        }

    private val dependenciesOutputDirs: List<String>
        get() = mutableListOf<String>().also { result ->
            allDependencies.processModules { module ->
                if (isTests) addDependencyMetaFile(module, result, isTests = true)

                // note: production targets should be also added as dependency to test targets
                addDependencyMetaFile(module, result, isTests = false)
            }
        }

    val destination: String
        get() = module.k2MetadataCompilerArguments.destination ?: outputDir.absolutePath

    private fun addDependencyMetaFile(
        module: JpsModule,
        result: MutableList<String>,
        isTests: Boolean
    ) {
        val dependencyBuildTarget = context.kotlinBuildTargets[ModuleBuildTarget(module, isTests)]

        if (dependencyBuildTarget != this@KotlinCommonModuleBuildTarget &&
            dependencyBuildTarget is KotlinCommonModuleBuildTarget &&
            dependencyBuildTarget.sourceFiles.isNotEmpty()
        ) {
            result.add(dependencyBuildTarget.destination)
        }
    }

    override fun createCacheStorage(paths: BuildDataPaths) = JpsIncrementalJvmCache(jpsModuleBuildTarget, paths) // todo: delete

    override fun updateChunkCaches(
        chunk: ModuleChunk,
        dirtyFilesHolder: KotlinChunkDirtySourceFilesHolder,
        outputItems: Map<ModuleBuildTarget, Iterable<GeneratedFile>>,
        incrementalCaches: Map<ModuleBuildTarget, JpsIncrementalCache>
    ) {

    }
}