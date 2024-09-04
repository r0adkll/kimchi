// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.menu

import com.r0adkll.kimchi.annotations.ContributesBinding
import com.r0adkll.kimchi.restaurant.common.scopes.SingleIn
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.menu.model.MenuItem
import me.tatarka.inject.annotations.Inject

@SingleIn(UserScope::class)
@ContributesBinding(UserScope::class)
@Inject
class InMemoryMenuRepository : MenuRepository {

  override suspend fun getItems(): List<MenuItem> {
    return listOf(
      MenuItem("Kimchi Fried Rice", "Fried Rice + Kimchi", "$12.99", ""),
      MenuItem("Kimchi Udon Noodles", "Noodles + Kimchi", "$13.99", ""),
      MenuItem("Kimchi Pancakes", "Kimchi Pancakes", "$6.99", ""),
    )
  }
}
