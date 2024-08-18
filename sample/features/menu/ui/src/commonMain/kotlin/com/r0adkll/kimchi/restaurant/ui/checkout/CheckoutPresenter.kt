package com.r0adkll.kimchi.restaurant.ui.checkout

import androidx.compose.runtime.Composable
import com.r0adkll.kimchi.circuit.annotations.CircuitInject
import com.r0adkll.kimchi.restaurant.common.scopes.UiScope
import com.r0adkll.kimchi.restaurant.common.screens.CheckoutScreen
import com.slack.circuit.runtime.presenter.Presenter
import me.tatarka.inject.annotations.Inject

@CircuitInject(CheckoutScreen::class, UiScope::class)
@Inject
class CheckoutPresenter : Presenter<CheckoutUiState> {

  @Composable
  override fun present(): CheckoutUiState {
    return CheckoutUiState {  }
  }
}
