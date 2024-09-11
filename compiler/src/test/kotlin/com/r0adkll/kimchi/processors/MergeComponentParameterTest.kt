// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.r0adkll.kimchi.compileKimchiWithTestSources
import com.r0adkll.kimchi.hasAnnotation
import com.r0adkll.kimchi.kotlinClass
import com.r0adkll.kimchi.parameter
import com.r0adkll.kimchi.primaryConstructor
import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File
import kotlin.reflect.full.declaredMemberProperties
import me.tatarka.inject.annotations.Component
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotNull
import strikt.assertions.none

class MergeComponentParameterTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  lateinit var workingDir: File

  @Test
  fun `@MergeComponent abstract class parameters get passed on`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.MergeComponent

        object ParamScope

        @MergeComponent(ParamScope::class)
        abstract class ParameterComponent(
          val param: Any,
        ) {
            companion object
        }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val mergedParameterComponent = kotlinClass("kimchi.merge.kimchi.MergedParameterComponent")

      expectThat(mergedParameterComponent)
        .get { declaredMemberProperties }
        .none {
          get { name } isEqualTo "param"
        }
    }
  }

  @Test
  fun `@MergeComponent abstract class parameters annotations are not passed in implementation`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.MergeComponent

        object ParamScope
        annotation class Annotation1
        annotation class Annotation2

        @MergeComponent(ParamScope::class)
        abstract class ParameterComponent(
          @Annotation1 val param1: String,
          @get:Annotation2 val param2: Boolean,
        ) {
            companion object
        }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val mergedParameterComponent = kotlinClass("kimchi.merge.kimchi.MergedParameterComponent")

      expectThat(mergedParameterComponent)
        .primaryConstructor()
        .isNotNull()
        .get { parameters }
        .none {
          get { annotations }.isNotEmpty()
        }
    }
  }

  @Test
  fun `@MergeComponent with parent @Component open val parameters compile`() {
    print(workingDir.absolutePath)
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.MergeComponent
        import me.tatarka.inject.annotations.Component

        object ParamScope

        @Component
        abstract class ParentComponent

        @MergeComponent(ParamScope::class)
        abstract class ParameterComponent(
          @Component open val parent: ParentComponent,
        ) {
            companion object
        }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val mergedParameterComponent = kotlinClass("kimchi.merge.kimchi.MergedParameterComponent")

      expectThat(mergedParameterComponent) {
        primaryConstructor()
          .isNotNull()
          .parameter(0)
          .and {
            get { name } isEqualTo "parent"
            hasAnnotation(Component::class)
          }

        get { declaredMemberProperties }
          .any {
            get { name } isEqualTo "parent"
          }
      }
    }
  }

  @Test
  fun `@MergeComponent with parent @Component non-value parameter compile`() {
    print(workingDir.absolutePath)
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.MergeComponent
        import me.tatarka.inject.annotations.Component

        object ParamScope

        @Component
        abstract class ParentComponent

        @MergeComponent(ParamScope::class)
        abstract class ParameterComponent(
          @Component parent: ParentComponent,
        ) {
            companion object
        }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val mergedParameterComponent = kotlinClass("kimchi.merge.kimchi.MergedParameterComponent")

      expectThat(mergedParameterComponent) {
        primaryConstructor()
          .isNotNull()
          .parameter(0)
          .and {
            get { name } isEqualTo "parent"
            hasAnnotation(Component::class)
          }

        get { declaredMemberProperties }
          .any {
            get { name } isEqualTo "parent"
          }
      }
    }
  }

  @Test
  fun `@MergeComponent with parent @Component non-open val parameter fails`() {
    print(workingDir.absolutePath)
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.MergeComponent
        import me.tatarka.inject.annotations.Component

        object ParamScope

        @Component
        abstract class ParentComponent

        @MergeComponent(ParamScope::class)
        abstract class ParameterComponent(
          @Component val parent: ParentComponent,
        ) {
            companion object
        }
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.INTERNAL_ERROR,
    )
  }

  @Test
  fun `@MergeComponent with parent @Component var parameter fails`() {
    print(workingDir.absolutePath)
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.MergeComponent
        import me.tatarka.inject.annotations.Component

        object ParamScope

        @Component
        abstract class ParentComponent

        @MergeComponent(ParamScope::class)
        abstract class ParameterComponent(
          @Component var parent: ParentComponent,
        ) {
            companion object
        }
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.INTERNAL_ERROR,
    )
  }
}
