/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build

import org.jetbrains.kotlin.jps.build.dependeciestxt.DependenciesTxt

abstract class AbstractMultiplatformJpsTest : AbstractIncrementalJpsTest() {
    override fun prepareModuleSources(module: DependenciesTxt.Module?) {
        prepareIndexedModuleSources(module!!)
    }
}