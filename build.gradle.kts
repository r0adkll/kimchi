// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessExtensionPredeclare
import com.diffplug.spotless.LineEnding
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import java.net.URI
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlinAndroid) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.kotlinParcelize) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.mavenPublish) apply false
  alias(libs.plugins.spotless)
}

allprojects {
  apply(plugin = "com.diffplug.spotless")
  val spotlessFormatters: SpotlessExtension.() -> Unit = {
    lineEndings = LineEnding.PLATFORM_NATIVE

    format("misc") {
      target("*.md", ".gitignore")
      endWithNewline()
    }
    kotlin {
      target("src/**/*.kt")
      ktlint(libs.versions.ktlint.get())
      trimTrailingWhitespace()
      endWithNewline()
      targetExclude("**/spotless.kt")
    }
    kotlinGradle {
      target("*.kts")
      ktlint(libs.versions.ktlint.get())
      trimTrailingWhitespace()
      endWithNewline()
      licenseHeaderFile(
        rootProject.file("spotless/spotless.kt"),
        "(import|plugins|buildscript|dependencies|pluginManagement|dependencyResolutionManagement)",
      )
    }

    // Apply license formatting separately for kotlin files so we can prevent it from overwriting
    // copied files
    format("license") {
      licenseHeaderFile(rootProject.file("spotless/spotless.kt"), "(package|@file:)")
      target("src/**/*.kt")
    }
  }
  configure<SpotlessExtension> {
    spotlessFormatters()
    if (project.rootProject == project) {
      predeclareDeps()
    }
  }
  if (project.rootProject == project) {
    configure<SpotlessExtensionPredeclare> { spotlessFormatters() }
  }
}

subprojects {
  val isPublished = project.hasProperty("POM_ARTIFACT_ID")

  pluginManager.withPlugin("com.vanniktech.maven.publish") {
    apply(plugin = "org.jetbrains.dokka")

    tasks.withType<DokkaTaskPartial>().configureEach {
      moduleName.set(project.path.removePrefix(":").replace(":", "/"))
      outputDirectory.set(layout.buildDirectory.dir("docs/partial"))
      dokkaSourceSets.configureEach {
        val readMeProvider = project.layout.projectDirectory.file("README.md")
        if (readMeProvider.asFile.exists()) {
          includes.from(readMeProvider)
        }

        if (name.contains("androidTest", ignoreCase = true)) {
          suppress.set(true)
        }
        skipDeprecated.set(true)

        // Skip internal packages
        perPackageOption {
          // language=RegExp
          matchingRegex.set(".*\\.internal\\..*")
          suppress.set(true)
        }
        // AndroidX and Android docs are automatically added by the Dokka plugin.

        // Add source links
        sourceLink {
          localDirectory.set(layout.projectDirectory.dir("src").asFile)
          val relPath = rootProject.projectDir.toPath().relativize(projectDir.toPath())
          remoteUrl.set(
            providers.gradleProperty("POM_SCM_URL").map { scmUrl ->
              URI("$scmUrl/tree/main/$relPath/src").toURL()
            },
          )
          remoteLineSuffix.set("#L")
        }
      }
    }

    configure<MavenPublishBaseExtension> {
      publishToMavenCentral(automaticRelease = true)
      signAllPublications()
    }
  }

  // Common android config
  val commonAndroidConfig: CommonExtension<*, *, *, *, *, *>.() -> Unit = {
    compileSdk = 34

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
    }
  }

  // Android library config
  pluginManager.withPlugin("com.android.library") {
    with(extensions.getByType<LibraryExtension>()) {
      commonAndroidConfig()
      defaultConfig { minSdk = 21 }
      testOptions { targetSdk = 34 }
    }

    // Single-variant libraries
    extensions.configure<LibraryAndroidComponentsExtension> {
      beforeVariants { builder ->
        if (builder.buildType == "debug") {
          builder.enable = false
        }
      }
    }
  }
}
