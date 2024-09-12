// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.r0adkll.kimchi.Keynum
import com.r0adkll.kimchi.compileKimchiWithTestSources
import com.r0adkll.kimchi.kotlinClass
import com.r0adkll.kimchi.mergedTestComponent
import com.r0adkll.kimchi.withFunction
import com.tschuchort.compiletesting.JvmCompilationResult
import java.io.File
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.starProjectedType
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class ContributesMultibindingTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  lateinit var workingDir: File

  @ParameterizedTest
  @MethodSource("mapKeyTestParameters")
  fun `map keys types are generated correctly`(mapKeyTest: MapKeyTest) {
    compileKimchiWithTestSources(
      mapKeyTest.source,
      workingDir = workingDir,
    ) {
      expectThat(mergedTestComponent)
        .withFunction("provideBinding_${mapKeyTest.expectedMapKeyFunctionSuffix(this)}") {
          with({ returnType }) {
            get { classifier } isEqualTo Pair::class
            get { arguments }
              .contains(
                KTypeProjection(
                  variance = KVariance.INVARIANT,
                  type = mapKeyTest.expectedMapKey(this@compileKimchiWithTestSources)::class.starProjectedType,
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

  companion object {
    @JvmStatic
    fun mapKeyTestParameters(): List<MapKeyTest> = listOf(
      IntKeyTest,
      LongKeyTest,
      StringKeyTest,
      ByteKeyTest,
      ShortKeyTest,
      FloatKeyTest,
      DoubleKeyTest,
      CharKeyTest,
      BooleanKeyTest,
      EnumKeyTest,
    )
  }
}

val JvmCompilationResult.binding get() = kotlinClass("kimchi.Binding")

val IntKeyTest = MapKeyTest(
  """
    package kimchi
    import com.r0adkll.kimchi.annotations.ContributesMultibinding
    import com.r0adkll.kimchi.annotations.IntKey

    interface Binding

    @IntKey(0)
    @ContributesMultibinding(TestScope::class)
    object RealBinding : Binding
  """.trimIndent(),
  expectedMapKey = { 0 },
)

val LongKeyTest = MapKeyTest(
  """
    package kimchi
    import com.r0adkll.kimchi.annotations.ContributesMultibinding
    import com.r0adkll.kimchi.annotations.LongKey

    interface Binding

    @LongKey(0L)
    @ContributesMultibinding(TestScope::class)
    object RealBinding : Binding
  """.trimIndent(),
  expectedMapKey = { 0L },
)

val StringKeyTest = MapKeyTest(
  """
    package kimchi
    import com.r0adkll.kimchi.annotations.ContributesMultibinding
    import com.r0adkll.kimchi.annotations.StringKey

    interface Binding

    @StringKey("test")
    @ContributesMultibinding(TestScope::class)
    object RealBinding : Binding
  """.trimIndent(),
  expectedMapKey = { "test" },
)

val ByteKeyTest = MapKeyTest(
  """
    package kimchi
    import com.r0adkll.kimchi.annotations.ContributesMultibinding

    interface Binding

    @ByteKey(0.toByte())
    @ContributesMultibinding(TestScope::class)
    object RealBinding : Binding
  """.trimIndent(),
  expectedMapKey = { 0.toByte() },
  expectedMapKeyFunctionSuffix = { "0b" },
)

val ShortKeyTest = MapKeyTest(
  """
    package kimchi
    import com.r0adkll.kimchi.annotations.ContributesMultibinding

    interface Binding

    @ShortKey(0.toShort())
    @ContributesMultibinding(TestScope::class)
    object RealBinding : Binding
  """.trimIndent(),
  expectedMapKey = { 0.toShort() },
  expectedMapKeyFunctionSuffix = { "0s" },
)

val FloatKeyTest = MapKeyTest(
  """
    package kimchi
    import com.r0adkll.kimchi.annotations.ContributesMultibinding

    interface Binding

    @FloatKey(0f)
    @ContributesMultibinding(TestScope::class)
    object RealBinding : Binding
  """.trimIndent(),
  expectedMapKey = { 0f },
  expectedMapKeyFunctionSuffix = { "0_0" },
)

val DoubleKeyTest = MapKeyTest(
  """
    package kimchi
    import com.r0adkll.kimchi.annotations.ContributesMultibinding

    interface Binding

    @DoubleKey(0.0)
    @ContributesMultibinding(TestScope::class)
    object RealBinding : Binding
  """.trimIndent(),
  expectedMapKey = { 0.0 },
  expectedMapKeyFunctionSuffix = { "0_0" },
)

val CharKeyTest = MapKeyTest(
  """
    package kimchi
    import com.r0adkll.kimchi.annotations.ContributesMultibinding

    interface Binding

    @CharKey('A')
    @ContributesMultibinding(TestScope::class)
    object RealBinding : Binding
  """.trimIndent(),
  expectedMapKey = { 'A' },
)

val BooleanKeyTest = MapKeyTest(
  """
    package kimchi
    import com.r0adkll.kimchi.annotations.ContributesMultibinding

    interface Binding

    @BooleanKey(true)
    @ContributesMultibinding(TestScope::class)
    object RealBinding : Binding
  """.trimIndent(),
  expectedMapKey = { true },
)

val EnumKeyTest = MapKeyTest(
  """
    package kimchi
    import com.r0adkll.kimchi.annotations.ContributesMultibinding
    import com.r0adkll.kimchi.Keynum

    interface Binding

    @EnumKey(Keynum.First)
    @ContributesMultibinding(TestScope::class)
    object RealBinding : Binding
  """.trimIndent(),
  expectedMapKey = { Keynum.First },
  expectedMapKeyFunctionSuffix = { "provideBinding_com_r0adkll_kimchi_Keynum_First" },
)

data class MapKeyTest(
  @Language("kotlin") val source: String,
  val expectedMapKey: JvmCompilationResult.() -> Any,
  val expectedMapKeyFunctionSuffix: JvmCompilationResult.() -> String = { expectedMapKey().toString() },
)
