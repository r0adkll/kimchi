// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.menu.section

import com.r0adkll.kimchi.annotations.ContributesBinding
import com.r0adkll.kimchi.restaurant.common.qualifiers.Named
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.menu.model.MenuItem
import com.r0adkll.kimchi.restaurant.menu.model.MenuSection

@Named("appetizer")
@ContributesBinding(UserScope::class)
object AppetizerSection : MenuSection {

  override val name: String = "Appetizers"

  override val items: List<MenuItem> = listOf(
    MenuItem("Tteokbokki", "Korean Rice Cakes", "$8.99", ""),
    MenuItem("Gyoza", "Pan Fried Dumplings", "$9.99", ""),
  )
}
