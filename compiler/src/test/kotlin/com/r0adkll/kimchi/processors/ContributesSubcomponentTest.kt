// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.r0adkll.kimchi.compileKimchiWithTestSources
import com.r0adkll.kimchi.hasAnnotation
import com.r0adkll.kimchi.hasReturnType
import com.r0adkll.kimchi.implements
import com.r0adkll.kimchi.kotlinClass
import com.r0adkll.kimchi.mergedTestComponent
import com.r0adkll.kimchi.singleIn
import com.r0adkll.kimchi.withFunction
import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat

class ContributesSubcomponentTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  lateinit var workingDir: File

  @Test
  fun `contributed subcomponents on abstract class fails`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesSubcomponent

        @ContributesSubcomponent(
          scope = TestScope::class,
          parentScope = TestParentScope::class,
        )
        abstract class TestSubcomponent(val param: Any)
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.INTERNAL_ERROR,
    )
  }

  @Test
  fun `contributed subcomponents without factory fails`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesSubcomponent

        @ContributesSubcomponent(
          scope = TestScope::class,
          parentScope = TestParentScope::class,
        )
        interface TestSubcomponent
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.INTERNAL_ERROR,
    )
  }

  @Test
  fun `contributed subcomponents without factory create function fails`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesSubcomponent

        @ContributesSubcomponent(
          scope = TestScope::class,
          parentScope = TestParentScope::class,
        )
        interface TestSubcomponent {

          @ContributesSubcomponent.Factory
          interface Factory
        }
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.INTERNAL_ERROR,
    )
  }

  @Test
  fun `contributed subcomponents with explicit parent definition fails`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Component
        import com.r0adkll.kimchi.annotations.ContributesSubcomponent

        @Component
        abstract class TestParentComponent

        @ContributesSubcomponent(
          scope = TestScope::class,
          parentScope = TestParentScope::class,
        )
        interface TestSubcomponent {

          @ContributesSubcomponent.Factory
          interface Factory {
            fun create(
              @Component parent: TestParentComponent,
            ) : TestSubcomponent
          }
        }
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.INTERNAL_ERROR,
    )
  }

  @Test
  fun `contributed subcomponents are merged into parent component`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesSubcomponent

        object ChildScope

        @ContributesSubcomponent(
          scope = ChildScope::class,
          parentScope = TestScope::class,
        )
        interface TestSubcomponent {

          @ContributesSubcomponent.Factory
          interface Factory {
            fun create() : TestSubcomponent
          }
        }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val testSubcomponent = kotlinClass("kimchi.TestSubcomponent")
      val testSubcomponentFactory = kotlinClass("kimchi.TestSubcomponent\$Factory")
      val mergedTestSubcomponent = kotlinClass("kimchi.merge.kimchi.MergedTestComponent\$MergedTestSubcomponent")
      expectThat(mergedTestComponent)
        .implements(testSubcomponentFactory)

      expectThat(mergedTestSubcomponent)
        .implements(testSubcomponent)
    }
  }

  @Test
  fun `contributed subcomponents generate provide functions for its factory on its parent`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesSubcomponent

        object ChildScope

        @ContributesSubcomponent(
          scope = ChildScope::class,
          parentScope = TestScope::class,
        )
        interface TestSubcomponent {

          @ContributesSubcomponent.Factory
          interface Factory {
            fun create() : TestSubcomponent
          }
        }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val testSubcomponentFactory = kotlinClass("kimchi.TestSubcomponent\$Factory")
      expectThat(mergedTestComponent)
        .withFunction("provideTestSubcomponentFactory") {
          hasReturnType(testSubcomponentFactory)
        }
    }
  }

  @Test
  fun `contributed subcomponents keep their kotlin-inject scoping`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesSubcomponent

        object ChildScope

        @SingleIn(ChildScope::class)
        @ContributesSubcomponent(
          scope = ChildScope::class,
          parentScope = TestScope::class,
        )
        interface TestSubcomponent {

          @ContributesSubcomponent.Factory
          interface Factory {
            fun create() : TestSubcomponent
          }
        }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val mergedTestSubcomponent = kotlinClass("kimchi.merge.kimchi.MergedTestComponent\$MergedTestSubcomponent")

      // Assert that any scope annotation applied to our components isn't also duplicated on the generated merged
      // implementation. Due to kotlin-inject's handling of inheritance doing so will error with "already applied"
      expectThat(mergedTestSubcomponent)
        .not().hasAnnotation(singleIn)
    }
  }
}
