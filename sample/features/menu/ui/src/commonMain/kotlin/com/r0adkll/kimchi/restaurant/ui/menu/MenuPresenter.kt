// Copyright (C) 2024 r0adkll
// SPDX-License-Identifier: Apache-2.0
package com.r0adkll.kimchi.restaurant.ui.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.r0adkll.kimchi.circuit.annotations.CircuitInject
import com.r0adkll.kimchi.restaurant.common.scopes.UiScope
import com.r0adkll.kimchi.restaurant.common.screens.MenuScreen
import com.r0adkll.kimchi.restaurant.menu.MenuRepository
import com.r0adkll.kimchi.restaurant.menu.model.Menu
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.flow.flow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@CircuitInject(MenuScreen::class, UiScope::class)
@Inject
class MenuPresenter(
  @Assisted private val navigator: Navigator,
  private val menuRepository: MenuRepository,
) : Presenter<MenuUiState> {

  @Composable
  override fun present(): MenuUiState {
    val menu by remember {
      flow { emit(menuRepository.getMenu()) }
    }.collectAsState(Menu(emptyList()))

    return MenuUiState(
      menu = menu,
    ) {
    }
  }
}
