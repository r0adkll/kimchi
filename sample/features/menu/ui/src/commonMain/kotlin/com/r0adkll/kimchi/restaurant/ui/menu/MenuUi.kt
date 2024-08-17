package com.r0adkll.kimchi.restaurant.ui.menu

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r0adkll.kimchi.circuit.annotations.CircuitInject
import com.r0adkll.kimchi.restaurant.common.scopes.UiScope
import com.r0adkll.kimchi.restaurant.common.screens.MenuScreen

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(UiScope::class, MenuScreen::class)
@Composable
fun MenuUi(
  state: MenuUiState,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = { Text("Kimchi Restaurant") },
      )
    },
    modifier = modifier,
  ) {
    LazyColumn {
      items(state.items) { menuItem ->
        ListItem(
          headlineContent = { Text(menuItem.title) },
          supportingContent = { Text(menuItem.description) },
          trailingContent = { Text(menuItem.price) },
        )
      }
    }
  }
}
