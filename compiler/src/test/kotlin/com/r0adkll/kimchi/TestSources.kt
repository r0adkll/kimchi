// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File
import java.io.OutputStream
import kotlin.reflect.KClass
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@Language("kotlin")
val TestScope = """
  package kimchi
  object TestScope
""".trimIndent()

// TODO: Migrate this to kotlin-reflect
val JvmCompilationResult.testScope: Class<*>
  get() = classLoader.loadClass("kimchi.TestScope")

@Language("kotlin")
val TestScope2 = """
  package kimchi
  object TestScope2
""".trimIndent()

val JvmCompilationResult.testScope2: KClass<*>
  get() = kotlinClass("kimchi.TestScope2")

@Language("kotlin")
val TestParentScope = """
  package kimchi
  object TestParentScope
""".trimIndent()

// TODO: Migrate this to kotlin-reflect
val JvmCompilationResult.testParentScope: Class<*>
  get() = classLoader.loadClass("kimchi.TestParentScope")

@Language("kotlin")
val TestQualifier = """
  package kimchi
  import me.tatarka.inject.annotations.Qualifier
  @Qualifier
  annotation class TestQualifier
""".trimIndent()

val JvmCompilationResult.testQualifier: KClass<*>
  get() = kotlinClass("kimchi.TestQualifier")

@Language("kotlin")
val TestComponent = """
  package kimchi
  import com.r0adkll.kimchi.annotations.MergeComponent
  @MergeComponent(TestScope::class)
  interface TestComponent {
    companion object
  }
""".trimIndent()

val JvmCompilationResult.testComponent: KClass<*>
  get() = kotlinClass("kimchi.TestComponent")

val JvmCompilationResult.mergedTestComponent: KClass<*>
  get() = kotlinClass("kimchi.merge.kimchi.MergedTestComponent")

@Language("kotlin")
val TestComponent2 = """
  package kimchi
  import com.r0adkll.kimchi.annotations.MergeComponent
  @MergeComponent(TestScope2::class)
  interface TestComponent2 {
    companion object
  }
""".trimIndent()

val JvmCompilationResult.testComponent2: KClass<*>
  get() = kotlinClass("kimchi.TestComponent2")

val JvmCompilationResult.mergedTestComponent2: KClass<*>
  get() = kotlinClass("kimchi.merge.kimchi.MergedTestComponent2")

@Language("kotlin")
val SingleIn = """
  package kimchi
  import kotlin.reflect.KClass
  @me.tatarka.inject.annotations.Scope
  annotation class SingleIn(val scope: KClass<*>)
""".trimIndent()

val JvmCompilationResult.singleIn: KClass<*>
  get() = kotlinClass("kimchi.SingleIn")

@Language("kotlin")
val ByteKey = """
  package kimchi
  import com.r0adkll.kimchi.annotations.MapKey
  @MapKey
  annotation class ByteKey(val value: Byte)
""".trimIndent()

@Language("kotlin")
val ShortKey = """
  package kimchi
  import com.r0adkll.kimchi.annotations.MapKey
  @MapKey
  annotation class ShortKey(val value: Short)
""".trimIndent()

@Language("kotlin")
val FloatKey = """
  package kimchi
  import com.r0adkll.kimchi.annotations.MapKey
  @MapKey
  annotation class FloatKey(val value: Float)
""".trimIndent()

@Language("kotlin")
val DoubleKey = """
  package kimchi
  import com.r0adkll.kimchi.annotations.MapKey
  @MapKey
  annotation class DoubleKey(val value: Double)
""".trimIndent()

@Language("kotlin")
val CharKey = """
  package kimchi
  import com.r0adkll.kimchi.annotations.MapKey
  @MapKey
  annotation class CharKey(val value: Char)
""".trimIndent()

@Language("kotlin")
val BooleanKey = """
  package kimchi
  import com.r0adkll.kimchi.annotations.MapKey
  @MapKey
  annotation class BooleanKey(val value: Boolean)
""".trimIndent()

enum class Keynum {
  First, Second, Third
}

@Language("kotlin")
val EnumKey = """
  package kimchi
  import com.r0adkll.kimchi.annotations.MapKey
  import com.r0adkll.kimchi.Keynum

  @MapKey
  annotation class EnumKey(val value: Keynum)
""".trimIndent()

/**
 * Add source files here to include in every compiler test
 */
val commonTestSources = arrayOf(
  TestScope,
  TestScope2,
  TestParentScope,
  TestQualifier,
  TestComponent,
  TestComponent2,
  SingleIn,
  ByteKey,
  ShortKey,
  FloatKey,
  DoubleKey,
  CharKey,
  BooleanKey,
  EnumKey,
)

/**
 * Test helper to compile kimchi compilers with common test source files
 */
@OptIn(ExperimentalCompilerApi::class)
fun compileKimchiWithTestSources(
  @Language("kotlin") vararg extraSource: String,
  messageOutputStream: OutputStream = System.out,
  workingDir: File? = null,
  symbolProcessorProviders: List<SymbolProcessorProvider> = emptyList(),
  expectExitCode: KotlinCompilation.ExitCode? = null,
  block: JvmCompilationResult.() -> Unit = { },
) {
  compileKimchi(
    *extraSource,
    *commonTestSources,
    messageOutputStream = messageOutputStream,
    workingDir = workingDir,
    symbolProcessorProviders = symbolProcessorProviders,
    expectExitCode = expectExitCode,
    block = block,
  )
}
