// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.r0adkll.kimchi.annotations.MergeComponent
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File
import java.io.OutputStream
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@Language("kotlin")
val TestScope = """
  package kimchi
  object TestScope
""".trimIndent()

val JvmCompilationResult.testScope: Class<*>
  get() = classLoader.loadClass("kimchi.TestScope")

@Language("kotlin")
val TestParentScope = """
  package kimchi
  object TestParentScope
""".trimIndent()

val JvmCompilationResult.testParentScope: Class<*>
  get() = classLoader.loadClass("kimchi.TestParentScope")

@Language("kotlin")
val TestComponent = """
  package kimchi
  import com.r0adkll.kimchi.annotations.MergeComponent
  @MergeComponent(TestScope::class)
  interface TestComponent
""".trimIndent()

val JvmCompilationResult.testComponent: Class<*>
  get() = classLoader.loadClass("kimchi.TestComponent")

/**
 * Add source files here to include in every compiler test
 */
val commonTestSources = arrayOf(
  TestScope,
  TestParentScope,
  TestComponent,
)

/**
 * Test helper to compile kimchi compilers with common test source files
 */
@OptIn(ExperimentalCompilerApi::class)
fun compileKimchiWithTestSources(
  @Language("kotlin") vararg extraSource: String,
  messageOutputStream: OutputStream = System.out,
  workingDir: File? = null,
  symbolProcessorProviders: List<SymbolProcessorProvider> = emptyList(),
  expectExitCode: KotlinCompilation.ExitCode? = null,
  block: JvmCompilationResult.() -> Unit = { },
) {
  compileKimchi(
    *extraSource,
    *commonTestSources,
    messageOutputStream = messageOutputStream,
    workingDir = workingDir,
    symbolProcessorProviders = symbolProcessorProviders,
    expectExitCode = expectExitCode,
    block = block,
  )
}

