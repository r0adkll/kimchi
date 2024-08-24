// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import java.io.File
import java.nio.file.Files
import java.util.ServiceLoader
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.config.JvmTarget
import strikt.api.expectThat

public class KimchiCompilation internal constructor(
  public val kotlinCompilation: KotlinCompilation,
) {

  private var isCompiled = false
  private var kimchiConfigured = false

  public fun configureKimchi(
    extraSymbolProcessorProviders: List<SymbolProcessorProvider> = emptyList(),
  ): KimchiCompilation = apply {
    checkNotCompiled()
    check(!kimchiConfigured) { "Kimchi should not be configured twice." }

    kimchiConfigured = true

    kotlinCompilation.apply {
      kspWithCompilation = true
      configureKsp(true) {
        symbolProcessorProviders += buildList {
          addAll(
            ServiceLoader.load(
              SymbolProcessorProvider::class.java,
              SymbolProcessorProvider::class.java.classLoader,
            ),
          )
          addAll(extraSymbolProcessorProviders)
        }
      }
    }
  }

  /** Adds the given sources to this compilation with their packages and names inferred. */
  public fun addSources(@Language("kotlin") vararg sources: String): KimchiCompilation = apply {
    checkNotCompiled()
    kotlinCompilation.sources += sources.mapIndexed { index, content ->
      val packageDir = content.lines()
        .firstOrNull { it.trim().startsWith("package ") }
        ?.substringAfter("package ")
        ?.replace('.', '/')
        ?.let { "$it/" }
        ?: ""

      val name = "${kotlinCompilation.workingDir.absolutePath}/sources/src/main/java/" +
        "$packageDir/Source$index.kt"

      Files.createDirectories(File(name).parentFile.toPath())

      SourceFile.kotlin(name, contents = content, trimIndent = true)
    }
  }

  private fun checkNotCompiled() {
    check(!isCompiled) {
      "Already compiled! Create a new compilation if you want to compile again."
    }
  }

  /**
   * Compiles the underlying [KotlinCompilation]. Note that if [configureAnvil] has not been called
   * prior to this, it will be configured with default behavior.
   */
  public fun compile(
    @Language("kotlin") vararg sources: String,
    expectExitCode: KotlinCompilation.ExitCode? = null,
    block: JvmCompilationResult.() -> Unit = {},
  ): JvmCompilationResult {
    checkNotCompiled()
    if (!kimchiConfigured) {
      // Configure with default behaviors
      configureKimchi()
    }
    addSources(*sources)
    isCompiled = true

    return kotlinCompilation.compile().apply {
      if (exitCode != expectExitCode) {
        when {
          expectExitCode == null -> {
            // No expected code, so no assertion to be made
          }
          expectExitCode == KotlinCompilation.ExitCode.OK -> {
            expectThat(exitCode) {
              assertThat("Compilation failed unexpectedly\n\n$messages") {
                it == KotlinCompilation.ExitCode.OK
              }
            }
          }
          exitCode == KotlinCompilation.ExitCode.OK -> {
            expectThat(exitCode) {
              assertThat("Compilation succeeded unexpectedly\n\n$messages") {
                it == expectExitCode
              }
            }
          }
          else -> {
            expectThat(exitCode) {
              assertThat("Error code mismatch\n\n$messages") {
                it == expectExitCode
              }
            }
          }
        }
      }
      block()
    }
  }

  public companion object {
    public operator fun invoke(): KimchiCompilation {
      return KimchiCompilation(
        KotlinCompilation().apply {
          // Sensible default behaviors
          inheritClassPath = true
          jvmTarget = JvmTarget.JVM_1_8.description
          verbose = false
        },
      )
    }
  }
}
