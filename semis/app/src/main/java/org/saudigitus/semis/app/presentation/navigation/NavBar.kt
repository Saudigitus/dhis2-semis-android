package org.saudigitus.semis.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem

private val navItems = listOf(
    NavigationBarItem(
        id = HomeBarNavigation.HOME.ordinal,
        icon = Icons.Default.Dashboard,
        label = "Home",
    ),
    NavigationBarItem(
        id = HomeBarNavigation.ANALYTICS.ordinal,
        icon = Icons.Default.BarChart,
        label = "Charts",
    ),
)

@Composable
fun NavBar(
    modifier: Modifier = Modifier,
    destination: Int = HomeBarNavigation.HOME.ordinal,
    onItemClick: (Int) -> Unit = {},
) {
    NavigationBar(
        modifier = modifier,
        items = navItems,
        selectedItemIndex = destination,
    ) {
        onItemClick(it)
    }
}