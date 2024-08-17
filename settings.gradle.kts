// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "kimchi"

include(":annotations")
include(
  ":compiler",
  ":compiler-utils",
)
include(
  ":circuit:annotations",
  ":circuit:compiler",
)
include(
  ":sample:androidApp",
  ":sample:desktopApp",
  ":sample:shared",
  ":sample:common",
  ":sample:features:menu:api",
  ":sample:features:menu:impl",
  ":sample:features:menu:ui",
)

// https://docs.gradle.org/5.6/userguide/groovy_plugin.html#sec:groovy_compilation_avoidance
enableFeaturePreview("GROOVY_COMPILATION_AVOIDANCE")

// https://docs.gradle.org/current/userguide/declaring_dependencies.html#sec:type-safe-project-accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
