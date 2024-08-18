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

  sourceSets {
    commonMain.dependencies {
      // Only here for doc linking
      compileOnly(libs.circuit.foundation)
      compileOnly(libs.kotlininject.runtime)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

android { namespace = "com.r0adkll.kimchi.circuit.annotations" }
