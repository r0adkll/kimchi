package com.r0adkll.kimchi.restaurant.menu.drinks

import com.r0adkll.kimchi.annotations.ContributesMultibinding
import com.r0adkll.kimchi.restaurant.common.qualifiers.Named
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.menu.model.MenuItem
import com.r0adkll.kimchi.restaurant.menu.model.MenuSection
import me.tatarka.inject.annotations.Inject

@Named("beverages")
@ContributesMultibinding(UserScope::class)
@Inject
class AlcoholicDrinks : MenuSection {
  override val name: String = "alcoholic"
  override val items: List<MenuItem> = listOf(
    MenuItem("Soju", "", "$6.99", ""),
    MenuItem("Kirin", "", "$4.99", ""),
    MenuItem("Bud-light", "", "$3.99", ""),
  )
}
