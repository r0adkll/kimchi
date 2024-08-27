// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
plugins {
  kotlin("jvm")
  alias(libs.plugins.mavenPublish)
  id("java-test-fixtures")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xopt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    freeCompilerArgs.add("-Xexplicit-api=strict")
  }
}

dependencies {
  implementation(libs.kotlininject.runtime)
  implementation(libs.kotlin.ksp)
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)

  testFixturesApi(libs.kotlin.reflect)
  testFixturesApi(libs.kotlin.compile.testing)
  testFixturesApi(libs.kotlin.compile.testing.ksp)
  testFixturesImplementation(projects.compiler)
  testFixturesImplementation(projects.annotations)
  testFixturesImplementation(libs.strikt.core)
}
