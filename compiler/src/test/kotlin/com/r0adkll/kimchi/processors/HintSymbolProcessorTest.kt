// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.r0adkll.kimchi.HINT_BINDING_PACKAGE
import com.r0adkll.kimchi.HINT_CONTRIBUTES_PACKAGE
import com.r0adkll.kimchi.HINT_MULTIBINDING_PACKAGE
import com.r0adkll.kimchi.HINT_SUBCOMPONENT_PACKAGE
import com.r0adkll.kimchi.compileKimchiWithTestSources
import com.r0adkll.kimchi.getHint
import com.r0adkll.kimchi.getHintScope
import com.r0adkll.kimchi.testParentScope
import com.r0adkll.kimchi.testScope
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File
import kotlin.reflect.KClass
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HintSymbolProcessorTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  lateinit var workingDir: File

  @ParameterizedTest
  @MethodSource("hintTestParameters")
  fun testHintProcessing(hintTest: HintTest) {
    compileKimchiWithTestSources(
      hintTest.source,
      workingDir = workingDir,
      expectExitCode = hintTest.expectedExitCode,
    ) {
      val expectedClass = hintTest.expectedClass(this)
      val hint = expectedClass.getHint(hintTest.hintPackage)
      val scope = expectedClass.getHintScope(hintTest.hintPackage)

      expectThat(hint).isEqualTo(hintTest.expectedHint(this))
      expectThat(scope).isEqualTo(hintTest.expectedScope(this))
    }
  }

  @Test
  fun `Duplicate class names do NOT cause a collision`() {
    println(workingDir.absolutePath)
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesTo

        interface Outer1 {
          @ContributesTo(TestScope::class)
          interface Inner
        }

        interface Outer2 {
          @ContributesTo(TestScope::class)
          interface Inner
        }
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.OK,
    )
  }

  companion object {
    @JvmStatic
    fun hintTestParameters(): List<HintTest> = listOf(
      contributesBindingHintTest,
      contributesMultiBindingHintTest,
      contributesToHintTest,
      contributesSubcomponentHintTest,
    )
  }
}

class HintTest(
  @param:Language("kotlin") val source: String,
  val expectedExitCode: KotlinCompilation.ExitCode,
  val expectedClass: JvmCompilationResult.() -> Class<*>,
  val expectedHint: JvmCompilationResult.() -> KClass<*>,
  val expectedScope: JvmCompilationResult.() -> KClass<*>,
  val hintPackage: String,
) {
  override fun toString(): String {
    return "HintTest($hintPackage)"
  }
}

val contributesBindingHintTest = HintTest(
  """
    package kimchi

    import kimchi.TestScope

    import com.r0adkll.kimchi.annotations.ContributesBinding
    import me.tatarka.inject.annotations.Inject

    interface TestBinding

    @ContributesBinding(TestScope::class)
    @Inject
    class RealTestBinding : TestBinding
  """.trimIndent(),
  KotlinCompilation.ExitCode.OK,
  { classLoader.loadClass("kimchi.RealTestBinding") },
  { classLoader.loadClass("kimchi.RealTestBinding").kotlin },
  { testScope.kotlin },
  HINT_BINDING_PACKAGE,
)

val contributesMultiBindingHintTest = HintTest(
  """
    package kimchi

    import kimchi.TestScope
    import me.tatarka.inject.annotations.Inject
    import com.r0adkll.kimchi.annotations.ContributesMultibinding

    interface TestBinding

    @ContributesMultibinding(TestScope::class)
    @Inject
    class RealTestBinding : TestBinding
  """.trimIndent(),
  KotlinCompilation.ExitCode.OK,
  { classLoader.loadClass("kimchi.RealTestBinding") },
  { classLoader.loadClass("kimchi.RealTestBinding").kotlin },
  { testScope.kotlin },
  HINT_MULTIBINDING_PACKAGE,
)

val contributesToHintTest = HintTest(
  """
    package kimchi

    import kimchi.TestScope
    import com.r0adkll.kimchi.annotations.ContributesTo

    @ContributesTo(TestScope::class)
    interface TestModule
  """.trimIndent(),
  KotlinCompilation.ExitCode.OK,
  { classLoader.loadClass("kimchi.TestModule") },
  { classLoader.loadClass("kimchi.TestModule").kotlin },
  { testScope.kotlin },
  HINT_CONTRIBUTES_PACKAGE,
)

val contributesSubcomponentHintTest = HintTest(
  """
    package kimchi

    import kimchi.*
    import com.r0adkll.kimchi.annotations.ContributesSubcomponent

    @ContributesSubcomponent(
      scope = TestScope::class,
      parentScope = TestParentScope::class,
    )
    interface TestSubcomponent {

      @ContributesSubcomponent.Factory
      interface Factory {
        fun create(): TestSubcomponent
      }
    }
  """.trimIndent(),
  KotlinCompilation.ExitCode.OK,
  { classLoader.loadClass("kimchi.TestSubcomponent") },
  { classLoader.loadClass("kimchi.TestSubcomponent").kotlin },
  { testParentScope.kotlin },
  HINT_SUBCOMPONENT_PACKAGE,
)
