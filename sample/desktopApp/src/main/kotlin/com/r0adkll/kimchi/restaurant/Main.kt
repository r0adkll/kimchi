// Copyright (C) 2023 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.r0adkll.kimchi.restaurant.common.ComponentHolder
import com.r0adkll.kimchi.restaurant.common.screens.MenuScreen
import com.r0adkll.kimchi.restaurant.common.session.UserSession
import com.r0adkll.kimchi.restaurant.di.UserComponent
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import kimchi.merge.com.r0adkll.kimchi.restaurant.createMergedDesktopApplicationComponent

@Suppress("CAST_NEVER_SUCCEEDS", "UNCHECKED_CAST", "USELESS_CAST", "KotlinRedundantDiagnosticSuppress")
fun main() = application {
  val applicationComponent = remember {
    DesktopApplicationComponent.createMergedDesktopApplicationComponent()
      .also {
        ComponentHolder.components += it
      }
  }

  val userComponent = remember {
    ComponentHolder.component<UserComponent.Factory>()
      .create(UserSession.LoggedIn)
      .also { ComponentHolder.components += it }
  }

  val backstack = rememberSaveableBackStack(listOf(MenuScreen))
  val navigator = rememberCircuitNavigator(backstack) { /* no-op */ }

  val windowState = rememberWindowState(
    width = 1080.dp,
    height = 720.dp,
    position = WindowPosition.Aligned(Alignment.Center),
  )
  Window(
    title = "Kimchi Restaurant",
    onCloseRequest = ::exitApplication,
    state = windowState,
    onKeyEvent = {
      if ((it.isCtrlPressed && it.key == Key.D) || it.key == Key.Escape) {
        navigator.pop()
        true
      } else {
        false
      }
    },
  ) {
    val component: WindowComponent = remember(applicationComponent) {
      ComponentHolder.component<WindowComponent.Factory>().create()
    }

    component.restaurantContent(
      backstack,
      navigator,
      WindowInsets(
        top = 24.dp,
        bottom = 24.dp,
      ),
      Modifier,
    )
  }
}
