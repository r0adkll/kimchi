package com.r0adkll.kimchi.restaurant.menu.section

import com.r0adkll.kimchi.annotations.ContributesBinding
import com.r0adkll.kimchi.restaurant.common.qualifiers.Named
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.menu.model.MenuItem
import com.r0adkll.kimchi.restaurant.menu.model.MenuSection
import me.tatarka.inject.annotations.Inject

@Named("dessert")
@ContributesBinding(UserScope::class)
@Inject
class DessertSection : MenuSection {

  override val name: String = "Dessert"

  override val items: List<MenuItem> = listOf(
    MenuItem("Mochi Ice Cream", "Ice cream mochi balls", "$12.99", ""),
    MenuItem("Kimchi Cheesecake", "Cheesecake made from the essence of kimchi", "$6.99", ""),
  )
}
