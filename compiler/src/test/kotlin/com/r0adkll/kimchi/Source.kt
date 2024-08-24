// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import com.tschuchort.compiletesting.JvmCompilationResult
import org.intellij.lang.annotations.Language

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

/**
 * Add source files here to include in every compiler test
 */
val commonTestSources = arrayOf(
  TestScope,
  TestParentScope,
)
