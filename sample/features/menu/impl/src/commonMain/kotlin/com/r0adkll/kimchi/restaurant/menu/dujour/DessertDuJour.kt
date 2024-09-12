// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.menu.dujour

import com.r0adkll.kimchi.annotations.ClassKey
import com.r0adkll.kimchi.annotations.ContributesMultibinding
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.menu.model.MenuItem
import com.r0adkll.kimchi.restaurant.menu.model.MenuSection

@ClassKey(DessertDuJour::class)
@ContributesMultibinding(UserScope::class)
object DessertDuJour : MenuSection {

  override val name: String = "appetizer"

  override val items: List<MenuItem> = listOf(
    MenuItem("Kimchi Ice Cream", "Ice cream with kimchi", "$2.99", ""),
  )
}
