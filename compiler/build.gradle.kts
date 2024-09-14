// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
plugins {
  kotlin("jvm")
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.ksp)
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xopt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
  }
}

dependencies {
  implementation(libs.autoservice.annotations)
  implementation(libs.kotlininject.runtime)
  implementation(libs.kotlin.ksp)
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)

  implementation(projects.annotations)
  implementation(projects.compilerUtils)

  ksp(libs.ksp.autoservice)

  testImplementation(testFixtures(projects.compilerUtils))
  testImplementation(libs.kotlin.compile.testing.ksp)
  testImplementation(libs.bundles.junit5)
  testImplementation(libs.junit5.engine)
  testImplementation(libs.strikt.core)
  testImplementation(libs.kotlininject.ksp)
}

tasks {
  test {
    useJUnitPlatform()
    maxHeapSize = "2g"
  }
}
