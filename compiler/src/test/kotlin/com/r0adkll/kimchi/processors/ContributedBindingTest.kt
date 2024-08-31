// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.r0adkll.kimchi.compileKimchiWithTestSources
import com.r0adkll.kimchi.declaredProperties
import com.r0adkll.kimchi.getter
import com.r0adkll.kimchi.hasAnnotation
import com.r0adkll.kimchi.hasReceiverOf
import com.r0adkll.kimchi.hasReturnType
import com.r0adkll.kimchi.hasReturnTypeOf
import com.r0adkll.kimchi.kotlinClass
import com.r0adkll.kimchi.mergedTestComponent
import com.r0adkll.kimchi.testQualifier
import com.r0adkll.kimchi.withFunction
import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File
import me.tatarka.inject.annotations.Provides
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.withElementAt
import strikt.assertions.withFirst

class ContributedBindingTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  lateinit var workingDir: File

  @Test
  fun `contributed binding gets added to a component`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.annotations.ContributesBinding

        interface Binding

        @ContributesBinding(TestScope::class)
        @Inject
        class RealBinding : Binding
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val binding = kotlinClass("kimchi.Binding")
      val realBinding = kotlinClass("kimchi.RealBinding")

      expectThat(mergedTestComponent)
        .declaredProperties()
        .withFirst {
          hasReceiverOf(realBinding)
          hasReturnTypeOf(binding)
          getter()
            .hasAnnotation(Provides::class)
        }
    }
  }

  @Test
  fun `contributed binding without @Inject fails`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.annotations.ContributesBinding

        interface Binding

        @ContributesBinding(TestScope::class)
        class RealBinding(val param: Any) : Binding
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.INTERNAL_ERROR,
    )
  }

  @Test
  fun `contributed binding object class gets added to a component`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.annotations.ContributesBinding

        interface Binding

        @ContributesBinding(TestScope::class)
        object RealBinding : Binding
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val binding = kotlinClass("kimchi.Binding")
      expectThat(mergedTestComponent)
        .withFunction("provideRealBinding") {
          hasAnnotation(Provides::class)
          hasReturnType(binding)
        }
    }
  }

  @Test
  fun `contributed binding is bound with explicit type`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.annotations.ContributesBinding

        interface Binding
        interface Binding2

        @ContributesBinding(TestScope::class, boundType = Binding2::class)
        @Inject
        class RealBinding : Binding, Binding2
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val binding = kotlinClass("kimchi.Binding2")
      val realBinding = kotlinClass("kimchi.RealBinding")

      expectThat(mergedTestComponent)
        .declaredProperties()
        .withFirst {
          hasReceiverOf(realBinding)
          hasReturnTypeOf(binding)
          getter()
            .hasAnnotation(Provides::class)
        }
    }
  }

  @Test
  fun `contributed binding with multiple supertypes fail`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.annotations.ContributesBinding

        interface Binding
        interface Binding2

        @ContributesBinding(TestScope::class)
        @Inject
        class RealBinding : Binding, Binding2
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.INTERNAL_ERROR,
    )
  }

  @Test
  fun `contributed binding with no supertypes fail`() {
    println(workingDir.absolutePath)
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.annotations.ContributesBinding

        @ContributesBinding(TestScope::class)
        @Inject
        class FailedBinding
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.INTERNAL_ERROR,
    )
  }

  @Test
  fun `contributed binding qualifier gets added to a component`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.annotations.ContributesBinding

        interface Binding

        @TestQualifier
        @ContributesBinding(TestScope::class)
        @Inject
        class RealBinding : Binding
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      expectThat(mergedTestComponent)
        .declaredProperties()
        .first()
        .getter()
        .hasAnnotation(testQualifier)
    }
  }

  @Test
  fun `contributed binding will replace another binding`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.annotations.ContributesBinding

        interface Binding

        @ContributesBinding(TestScope::class)
        @Inject
        class RealBinding : Binding

        @ContributesBinding(
          scope = TestScope::class,
          replaces = [RealBinding::class],
        )
        @Inject
        class RealBinding2 : Binding
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val binding = kotlinClass("kimchi.Binding")
      val realBinding2 = kotlinClass("kimchi.RealBinding2")
      expectThat(mergedTestComponent)
        .declaredProperties()
        .hasSize(1)
        .withFirst {
          hasReceiverOf(realBinding2)
          hasReturnTypeOf(binding)
        }
    }
  }

  @Test
  fun `duplicate bindings will fail`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.annotations.ContributesBinding

        interface Binding

        @ContributesBinding(TestScope::class)
        @Inject
        class RealBinding : Binding

        @ContributesBinding(TestScope::class)
        @Inject
        class RealBinding2 : Binding
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.INTERNAL_ERROR,
    )
  }

  @Test
  fun `qualifier does not cause duplicate bindings`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.annotations.ContributesBinding

        interface Binding

        @ContributesBinding(TestScope::class)
        @Inject
        class RealBinding : Binding

        @TestQualifier
        @ContributesBinding(TestScope::class)
        @Inject
        class RealBinding2 : Binding
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val binding = kotlinClass("kimchi.Binding")
      val realBinding = kotlinClass("kimchi.RealBinding")
      val realBinding2 = kotlinClass("kimchi.RealBinding2")
      expectThat(mergedTestComponent)
        .declaredProperties()
        .hasSize(2)
        .withElementAt(0) {
          hasReceiverOf(realBinding)
          hasReturnTypeOf(binding)
        }
        .withElementAt(1) {
          hasReceiverOf(realBinding2)
          hasReturnTypeOf(binding)
          getter()
            .hasAnnotation(testQualifier)
        }
    }
  }

  @Test
  fun `contributed binding of higher rank will replace another binding`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import me.tatarka.inject.annotations.Inject
        import com.r0adkll.kimchi.annotations.ContributesBinding

        interface Binding

        @ContributesBinding(
          scope = TestScope::class,
          rank = 10,
        )
        @Inject
        class RealBinding : Binding

        @ContributesBinding(
          scope = TestScope::class,
          rank = 100,
        )
        @Inject
        class RealBinding2 : Binding
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val binding = kotlinClass("kimchi.Binding")
      val realBinding2 = kotlinClass("kimchi.RealBinding2")
      expectThat(mergedTestComponent)
        .declaredProperties()
        .hasSize(1)
        .withFirst {
          hasReceiverOf(realBinding2)
          hasReturnTypeOf(binding)
        }
    }
  }
}
