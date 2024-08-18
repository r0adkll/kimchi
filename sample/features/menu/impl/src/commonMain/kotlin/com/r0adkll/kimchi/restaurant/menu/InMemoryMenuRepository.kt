// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.menu

import com.r0adkll.kimchi.annotations.ContributesBinding
import com.r0adkll.kimchi.restaurant.common.qualifiers.Named
import com.r0adkll.kimchi.restaurant.common.scopes.SingleIn
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.menu.model.Menu
import com.r0adkll.kimchi.restaurant.menu.model.MenuItem
import com.r0adkll.kimchi.restaurant.menu.model.MenuSection
import me.tatarka.inject.annotations.Inject

@SingleIn(UserScope::class)
@ContributesBinding(UserScope::class)
@Inject
class InMemoryMenuRepository(
  @Named("appetizer") val appetizer: MenuSection,
  @Named("entree") val entrees: MenuSection,
  @Named("dessert") val dessert: MenuSection,
  @Named("dujour") val duJours: Map<String, MenuSection>,
  @Named("beverages") val beverages: Set<MenuSection>,
) : MenuRepository {

  override suspend fun getMenu(): Menu {
    return Menu(
      sections = listOf(
        appetizer + duJours["appetizer"],
        entrees + duJours["entree"],
        dessert + duJours["dessert"],
        *beverages.toTypedArray(),
      ),
    )
  }

  operator fun MenuSection.plus(other: MenuSection?): MenuSection {
    if (other == null) return this
    val name = this.name
    val combinedItems = items + other.items
    return object : MenuSection {
      override val name: String = name
      override val items: List<MenuItem> = combinedItems
    }
  }
}
