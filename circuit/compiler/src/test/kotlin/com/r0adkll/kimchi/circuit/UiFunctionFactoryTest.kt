// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.circuit

import com.r0adkll.kimchi.annotations.ContributesTo
import com.r0adkll.kimchi.hasAnnotation
import com.r0adkll.kimchi.hasReturnType
import com.r0adkll.kimchi.implements
import com.r0adkll.kimchi.isTypeOf
import com.r0adkll.kimchi.kotlinClass
import com.r0adkll.kimchi.parameter
import com.r0adkll.kimchi.primaryConstructor
import com.r0adkll.kimchi.withAnnotation
import com.r0adkll.kimchi.withFunction
import com.slack.circuit.runtime.ui.Ui
import java.io.File
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class UiFunctionFactoryTest {

  @TempDir(cleanup = CleanupMode.NEVER)
  lateinit var workingDir: File

  @Test
  fun `ui composable function generates factory and contributed component`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Modifier
        import com.r0adkll.kimchi.circuit.annotations.CircuitInject

        @CircuitInject(TestScreen::class, TestScope::class)
        @Composable
        fun TestUi(
          state: TestUiState,
          modifier: Modifier = Modifier,
        ) { }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val factory = kotlinClass("kimchi.TestUiUiFactory")
      expectThat(factory)
        .hasAnnotation(Inject::class)
        .implements(Ui.Factory::class)

      val component = kotlinClass("kimchi.TestUiUiFactoryComponent")
      expectThat(component)
        .withAnnotation<ContributesTo> {
          get { scope } isEqualTo testScope
        }
        .withFunction("bindTestUiUiFactory") {
          hasAnnotation(IntoSet::class)
          hasAnnotation(Provides::class)
          parameter(1)
            .isTypeOf(factory)
          hasReturnType(Ui.Factory::class)
        }
    }
  }

  @Test
  fun `ui composable function for static screen generates factory and contributed component`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Modifier
        import com.r0adkll.kimchi.circuit.annotations.CircuitInject

        @CircuitInject(TestScreen::class, TestScope::class)
        @Composable
        fun TestUi(
          screen: TestScreen,
          modifier: Modifier = Modifier,
        ) { }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val factory = kotlinClass("kimchi.TestUiUiFactory")
      expectThat(factory)
        .hasAnnotation(Inject::class)
        .implements(Ui.Factory::class)

      val component = kotlinClass("kimchi.TestUiUiFactoryComponent")
      expectThat(component)
        .withAnnotation<ContributesTo> {
          get { scope } isEqualTo testScope
        }
        .withFunction("bindTestUiUiFactory") {
          hasAnnotation(IntoSet::class)
          hasAnnotation(Provides::class)
          parameter(1)
            .isTypeOf(factory)
          hasReturnType(Ui.Factory::class)
        }
    }
  }

  @Test
  fun `ui composable function with injected parameters injects into factory`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Modifier
        import com.r0adkll.kimchi.circuit.annotations.CircuitInject

        class Injected

        @CircuitInject(TestScreen::class, TestScope::class)
        @Composable
        fun TestUi(
          state: TestUiState,
          injected: Injected,
          modifier: Modifier = Modifier,
        ) { }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val factory = kotlinClass("kimchi.TestUiUiFactory")
      val injectedComposable = kotlinClass("kimchi.Injected")

      expectThat(factory)
        .primaryConstructor()
        .isNotNull()
        .parameter(0)
        .isTypeOf(injectedComposable)
    }
  }

  @Test
  fun `ui composable function with injected typealias parameters injects into factory`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Modifier
        import com.r0adkll.kimchi.circuit.annotations.CircuitInject

        typealias Injected = @Composable () -> Unit

        @CircuitInject(TestScreen::class, TestScope::class)
        @Composable
        fun TestUi(
          state: TestUiState,
          injected: Injected,
          modifier: Modifier = Modifier,
        ) { }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val factory = kotlinClass("kimchi.TestUiUiFactory")
      val injectedComposable = kotlinClass("kotlin.jvm.functions.Function0")

      expectThat(factory)
        .primaryConstructor()
        .isNotNull()
        .parameter(0)
        .isTypeOf(injectedComposable)
    }
  }

  @Test
  fun `ui composable function with nested screen reference compiles`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Modifier
        import com.r0adkll.kimchi.circuit.annotations.CircuitInject
        import com.slack.circuit.runtime.screen.Screen

        data object Screens {
          data object TestScreen : Screen
        }

        @CircuitInject(Screens.TestScreen::class, TestScope::class)
        @Composable
        fun TestUi(
          state: TestUiState,
          modifier: Modifier = Modifier,
        ) { }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val factory = kotlinClass("kimchi.TestUiUiFactory")
      expectThat(factory)
        .hasAnnotation(Inject::class)
        .implements(Ui.Factory::class)

      val component = kotlinClass("kimchi.TestUiUiFactoryComponent")
      expectThat(component)
        .withAnnotation<ContributesTo> {
          get { scope } isEqualTo testScope
        }
        .withFunction("bindTestUiUiFactory") {
          hasAnnotation(IntoSet::class)
          hasAnnotation(Provides::class)
          parameter(1)
            .isTypeOf(factory)
          hasReturnType(Ui.Factory::class)
        }
    }
  }

  @Test
  fun `nested ui composable function compiles`() {
    println(workingDir.absolutePath)
    compileKimchiWithTestSources(
      """
        package kimchi

        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Modifier
        import com.r0adkll.kimchi.circuit.annotations.CircuitInject
        import com.slack.circuit.runtime.screen.Screen

        object TestUiScreen {
          @CircuitInject(TestScreen::class, TestScope::class)
          @Composable
          fun TestUi(
            state: TestUiState,
            modifier: Modifier = Modifier,
          ) { }
        }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val factory = kotlinClass("kimchi.TestUiUiFactory")
      expectThat(factory)
        .hasAnnotation(Inject::class)
        .implements(Ui.Factory::class)

      val component = kotlinClass("kimchi.TestUiUiFactoryComponent")
      expectThat(component)
        .withAnnotation<ContributesTo> {
          get { scope } isEqualTo testScope
        }
        .withFunction("bindTestUiUiFactory") {
          hasAnnotation(IntoSet::class)
          hasAnnotation(Provides::class)
          parameter(1)
            .isTypeOf(factory)
          hasReturnType(Ui.Factory::class)
        }
    }
  }
}
