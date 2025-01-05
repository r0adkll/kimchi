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
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
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
import strikt.assertions.withElementAt

class PresenterClassFactoryTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  lateinit var workingDir: File

  @Test
  fun `Presenter class generates factory and contributed component`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import androidx.compose.runtime.Composable
        import me.tatarka.inject.annotations.Assisted
        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.circuit.annotations.CircuitInject
        import com.slack.circuit.runtime.Navigator
        import com.slack.circuit.runtime.presenter.Presenter
        import com.slack.circuit.runtime.CircuitContext

        @CircuitInject(TestScreen::class, TestScope::class)
        @Inject
        class TestPresenter(
          @Assisted private val screen: TestScreen,
          @Assisted private val navigator: Navigator,
          @Assisted private val context: CircuitContext,
        ) : Presenter<TestUiState> {

            @Composable
            override fun present(): TestUiState {
              return TestUiState()
            }
        }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val factory = kotlinClass("kimchi.TestPresenterFactory")
      expectThat(factory)
        .hasAnnotation(Inject::class)
        .implements(Presenter.Factory::class)
        .primaryConstructor()
        .isNotNull()
        .parameter(0)
        .isTypeOf(Function3::class)
        .with({type.arguments}) {
          withElementAt(0) {
            get { type!!.classifier } isEqualTo testScreen
          }
          withElementAt(1) {
            get { type!!.classifier } isEqualTo Navigator::class
          }
          withElementAt(2) {
            get { type!!.classifier } isEqualTo CircuitContext::class
          }
          withElementAt(3) {
            get { type!!.classifier } isEqualTo kotlinClass("kimchi.TestPresenter")
          }
        }

      val component = kotlinClass("kimchi.TestPresenterFactoryComponent")
      expectThat(component)
        .withAnnotation<ContributesTo> {
          get { scope } isEqualTo testScope
        }
        .withFunction("bindTestPresenterFactory") {
          hasAnnotation(IntoSet::class)
          hasAnnotation(Provides::class)
          parameter(1)
            .isTypeOf(factory)
          hasReturnType(Presenter.Factory::class)
        }
    }
  }
}
