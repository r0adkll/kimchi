// Copyright (C) 2023 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.common

data class ApplicationInfo(
  val packageName: String,
  val debugBuild: Boolean,
  val flavor: Flavor,
  val versionName: String,
  val versionCode: Int,
)

enum class Flavor {
  Standard,
}
