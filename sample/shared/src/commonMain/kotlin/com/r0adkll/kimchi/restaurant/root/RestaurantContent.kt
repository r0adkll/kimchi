// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.r0adkll.kimchi.restaurant.theme.RestaurantTheme
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.retained.LocalRetainedStateRegistry
import com.slack.circuit.retained.lifecycleRetainedStateRegistry
import com.slack.circuit.runtime.Navigator
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias RestaurantContent = @Composable (
  backstack: SaveableBackStack,
  navigator: Navigator,
  modifier: Modifier,
) -> Unit

@Inject
@Composable
fun RestaurantContent(
  circuit: Circuit,
  @Assisted backstack: SaveableBackStack,
  @Assisted navigator: Navigator,
  @Assisted modifier: Modifier = Modifier,
) {
  CompositionLocalProvider(
    LocalRetainedStateRegistry provides lifecycleRetainedStateRegistry(),
  ) {
    CircuitCompositionLocals(circuit) {
      RestaurantTheme {
        Home(
          backstack = backstack,
          navigator = navigator,
          modifier = modifier,
        )
      }
    }
  }
}
