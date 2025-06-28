// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
import java.util.Locale
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.ksp)
}

kotlin {
  jvm()
  androidTarget()

  applyDefaultHierarchyTemplate()

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach {
    it.binaries.framework {
      baseName = "shared"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      api(projects.annotations)

      api(projects.sample.common)
      api(projects.sample.features.menu.ui)
      api(projects.sample.features.menu.impl)

      api(libs.circuit.runtime)
      api(libs.circuit.foundation)
      api(libs.circuit.overlay)
      api(libs.circuitx.gesturenav)

      api(compose.ui)
      api(compose.material)
      api(compose.material3)
      api(compose.materialIconsExtended)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

addKspDependencyForAllTargets(libs.kotlininject.ksp)
addKspDependencyForAllTargets(projects.compiler)

composeCompiler {
  includeSourceInformation.set(true)
}

android { namespace = "com.r0adkll.kimchi.restaurant" }

private fun Project.addKspDependencyForAllTargets(dependencyNotation: Any) {
  val kmpExtension = extensions.getByType<KotlinMultiplatformExtension>()
  dependencies {
    kmpExtension.targets
      .asSequence()
      .filter { target ->
        // Don't add KSP for common target, only final platforms
        target.platformType != KotlinPlatformType.common
      }
      .forEach { target ->
        add(
          "ksp${target.targetName.capitalized()}",
          dependencyNotation,
        )
      }
  }
}

fun String.capitalized(): CharSequence = let<CharSequence, CharSequence> {
  if (it.isEmpty()) {
    it
  } else it[0].titlecase(
    Locale.getDefault(),
  ) + it.substring(1)
}
