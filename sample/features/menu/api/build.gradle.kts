import java.util.Locale
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.ksp)
}

kotlin {
  jvm()
  androidTarget()

  applyDefaultHierarchyTemplate()

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach {
    it.binaries.framework {
      baseName = "menu_api"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      api(projects.annotations)
      api(projects.sample.common)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

addKspDependencyForAllTargets(projects.compiler)

android { namespace = "com.r0adkll.kimchi.restaurant.menu.api" }

private fun Project.addKspDependencyForAllTargets(dependencyNotation: Any) {
  val kmpExtension = extensions.getByType<KotlinMultiplatformExtension>()
  dependencies {
    kmpExtension.targets
      .asSequence()
      .filter { target ->
        // Don't add KSP for common target, only final platforms
        target.platformType != KotlinPlatformType.common
      }
      .forEach { target ->
        add(
          "ksp${target.targetName.capitalized()}",
          dependencyNotation,
        )
      }
  }
}

fun String.capitalized(): CharSequence = let<CharSequence, CharSequence> {
  if (it.isEmpty()) it else it[0].titlecase(
    Locale.getDefault()
  ) + it.substring(1)
}
