// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.circuit

import com.r0adkll.kimchi.compileKimchi
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File
import org.intellij.lang.annotations.Language

@Language("kotlin")
val TestScreen = """
  package kimchi
  import com.slack.circuit.runtime.screen.Screen
  data object TestScreen : Screen
""".trimIndent()

@Language("kotlin")
val TestUiState = """
  package kimchi
  import com.slack.circuit.runtime.CircuitUiState
  class TestUiState : CircuitUiState
""".trimIndent()

@Language("kotlin")
val TestScope = """
  package kimchi
  object TestScope
""".trimIndent()

@Language("kotlin")
val Composable = """
  package androidx.compose.runtime
  @Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE,
    AnnotationTarget.TYPE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER
  )
  annotation class Composable
""".trimIndent()

@Language("kotlin")
val Modifier = """
  package androidx.compose.ui
  interface Modifier {
    companion object : Modifier
  }
""".trimIndent()

@Language("kotlin")
val CircuitUiState = """
  package com.slack.circuit.runtime
  interface CircuitUiState
""".trimIndent()

@Language("kotlin")
val CircuitContext = """
  package com.slack.circuit.runtime
  class CircuitContext
""".trimIndent()

@Language("kotlin")
val CircuitScreen = """
  package com.slack.circuit.runtime.screen
  interface Screen
""".trimIndent()

@Language("kotlin")
val CircuitUi = """
  package com.slack.circuit.runtime.ui

  import androidx.compose.runtime.Composable
  import androidx.compose.runtime.Stable
  import androidx.compose.ui.Modifier
  import com.slack.circuit.runtime.CircuitContext
  import com.slack.circuit.runtime.CircuitUiState
  import com.slack.circuit.runtime.screen.Screen

  @Stable
  interface Ui<UiState : CircuitUiState> {
    @Composable fun Content(state: UiState, modifier: Modifier)

    fun interface Factory {
      fun create(screen: Screen, context: CircuitContext): Ui<*>?
    }
  }

  inline fun <UiState : CircuitUiState> ui(
    crossinline body: @Composable (state: UiState, modifier: Modifier) -> Unit
  ): Ui<UiState> {
    return object : Ui<UiState> {
      @Composable
      override fun Content(state: UiState, modifier: Modifier) {
        body(state, modifier)
      }
    }
  }
""".trimIndent()

fun compileKimchiWithTestSources(
  @Language("kotlin") vararg sources: String,
  expectExitCode: KotlinCompilation.ExitCode = KotlinCompilation.ExitCode.OK,
  workingDir: File? = null,
  block: JvmCompilationResult.() -> Unit,
) = compileKimchi(
  *sources,
  TestScreen,
  TestUiState,
  TestScope,
  CircuitUiState,
  CircuitContext,
  CircuitScreen,
  CircuitUi,
  Composable,
  Modifier,
  expectExitCode = expectExitCode,
  workingDir = workingDir,
  block = block,
)
