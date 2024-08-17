import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinParcelize)
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
      baseName = "common"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      api(projects.annotations)
      api(projects.circuit.annotations)

      api(libs.circuit.runtime)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }

  targets.configureEach {
    val isAndroidTarget = platformType == KotlinPlatformType.androidJvm
    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions {
          if (isAndroidTarget) {
            freeCompilerArgs.addAll(
              "-P",
              "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.r0adkll.kimchi.restaurant.common.screens.Parcelize",
            )
          }
        }
      }
    }
  }
}

android { namespace = "com.r0adkll.kimchi.restaurant.common" }
