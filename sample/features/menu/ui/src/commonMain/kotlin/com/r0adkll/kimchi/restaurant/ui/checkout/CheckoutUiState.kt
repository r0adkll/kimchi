// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.ui.checkout

import com.r0adkll.kimchi.restaurant.ui.order.OrderUiEvent
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

data class CheckoutUiState(
  val eventSink: (OrderUiEvent) -> Unit,
) : CircuitUiState

sealed interface CheckoutUiEvent : CircuitUiEvent
