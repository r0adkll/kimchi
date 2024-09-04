// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.menu.dujour

import com.r0adkll.kimchi.annotations.ContributesMultibinding
import com.r0adkll.kimchi.annotations.StringKey
import com.r0adkll.kimchi.restaurant.common.qualifiers.Named
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.menu.model.MenuItem
import com.r0adkll.kimchi.restaurant.menu.model.MenuSection
import me.tatarka.inject.annotations.Inject

@Named("dujour")
@StringKey("entree")
@ContributesMultibinding(UserScope::class)
@Inject
class EntreeDuJour : MenuSection {

  override val name: String = "appetizer"

  override val items: List<MenuItem> = listOf(
    MenuItem("Just Kimchi", "Literally, just kimchi", "$2.99", ""),
  )
}
