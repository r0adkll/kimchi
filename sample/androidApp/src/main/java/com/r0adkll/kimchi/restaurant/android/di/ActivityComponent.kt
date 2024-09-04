// Copyright (C) 2022 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.android.di

import android.app.Activity
import androidx.core.os.ConfigurationCompat
import com.r0adkll.kimchi.annotations.ContributesSubcomponent
import com.r0adkll.kimchi.restaurant.common.scopes.SingleIn
import com.r0adkll.kimchi.restaurant.common.scopes.UiScope
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.root.RestaurantContent
import java.util.Locale
import me.tatarka.inject.annotations.Provides

@SingleIn(UiScope::class)
@ContributesSubcomponent(
  scope = UiScope::class,
  parentScope = UserScope::class,
)
interface ActivityComponent {
  val restaurantContent: RestaurantContent

  @Provides
  fun provideActivityLocale(activity: Activity): Locale {
    return ConfigurationCompat.getLocales(activity.resources.configuration)
      .get(0) ?: Locale.getDefault()
  }

  @ContributesSubcomponent.Factory
  interface Factory {
    fun create(activity: Activity): ActivityComponent
  }
}
