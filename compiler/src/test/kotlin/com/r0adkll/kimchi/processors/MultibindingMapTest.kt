// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.r0adkll.kimchi.compileKimchiWithTestSources
import com.r0adkll.kimchi.kotlinClass
import com.r0adkll.kimchi.mergedTestComponent
import com.r0adkll.kimchi.mergedTestComponent2
import com.r0adkll.kimchi.withFunction
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.starProjectedType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.none

class MultibindingMapTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  lateinit var workingDir: File

  @Test
  fun `Multiple @ContributesBindings with explicit boundType binds to that type`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesMultibinding
        import com.r0adkll.kimchi.annotations.StringKey

        interface UnderBinding
        interface Binding : UnderBinding

        @StringKey("test")
        @ContributesMultibinding(TestScope::class)
        @ContributesMultibinding(TestScope::class, boundType = UnderBinding::class)
        object RealBinding : Binding
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.OK,
    ) {
      val underBinding = kotlinClass("kimchi.UnderBinding")
      expectThat(mergedTestComponent)
        .withFunction("provideBinding_test") {
          with({ returnType }) {
            get { classifier } isEqualTo Pair::class
            get { arguments }
              .contains(
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = String::class.starProjectedType,
                ),
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = binding.starProjectedType,
                ),
              )
          }
        }
        .withFunction("provideUnderBinding_test") {
          with({ returnType }) {
            get { classifier } isEqualTo Pair::class
            get { arguments }
              .contains(
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = String::class.starProjectedType,
                ),
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = underBinding.starProjectedType,
                ),
              )
          }
        }
    }
  }

  @Test
  fun `Multiple @ContributesBindings with map keys are supported`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesMultibinding
        import com.r0adkll.kimchi.annotations.StringKey

        interface Binding

        @StringKey("test")
        @ContributesMultibinding(TestScope::class)
        @ContributesMultibinding(TestScope2::class)
        object RealBinding : Binding
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.OK,
    ) {
      expectThat(mergedTestComponent)
        .withFunction("provideBinding_test") {
          with({ returnType }) {
            get { classifier } isEqualTo Pair::class
            get { arguments }
              .contains(
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = String::class.starProjectedType,
                ),
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = binding.starProjectedType,
                ),
              )
          }
        }

      expectThat(mergedTestComponent2)
        .withFunction("provideBinding_test") {
          with({ returnType }) {
            get { classifier } isEqualTo Pair::class
            get { arguments }
              .contains(
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = String::class.starProjectedType,
                ),
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = binding.starProjectedType,
                ),
              )
          }
        }
    }
  }

  @Test
  fun `Multiple @ContributesBindings with replaces excludes correctly`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesMultibinding
        import com.r0adkll.kimchi.annotations.StringKey
        import me.tatarka.inject.annotations.Inject

        interface Binding

        @StringKey("test")
        @ContributesMultibinding(TestScope::class)
        @ContributesMultibinding(TestScope2::class, replaces = [OtherRealBinding::class])
        object RealBinding : Binding

        @StringKey("test")
        @ContributesMultibinding(TestScope2::class)
        @Inject
        class OtherRealBinding : Binding
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.OK,
    ) {
      val otherRealBinding = kotlinClass("kimchi.OtherRealBinding")
      expectThat(mergedTestComponent)
        .withFunction("provideBinding_test") {
          with({ returnType }) {
            get { classifier } isEqualTo Pair::class
            get { arguments }
              .contains(
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = String::class.starProjectedType,
                ),
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = binding.starProjectedType,
                ),
              )
          }
        }

      expectThat(mergedTestComponent2)
        .withFunction("provideBinding_test") {
          get { parameters }
            .none {
              get { type.classifier } isEqualTo otherRealBinding
            }
          with({ returnType }) {
            get { classifier } isEqualTo Pair::class
            get { arguments }
              .contains(
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = String::class.starProjectedType,
                ),
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = binding.starProjectedType,
                ),
              )
          }
        }
    }
  }

  @Test
  fun `Multiple @ContributesBindings with explicit boundType generates correctly`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesMultibinding
        import com.r0adkll.kimchi.annotations.StringKey
        import me.tatarka.inject.annotations.Inject

        interface UnderBinding
        interface Binding : UnderBinding

        @StringKey("test")
        @ContributesMultibinding(TestScope::class)
        @ContributesMultibinding(TestScope2::class, boundType = UnderBinding::class)
        object RealBinding : Binding
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = KotlinCompilation.ExitCode.OK,
    ) {
      val underBinding = kotlinClass("kimchi.UnderBinding")
      expectThat(mergedTestComponent)
        .withFunction("provideBinding_test") {
          with({ returnType }) {
            get { classifier } isEqualTo Pair::class
            get { arguments }
              .contains(
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = String::class.starProjectedType,
                ),
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = binding.starProjectedType,
                ),
              )
          }
        }

      expectThat(mergedTestComponent2)
        .withFunction("provideUnderBinding_test") {
          with({ returnType }) {
            get { classifier } isEqualTo Pair::class
            get { arguments }
              .contains(
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = String::class.starProjectedType,
                ),
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = underBinding.starProjectedType,
                ),
              )
          }
        }
    }
  }
}

private val JvmCompilationResult.binding get() = kotlinClass("kimchi.Binding")
private val JvmCompilationResult.realBinding get() = kotlinClass("kimchi.RealBinding")
