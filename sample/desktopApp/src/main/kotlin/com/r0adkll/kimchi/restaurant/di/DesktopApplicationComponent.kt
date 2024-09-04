// Copyright (C) 2023 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.di

import androidx.compose.ui.unit.Density
import com.r0adkll.kimchi.annotations.MergeComponent
import com.r0adkll.kimchi.restaurant.common.scopes.AppScope
import com.r0adkll.kimchi.restaurant.common.scopes.SingleIn
import java.util.prefs.Preferences
import me.tatarka.inject.annotations.Provides

@SingleIn(AppScope::class)
@MergeComponent(
  scope = AppScope::class,
)
abstract class DesktopApplicationComponent : SharedAppComponent {

  @SingleIn(AppScope::class)
  @Provides
  fun providePreferences(): Preferences = Preferences.userRoot().node("app.campfire")

  @Provides
  fun provideDensity(): Density = Density(density = 1f)

  companion object
}
