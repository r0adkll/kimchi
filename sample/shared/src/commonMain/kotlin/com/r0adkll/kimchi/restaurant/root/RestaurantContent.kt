// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.root

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.r0adkll.kimchi.restaurant.theme.RestaurantTheme
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.retained.LocalRetainedStateRegistry
import com.slack.circuit.retained.continuityRetainedStateRegistry
import com.slack.circuit.runtime.Navigator
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias RestaurantContentWithInsets = @Composable (
  backstack: SaveableBackStack,
  navigator: Navigator,
  windowInsets: WindowInsets,
  modifier: Modifier,
) -> Unit

@Inject
@Composable
fun RestaurantContentWithInsets(
  @Assisted backstack: SaveableBackStack,
  @Assisted navigator: Navigator,
  @Assisted windowInsets: WindowInsets,
  circuit: Circuit,
  @Assisted modifier: Modifier = Modifier,
) {
  CompositionLocalProvider(
    LocalRetainedStateRegistry provides continuityRetainedStateRegistry(),
  ) {
    CircuitCompositionLocals(circuit) {
      RestaurantTheme {
        Home(
          backstack = backstack,
          navigator = navigator,
          windowInsets = windowInsets,
          modifier = modifier,
        )
      }
    }
  }
}

typealias RestaurantContent = @Composable (
  backstack: SaveableBackStack,
  navigator: Navigator,
  modifier: Modifier,
) -> Unit

@Inject
@Composable
fun RestaurantContent(
  @Assisted backstack: SaveableBackStack,
  @Assisted navigator: Navigator,
  circuit: Circuit,
  @Assisted modifier: Modifier = Modifier,
) {
  RestaurantContentWithInsets(
    backstack = backstack,
    navigator = navigator,
    circuit = circuit,
    windowInsets = WindowInsets.systemBars.exclude(WindowInsets.statusBars),
    modifier = modifier,
  )
}
