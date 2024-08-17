// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package com.r0adkll.kimchi.restaurant.di

import com.r0adkll.kimchi.annotations.MergeComponent
import com.r0adkll.kimchi.restaurant.common.scopes.AppScope
import com.r0adkll.kimchi.restaurant.common.scopes.SingleIn
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSUserDefaults

@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
abstract class IosApplicationComponent : SharedAppComponent {

  @Provides
  fun provideNsUserDefaults(): NSUserDefaults = NSUserDefaults.standardUserDefaults

  companion object
}
