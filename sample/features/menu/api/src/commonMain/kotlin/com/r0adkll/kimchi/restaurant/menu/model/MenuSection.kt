// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.menu.model

interface MenuSection {
  val name: String
  val items: List<MenuItem>
}
