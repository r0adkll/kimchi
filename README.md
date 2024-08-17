![](.github/art/banner.png)

# Kimchi

**K** - _Kotlin_
**i** - _Inject_
**m** - _Merge_
**c** - _Component_
**h** - _Hints_
**i** - _Intuitively_

Kimchi is an [Anvil](https://github.com/square/anvil)-like KSP processor for [kotlin-inject](https://github.com/evant/kotlin-inject) that lets you contribute bindings, modules, and subcomponents across multi-module projects to form your dependency injection graph without manual wiring.

## Getting started

[![Maven Central](https://img.shields.io/maven-central/v/com.r0adkll.kimchi/kimchi-compiler.svg)](https://search.maven.org/search?q=g:com.r0adkll.kimchi)
[![Sonatype Snapshot](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.r0adkll.kimchi/kimchi-compiler.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/r0adkll/kimchi/)

### Download

`settings.gradle.kts`

```kotlin
pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}
```

`build.gradle.kts`

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.10"
    id("com.google.devtools.ksp") version "2.0.10-1.0.24"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("me.tatarka.inject:kotlin-inject-runtime:0.7.1")
    ksp("me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.1")

    implementation("com.r0adkll.kimchi:kimchi-annotations:<latest_version>")
    ksp("com.r0adkll.kimchi:kimchi-compiler:<latest_version>")

    // For Circuit Support
    implementation("com.r0adkll.kimchi:kimchi-circuit-annotations:<latest_version>")
    ksp("com.r0adkll.kimchi:kimchi-circuit-compiler:<latest_version>")
}
```

🚧 _THIS LIBRARY IS A WIP_ 🚧

## License

```
Copyright 2024 r0adkll

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
