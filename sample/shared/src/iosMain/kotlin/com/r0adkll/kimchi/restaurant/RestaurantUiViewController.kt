// Copyright (C) 2020 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.r0adkll.kimchi.restaurant.common.screens.MenuScreen
import com.r0adkll.kimchi.restaurant.root.RestaurantContent
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import me.tatarka.inject.annotations.Inject
import platform.UIKit.UIViewController

typealias RestaurantUiViewController = () -> UIViewController

@Inject
fun RestaurantUiViewController(
  restaurantContent: RestaurantContent,
): UIViewController = ComposeUIViewController {
  val backstack = rememberSaveableBackStack(listOf(MenuScreen))
  val navigator = rememberCircuitNavigator(backstack, onRootPop = { /* no-op */ })

  val uiViewController = LocalUIViewController.current

  // Increase the touch slop. The default value of 3.dp is a bit low imo, so we use the
  // Android default value of 8.dp
  // https://github.com/JetBrains/compose-multiplatform/issues/3397
  val vc = LocalViewConfiguration.current.withTouchSlop(
    with(LocalDensity.current) { 8.dp.toPx() },
  )

  LocalWindowInfo
  CompositionLocalProvider(LocalViewConfiguration provides vc) {
    restaurantContent(
      backstack,
      navigator,
      Modifier,
    )
  }
}

private fun ViewConfiguration.withTouchSlop(
  touchSlop: Float,
): ViewConfiguration = object : ViewConfiguration by this {
  override val touchSlop: Float = touchSlop
}
