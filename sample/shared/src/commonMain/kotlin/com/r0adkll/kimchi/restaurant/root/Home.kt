package com.r0adkll.kimchi.restaurant.root

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.gesturenavigation.GestureNavigationDecoration

@Composable
internal fun Home(
  backstack: SaveableBackStack,
  navigator: Navigator,
  windowInsets: WindowInsets,
  modifier: Modifier = Modifier,
) {
  val rootScreen by remember(backstack) {
    derivedStateOf { backstack.last().screen }
  }

  val navigationItems = remember { buildNavigationItems() }

  Scaffold(
    bottomBar = {
      HomeNavigationBar(
        selectedNavigation = rootScreen,
        navigationItems = navigationItems,
        onNavigationSelected = { navigator.resetRoot(it) },
        modifier = Modifier.fillMaxWidth(),
      )
    },
    contentWindowInsets = windowInsets,
    modifier = modifier,
  ) { paddingValues ->
    NavigableCircuitContent(
      navigator = navigator,
      backStack = backstack,
      decoration = GestureNavigationDecoration(
        onBackInvoked = navigator::pop,
      ),
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxHeight(),
    )
  }
}

@Composable
private fun HomeNavigationBar(
  selectedNavigation: Screen,
  navigationItems: List<HomeNavigationItem>,
  onNavigationSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier,
) {
  NavigationBar(
    modifier = modifier,
    windowInsets = WindowInsets.navigationBars,
  ) {
    for (item in navigationItems) {
      NavigationBarItem(
        icon = {
          HomeNavigationItemIcon(
            item = item,
            selected = selectedNavigation == item.screen,
          )
        },
        label = { Text(text = item.label) },
        selected = selectedNavigation == item.screen,
        onClick = { onNavigationSelected(item.screen) },
      )
    }
  }
}

@Composable
private fun HomeNavigationItemIcon(item: HomeNavigationItem, selected: Boolean) {
  if (item.selectedImageVector != null) {
    Crossfade(targetState = selected) { s ->
      Icon(
        imageVector = if (s) item.selectedImageVector else item.iconImageVector,
        contentDescription = item.contentDescription,
      )
    }
  } else {
    Icon(
      imageVector = item.iconImageVector,
      contentDescription = item.contentDescription,
    )
  }
}

@Immutable
private data class HomeNavigationItem(
  val screen: Screen,
  val label: String,
  val contentDescription: String,
  val iconImageVector: ImageVector,
  val selectedImageVector: ImageVector? = null,
)

private fun buildNavigationItems(): List<HomeNavigationItem> {
  return listOf(
//    HomeNavigationItem(
//      screen = DecksScreen(),
//      label = strings.decks,
//      contentDescription = strings.decksTabContentDescription,
//      iconImageVector = DeckBoxIcons.Outline.Decks,
//      selectedImageVector = DeckBoxIcons.Filled.Decks,
//    ),
//    HomeNavigationItem(
//      screen = BoosterPackScreen(),
//      label = strings.boosterPacks,
//      contentDescription = strings.boosterPacksTabContentDescription,
//      iconImageVector = Icons.Outlined.BoosterPack,
//      selectedImageVector = Icons.Filled.BoosterPack,
//    ),
//    HomeNavigationItem(
//      screen = ExpansionsScreen(),
//      label = strings.expansions,
//      contentDescription = strings.expansionsTabContentDescription,
//      iconImageVector = DeckBoxIcons.Outline.Collection,
//      selectedImageVector = DeckBoxIcons.Filled.Collection,
//    ),
//    HomeNavigationItem(
//      screen = BrowseScreen(),
//      label = strings.browse,
//      contentDescription = strings.browseTabContentDescription,
//      iconImageVector = DeckBoxIcons.Outline.Browse,
//      selectedImageVector = DeckBoxIcons.Filled.Browse,
//    ),
  )
}
