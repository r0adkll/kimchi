// Copyright (C) 2025 r0adkll
// SPDX-License-Identifier: Apache-2.0
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.mavenPublish)
}

kotlin {
  androidTarget { publishLibraryVariants("release") }
  jvm()

  applyDefaultHierarchyTemplate()

  iosX64()
  iosArm64()
  iosSimulatorArm64()
  macosX64()
  macosArm64()
  js(IR) {
    outputModuleName = property("POM_ARTIFACT_ID").toString()
    browser()
  }
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    outputModuleName = property("POM_ARTIFACT_ID").toString()
    browser()
  }

  sourceSets {
    commonMain.dependencies {
      // Only here for doc linking
      compileOnly(libs.circuit.foundation)
      compileOnly(libs.kotlininject.runtime)
    }
  }
}

android { namespace = "com.r0adkll.kimchi.circuit.annotations" }
androidComponents { beforeVariants { variant -> variant.androidTest.enable = false } }
