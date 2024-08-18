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
class NonAlcoholicDrinks : MenuSection {
  override val name: String = "non-alcoholic"
  override val items: List<MenuItem> = listOf(
    MenuItem("Coke-cola", "", "$2.99", ""),
    MenuItem("Tea", "", "$1.99", ""),
    MenuItem("Coffee", "", "$1.99", ""),
  )
}
