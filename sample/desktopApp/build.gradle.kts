// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("jvm")
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.ksp)
}

dependencies {
  implementation(projects.annotations)
  implementation(projects.sample.shared)
  implementation(libs.kotlininject.runtime)

  implementation(compose.desktop.currentOs)

  ksp(projects.compiler)
  ksp(libs.kotlininject.ksp)
}

compose.desktop {
  application {
    mainClass = "com.r0adkll.kimchi.restaurant.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.r0adkll.kimchi.restaurant"
      packageVersion = "1.0.0"
    }
  }
}
