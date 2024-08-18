package com.r0adkll.kimchi.restaurant.menu.section

import com.r0adkll.kimchi.annotations.ContributesBinding
import com.r0adkll.kimchi.restaurant.common.qualifiers.Named
import com.r0adkll.kimchi.restaurant.common.scopes.UserScope
import com.r0adkll.kimchi.restaurant.menu.model.MenuItem
import com.r0adkll.kimchi.restaurant.menu.model.MenuSection
import me.tatarka.inject.annotations.Inject

@Named("entree")
@ContributesBinding(UserScope::class)
object EntreeSection : MenuSection {

  override val name: String = "Entrees"

  override val items: List<MenuItem> = listOf(
    MenuItem("Kimchi Fried Rice", "Fried Rice tossed with house made Kimchi", "$14.99", ""),
    MenuItem("Kimchi Udon", "Udon noodles in a Kimchi broth", "$16.99", ""),
  )
}
