package com.r0adkll.kimchi.circuit

import java.io.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.elementAt
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.withElementAt

class UiFunctionFactoryTest {

  @TempDir
  lateinit var workingDir: File

  @Test
  fun `ui composable function with injected parameters injects into factory`() {
    println(workingDir.absolutePath)
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
      val factory = classLoader.loadClass("kimchi.TestUiUiFactory")
      val injectedComposable = classLoader.loadClass("kimchi.Injected")
      val primaryConstructor = factory.constructors.first()

      expectThat(primaryConstructor.parameters.toList())
        .hasSize(1)
        .withElementAt(0) {
          get { type } isEqualTo injectedComposable
        }
    }
  }
}
