// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.processors

import com.r0adkll.kimchi.compileKimchiWithTestSources
import com.r0adkll.kimchi.kotlinClass
import com.r0adkll.kimchi.topLevelFunctions
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.extensionReceiverParameter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.first
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class MergeComponentCreationTest {

  @TempDir(cleanup = CleanupMode.NEVER)
  lateinit var workingDir: File

  @Test
  fun `component with companion object generates creation ext function against it`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.MergeComponent

        @MergeComponent(TestScope::class)
        interface CompanionObjectComponent {
          companion object
        }
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val companionObject = kotlinClass("kimchi.CompanionObjectComponent\$Companion")
      val mergedComponent = classLoader.loadClass("kimchi.merge.kimchi.MergedCompanionObjectComponent")
      val functions = mergedComponent.topLevelFunctions("kimchi.merge.kimchi")
      expectThat(functions)
        .isNotNull()
        .first { it.extensionReceiverParameter != null }
        .get { extensionReceiverParameter }
        .isNotNull()
        .get { type.classifier }
        .isEqualTo(companionObject)
    }
  }

  @Test
  fun `component without companion object generates creation ext function against KClass`() {
    compileKimchiWithTestSources(
      """
        package kimchi

        import com.r0adkll.kimchi.annotations.MergeComponent

        @MergeComponent(TestScope::class)
        interface CompanionObjectComponent
      """.trimIndent(),
      workingDir = workingDir,
    ) {
      val mergedComponent = classLoader.loadClass("kimchi.merge.kimchi.MergedCompanionObjectComponent")
      val functions = mergedComponent.topLevelFunctions("kimchi.merge.kimchi")
      expectThat(functions)
        .isNotNull()
        .first { it.extensionReceiverParameter != null }
        .get { extensionReceiverParameter }
        .isNotNull()
        .get { type.classifier }
        .isEqualTo(KClass::class)
    }
  }
}
