// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.r0adkll.kimchi.compileKimchiWithTestSources
import com.r0adkll.kimchi.kotlinClass
import com.r0adkll.kimchi.mergedTestComponent
import com.r0adkll.kimchi.mergedTestComponent2
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import java.io.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.doesNotContain
import strikt.assertions.map

class ContributesToTest {

  @TempDir(cleanup = CleanupMode.ON_SUCCESS)
  lateinit var workingDir: File

  @Test
  fun `@ContributesTo interfaces are extended in the generated component`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesTo

        @ContributesTo(TestScope::class)
        interface Module
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = ExitCode.OK,
    ) {
      val module = kotlinClass("kimchi.Module")
      expectThat(mergedTestComponent)
        .get { supertypes }
        .map { it.classifier }
        .contains(module)
    }
  }

  @Test
  fun `@ContributesTo interfaces with replaces excludes correctly`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesTo

        @ContributesTo(TestScope::class)
        interface Module

        @ContributesTo(TestScope::class, replaces = [Module::class])
        interface OtherModule
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = ExitCode.OK,
    ) {
      val module = kotlinClass("kimchi.Module")
      val otherModule = kotlinClass("kimchi.OtherModule")
      expectThat(mergedTestComponent)
        .get { supertypes }
        .map { it.classifier }
        .and {
          doesNotContain(module)
          contains(otherModule)
        }
    }
  }

  @Test
  fun `multiple @ContributesTo uses contribute to their scopes`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesTo

        @ContributesTo(TestScope::class)
        @ContributesTo(TestScope2::class)
        interface Module
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = ExitCode.OK,
    ) {
      val module = kotlinClass("kimchi.Module")
      expectThat(mergedTestComponent)
        .get { supertypes }
        .map { it.classifier }
        .contains(module)

      expectThat(mergedTestComponent2)
        .get { supertypes }
        .map { it.classifier }
        .contains(module)
    }
  }

  @Test
  fun `multiple @ContributesTo with replaces properly excludes other module`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.ContributesTo

        @ContributesTo(TestScope2::class)
        interface OtherModule

        @ContributesTo(TestScope::class)
        @ContributesTo(TestScope2::class, replaces = [OtherModule::class])
        interface Module
      """.trimIndent(),
      workingDir = workingDir,
      expectExitCode = ExitCode.OK,
    ) {
      val module = kotlinClass("kimchi.Module")
      val otherModule = kotlinClass("kimchi.OtherModule")
      expectThat(mergedTestComponent)
        .get { supertypes }
        .map { it.classifier }
        .contains(module)

      expectThat(mergedTestComponent2)
        .get { supertypes }
        .map { it.classifier }
        .and {
          doesNotContain(otherModule)
          contains(module)
        }
    }
  }
}
