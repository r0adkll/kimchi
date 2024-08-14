plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.mavenPublish)
}

kotlin {
  androidTarget { publishLibraryVariants("release") }
  jvm()

  applyDefaultHierarchyTemplate()

  iosX64()
  iosArm64()
  iosSimulatorArm64()
  macosX64()
  macosArm64()

  sourceSets {
    commonMain.dependencies {
      //put your multiplatform dependencies here
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

android { namespace = "com.r0adkll.kimchi.annotations" }
