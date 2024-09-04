// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.ui.order

import androidx.compose.runtime.Composable
import com.r0adkll.kimchi.circuit.annotations.CircuitInject
import com.r0adkll.kimchi.restaurant.common.scopes.UiScope
import com.r0adkll.kimchi.restaurant.common.screens.OrderScreen
import com.r0adkll.kimchi.restaurant.ui.checkout.CheckoutUiState

@CircuitInject(OrderScreen::class, UiScope::class)
@Composable
fun orderPresenter(): CheckoutUiState {
  return CheckoutUiState {
  }
}
