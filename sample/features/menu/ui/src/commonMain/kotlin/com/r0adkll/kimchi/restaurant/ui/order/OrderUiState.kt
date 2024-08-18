// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.ui.order

import com.r0adkll.kimchi.restaurant.ui.checkout.CheckoutUiEvent
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

data class OrderUiState(
  val eventSink: (CheckoutUiEvent) -> Unit,
) : CircuitUiState

sealed interface OrderUiEvent : CircuitUiEvent
