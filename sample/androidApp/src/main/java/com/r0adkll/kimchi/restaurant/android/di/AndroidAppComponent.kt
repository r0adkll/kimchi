// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.android.di

import android.app.Application
import com.r0adkll.kimchi.annotations.MergeComponent
import com.r0adkll.kimchi.restaurant.android.BuildConfig
import com.r0adkll.kimchi.restaurant.common.ApplicationInfo
import com.r0adkll.kimchi.restaurant.common.Flavor
import com.r0adkll.kimchi.restaurant.common.scopes.AppScope
import com.r0adkll.kimchi.restaurant.common.scopes.SingleIn
import com.r0adkll.kimchi.restaurant.di.SharedAppComponent
import me.tatarka.inject.annotations.Provides

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
abstract class AndroidAppComponent(
  @get:Provides val application: Application,
) : SharedAppComponent {

  @SingleIn(AppScope::class)
  @Provides
  fun provideApplicationInfo(application: Application): ApplicationInfo {
    val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)

    return ApplicationInfo(
      packageName = application.packageName,
      debugBuild = BuildConfig.DEBUG,
      flavor = Flavor.Standard,
      versionName = packageInfo.versionName,
      versionCode = packageInfo.versionCode,
    )
  }

  companion object
}
