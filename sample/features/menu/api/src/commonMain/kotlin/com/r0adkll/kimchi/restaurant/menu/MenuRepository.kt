package com.r0adkll.kimchi.restaurant.menu

import com.r0adkll.kimchi.restaurant.menu.model.MenuItem

interface MenuRepository {

  suspend fun getItems(): List<MenuItem>
}
