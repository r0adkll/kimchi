// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.util

import com.squareup.kotlinpoet.ClassName

object ClassNames {

  val Composable = ClassName("androidx.compose.runtime", "Composable")
  val Modifier = ClassName("androidx.compose.ui", "Modifier")

  object Circuit {
    val Ui = ClassName("com.slack.circuit.runtime.ui", "Ui")
    val UiFactory = ClassName("com.slack.circuit.runtime.ui", "Ui", "Factory")
    val UiState = ClassName("com.slack.circuit.runtime", "CircuitUiState")
    val Presenter = ClassName("com.slack.circuit.runtime.presenter", "Presenter")
    val PresenterFactory = ClassName("com.slack.circuit.runtime.presenter", "Presenter", "Factory")
    val Context = ClassName("com.slack.circuit.runtime", "CircuitContext")
    val Screen = ClassName("com.slack.circuit.runtime.screen", "Screen")
    val StaticScreen = ClassName("com.slack.circuit.runtime.screen", "StaticScreen")
    val Navigator = ClassName("com.slack.circuit.runtime", "Navigator")
  }
}
