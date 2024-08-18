package com.r0adkll.kimchi.restaurant.ui.checkout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r0adkll.kimchi.circuit.annotations.CircuitInject
import com.r0adkll.kimchi.restaurant.common.scopes.UiScope
import com.r0adkll.kimchi.restaurant.common.screens.CheckoutScreen
import com.r0adkll.kimchi.restaurant.common.screens.OrderScreen
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.Inject

@CircuitInject(CheckoutScreen::class, UiScope::class)
@Inject
class CheckoutUi : Ui<CheckoutUiState> {

  @Composable
  override fun Content(state: CheckoutUiState, modifier: Modifier) {
    TODO("Not yet implemented")
  }
}
