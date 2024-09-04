// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
plugins {
  kotlin("jvm")
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.ksp)
}

dependencies {
  implementation(libs.autoservice.annotations)
  implementation(libs.kotlininject.runtime)
  implementation(libs.kotlin.ksp)
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)

  implementation(projects.annotations)
  implementation(projects.circuit.annotations)
  implementation(projects.compilerUtils)

  ksp(libs.ksp.autoservice)
}
