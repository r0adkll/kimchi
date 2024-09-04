// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import com.r0adkll.kimchi.restaurant.android.di.ActivityComponent
import com.r0adkll.kimchi.restaurant.common.ComponentHolder
import com.r0adkll.kimchi.restaurant.common.screens.MenuScreen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val activityComponent = ComponentHolder.component<ActivityComponent.Factory>()
      .create(this)

    ComponentHolder.components += activityComponent

    setContent {
      val backstack = rememberSaveableBackStack(listOf(MenuScreen))
      val navigator = rememberCircuitNavigator(backstack)

      activityComponent.restaurantContent(
        backstack,
        navigator,
        Modifier,
      )
    }
  }
}
