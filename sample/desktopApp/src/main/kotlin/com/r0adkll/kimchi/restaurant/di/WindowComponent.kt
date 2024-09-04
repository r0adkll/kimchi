// Copyright (C) 2023 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.di

import com.r0adkll.kimchi.annotations.ContributesSubcomponent
import com.r0adkll.kimchi.restaurant.common.scopes.SingleIn
import com.r0adkll.kimchi.restaurant.common.scopes.UiScope
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.root.RestaurantContent

@SingleIn(UiScope::class)
@ContributesSubcomponent(
  scope = UiScope::class,
  parentScope = UserScope::class,
)
interface WindowComponent {
  val restaurantContent: RestaurantContent

  @ContributesSubcomponent.Factory
  interface Factory {
    fun create(): WindowComponent
  }
}
