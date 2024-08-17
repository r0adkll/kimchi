import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  kotlin("jvm")
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

dependencies {
  implementation(projects.sample.shared)
  implementation(compose.desktop.currentOs)
}

compose.desktop {
  application {
    mainClass = "com.r0adkll.kimchi.restaurant.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "com.r0adkll.kimchi.restaurant"
      packageVersion = "1.0.0"
    }
  }
}
