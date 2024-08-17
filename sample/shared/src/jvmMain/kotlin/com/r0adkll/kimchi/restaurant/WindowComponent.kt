// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package com.r0adkll.kimchi.restaurant

import com.r0adkll.kimchi.annotations.ContributesSubcomponent
import com.r0adkll.kimchi.restaurant.common.scopes.SingleIn
import com.r0adkll.kimchi.restaurant.common.scopes.UiScope
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.di.UiComponent
import com.r0adkll.kimchi.restaurant.root.RestaurantContentWithInsets

@SingleIn(UiScope::class)
@ContributesSubcomponent(
  scope = UiScope::class,
  parentScope = UserScope::class,
)
interface WindowComponent : UiComponent {
  val restaurantContent: RestaurantContentWithInsets

  @ContributesSubcomponent.Factory
  interface Factory {
    fun create(): WindowComponent
  }
}
