// Copyright 2023, Drew Heavner and the Campfire project contributors
// SPDX-License-Identifier: Apache-2.0

package com.r0adkll.kimchi.restaurant.di

import com.r0adkll.kimchi.annotations.ContributesSubcomponent
import com.r0adkll.kimchi.restaurant.RestaurantUiViewController
import com.r0adkll.kimchi.restaurant.common.scopes.SingleIn
import com.r0adkll.kimchi.restaurant.common.scopes.UiScope
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import me.tatarka.inject.annotations.Provides
import platform.UIKit.UIViewController

@SingleIn(UiScope::class)
@ContributesSubcomponent(
  scope = UiScope::class,
  parentScope = UserScope::class,
)
interface HomeUiControllerComponent : UiComponent {
  val uiViewControllerFactory: () -> UIViewController

  @Provides
  @SingleIn(UiScope::class)
  fun uiViewController(bind: RestaurantUiViewController): UIViewController = bind()

  @ContributesSubcomponent.Factory
  interface Factory {
    fun create(): HomeUiControllerComponent
  }
}
