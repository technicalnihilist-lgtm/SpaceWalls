package com.spacewalls.app.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spacewalls.app.ui.category.CategoryScreen
import com.spacewalls.app.ui.credits.CreditsScreen
import com.spacewalls.app.ui.detail.DetailScreen
import com.spacewalls.app.ui.explore.ExploreScreen
import com.spacewalls.app.ui.favorites.FavoritesScreen
import com.spacewalls.app.ui.home.HomeScreen
import com.spacewalls.app.ui.search.SearchScreen
import com.spacewalls.app.ui.theme.SpaceColors
import com.spacewalls.app.ui.theme.SpaceIcons

object Routes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val FAVORITES = "favorites"
    const val CREDITS = "credits"
    const val SEARCH = "search"
    const val CATEGORY = "category/{categoryId}"
    const val DETAIL = "detail/{wallpaperId}"

    fun category(id: String) = "category/$id"
    fun detail(id: String) = "detail/$id"
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val destinations = listOf(
    TopLevelDestination(Routes.HOME, "Home", Icons.Rounded.Home),
    TopLevelDestination(Routes.EXPLORE, "Explore", SpaceIcons.Explore),
    TopLevelDestination(Routes.FAVORITES, "Saved", Icons.Rounded.Favorite),
    TopLevelDestination(Routes.CREDITS, "Credits", SpaceIcons.LibraryBooks)
)

@Composable
fun SpaceWallsRoot(navController: NavHostController = rememberNavController()) {

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = destinations.any { it.route == currentRoute }

    Scaffold(
        containerColor = SpaceColors.Black,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = SpaceColors.Void,
                    tonalElevation = 0.dp
                ) {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(text = destination.label)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SpaceColors.CosmicBlue,
                                selectedTextColor = SpaceColors.CosmicBlue,
                                unselectedIconColor = SpaceColors.TextTertiary,
                                unselectedTextColor = SpaceColors.TextTertiary,
                                indicatorColor = SpaceColors.CosmicBlue.copy(alpha = 0.14f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SpaceColors.Black)
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onOpenWallpaper = { id -> navController.navigate(Routes.detail(id)) },
                        onOpenCategory = { id -> navController.navigate(Routes.category(id)) },
                        onOpenSearch = { navController.navigate(Routes.SEARCH) },
                        onOpenExplore = { navController.navigate(Routes.EXPLORE) }
                    )
                }
                composable(Routes.EXPLORE) {
                    ExploreScreen(
                        onOpenCategory = { id -> navController.navigate(Routes.category(id)) },
                        onOpenSearch = { navController.navigate(Routes.SEARCH) }
                    )
                }
                composable(Routes.FAVORITES) {
                    FavoritesScreen(
                        onOpenWallpaper = { id -> navController.navigate(Routes.detail(id)) }
                    )
                }
                composable(Routes.CREDITS) {
                    CreditsScreen(
                        onOpenWallpaper = { id -> navController.navigate(Routes.detail(id)) }
                    )
                }
                composable(Routes.SEARCH) {
                    SearchScreen(
                        onOpenWallpaper = { id -> navController.navigate(Routes.detail(id)) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.CATEGORY) { entry ->
                    val id = entry.arguments?.getString("categoryId").orEmpty()
                    CategoryScreen(
                        categoryId = id,
                        onOpenWallpaper = { wallpaperId ->
                            navController.navigate(Routes.detail(wallpaperId))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.DETAIL) { entry ->
                    val id = entry.arguments?.getString("wallpaperId").orEmpty()
                    DetailScreen(
                        wallpaperId = id,
                        onBack = { navController.popBackStack() },
                        onOpenCategory = { categoryId ->
                            navController.navigate(Routes.category(categoryId))
                        }
                    )
                }
            }
        }
    }
}
