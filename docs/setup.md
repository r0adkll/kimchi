Installation
============

This library builds upon [kotlin-inject](https://github.com/evant/kotlin-inject) so make sure you are setup to use it as well.

Setting up Kimchi is easy! Just add the following and start merging!

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.r0adkll.kimchi/kimchi-compiler.svg)](https://search.maven.org/search?q=g:com.r0adkll.kimchi)
[![Sonatype Snapshot](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.r0adkll.kimchi/kimchi-compiler.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/r0adkll/kimchi/)

### Add KSP Plugin to your project

```kotlin
plugins {
  id("com.google.devtools.ksp") version "2.0.20-1.0.24"
}
```

### Add annotations dependency

```kotlin
dependencies {
  implementation("com.r0adkll.kimchi:kimchi-annotations:<version>")

  // If you want to use @CircuitInject
  implementation("com.r0adkll.kimchi:kimchi-circuit-annotations:<version>")
}
```

### Add KSP compiler

=== "Android / Jvm"
    ```kotlin
    dependencies {
      ksp("com.r0adkll.kimchi:kimchi-compiler:<version>")

      // If you want to use @CircuitInject
      ksp("com.r0adkll.kimchi:kimchi-circuit-compiler:<version>")

      // Don't forget to add kotlin-inject if you use @MergeComponent
      ksp("me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.1")
    }
    ```

=== "Multiplatform"
    ```kotlin
    kotlin {
      sourceSets {
        commonMain {
          dependencies {
            implementation("com.r0adkll.kimchi:kimchi-annotations:<version>")
          }
        }
      }
    }

    dependencies {
      add("kspJvm", "com.r0adkll.kimchi:kimchi-compiler:<version>")
      add("kspAndroid", "com.r0adkll.kimchi:kimchi-compiler:<version>")
      add("kspIosArm64", "com.r0adkll.kimchi:kimchi-compiler:<version>")

      // Don't forget to add the kimchi-circuit-compiler when using @CircuitInject
      // Don't forget to add kotlin-inject if using @MergeComponent
    }
    ```
