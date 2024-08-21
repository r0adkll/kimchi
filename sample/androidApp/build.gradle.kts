// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlinAndroid)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.r0adkll.kimchi.restaurant.android"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.r0adkll.kimchi.restaurant.android"
    minSdk = 26
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  implementation(projects.annotations)
  implementation(projects.sample.shared)

  implementation(libs.androidx.activity.compose)
  implementation(libs.circuit.runtime)
  implementation(libs.circuit.foundation)

  ksp(projects.compiler)
  ksp(libs.kotlininject.ksp)
}
