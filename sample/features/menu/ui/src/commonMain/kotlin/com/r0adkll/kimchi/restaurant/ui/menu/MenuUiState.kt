// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.ui.menu

import com.r0adkll.kimchi.restaurant.menu.model.Menu
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

data class MenuUiState(
  val menu: Menu,
  val eventSink: (MenuUiEvent) -> Unit,
) : CircuitUiState

sealed interface MenuUiEvent : CircuitUiEvent
