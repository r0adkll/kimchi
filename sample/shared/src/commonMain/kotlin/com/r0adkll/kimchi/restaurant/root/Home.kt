// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.root

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.runtime.Navigator
import com.slack.circuitx.gesturenavigation.GestureNavigationDecoration

@Composable
internal fun Home(
  backstack: SaveableBackStack,
  navigator: Navigator,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
  ) { paddingValues ->
    NavigableCircuitContent(
      navigator = navigator,
      backStack = backstack,
      decoration = GestureNavigationDecoration(
        onBackInvoked = navigator::pop,
      ),
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxHeight(),
    )
  }
}
