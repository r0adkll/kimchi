// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
plugins {
  kotlin("jvm")
  alias(libs.plugins.ksp)
  id("jvm-test-suite")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation(projects.annotations)
  implementation(libs.kotlininject.runtime)

  ksp(projects.compiler)
  ksp(libs.kotlininject.ksp)

  testImplementation(libs.bundles.junit5)
  testImplementation(libs.junit5.engine)
  testImplementation(libs.strikt.core)
}

testing {
  suites {
    named<JvmTestSuite>("test") {
      useJUnitJupiter(libs.versions.junit5)
    }
  }
}
