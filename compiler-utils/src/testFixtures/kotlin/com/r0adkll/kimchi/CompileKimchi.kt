// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File
import java.io.OutputStream
import org.intellij.lang.annotations.Language

/**
 * A simple API to help test code generators with Kimchi in unit tests.
 */
public fun compileKimchi(
  @Language("kotlin") vararg sources: String,
  messageOutputStream: OutputStream = System.out,
  workingDir: File? = null,
  symbolProcessorProviders: List<SymbolProcessorProvider> = emptyList(),
  expectExitCode: KotlinCompilation.ExitCode? = null,
  block: JvmCompilationResult.() -> Unit = { },
): JvmCompilationResult {
  return KimchiCompilation()
    .apply {
      kotlinCompilation.apply {
        this.messageOutputStream = messageOutputStream
        if (workingDir != null) {
          this.workingDir = workingDir
        }
      }
    }
    .configureKimchi(
      extraSymbolProcessorProviders = symbolProcessorProviders,
    )
    .compile(
      *sources,
      expectExitCode = expectExitCode,
    )
    .also(block)
}
