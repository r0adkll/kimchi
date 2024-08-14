plugins {
  kotlin("jvm")
  alias(libs.plugins.mavenPublish)
}

dependencies {
  implementation(libs.kotlininject.runtime)
  implementation(libs.kotlin.ksp)
  implementation(libs.kotlinpoet)
  implementation(libs.kotlinpoet.ksp)

  implementation(projects.annotations)
}
