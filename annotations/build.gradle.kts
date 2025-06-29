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
  jvm()
  androidTarget { publishLibraryVariants("release") }

  applyDefaultHierarchyTemplate()

  iosX64()
  iosArm64()
  iosSimulatorArm64()
  linuxArm64()
  linuxX64()
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
      api(libs.kotlininject.runtime)
    }
  }

  compilerOptions {
    jvmToolchain(17)
  }
}

android { namespace = "com.r0adkll.kimchi.annotations" }
androidComponents { beforeVariants { variant -> variant.androidTest.enable = false } }
