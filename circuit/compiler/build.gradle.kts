// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
plugins {
  kotlin("jvm")
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.ksp)
  id("jvm-test-suite")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xopt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
  }
}

dependencies {
  implementation(libs.autoservice.annotations)
  implementation(libs.kotlin.ksp)
  implementation(libs.kotlininject.runtime)
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)

  implementation(projects.annotations)
  implementation(projects.circuit.annotations)
  implementation(projects.compilerUtils)

  ksp(libs.ksp.autoservice)

  testImplementation(testFixtures(projects.compilerUtils))
  testImplementation(libs.bundles.junit5)
  testImplementation(libs.circuit.runtime)
  testImplementation(libs.circuit.foundation)
  testImplementation(libs.junit5.engine)
  testImplementation(libs.kotlin.compile.testing.ksp)
  testImplementation(libs.strikt.core)
}

testing {
  suites {
    named<JvmTestSuite>("test") {
      useJUnitJupiter(libs.versions.junit5)
    }
  }
}
