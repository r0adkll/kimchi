plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
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
      baseName = "ui"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      api(projects.annotations)
      api(projects.circuit.annotations)

      implementation(libs.circuit.foundation)
      implementation(libs.circuit.runtime)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

dependencies {
  add("kspCommonMainMetadata", libs.kotlininject.ksp)
  add("kspCommonMainMetadata", projects.compiler)
  add("kspCommonMainMetadata", projects.circuit.compiler)
}

composeCompiler {
  enableStrongSkippingMode.set(true)
  includeSourceInformation.set(true)
}

android { namespace = "com.r0adkll.kimchi.restaurant.ui" }
