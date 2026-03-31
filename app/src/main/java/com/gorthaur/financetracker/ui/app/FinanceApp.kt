package com.gorthaur.financetracker.ui.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gorthaur.financetracker.navigation.AppDestination
import com.gorthaur.financetracker.ui.screens.DashboardScreen
import com.gorthaur.financetracker.ui.screens.IncomesScreen
import com.gorthaur.financetracker.ui.screens.ServicesScreen
import com.gorthaur.financetracker.ui.screens.SettingsScreen
import com.gorthaur.financetracker.ui.screens.ShoppingListsScreen

@Composable
fun FinanceApp(appState: FinanceAppState) {
    val navController = rememberNavController()

    val destinations = listOf(
        AppDestination.Dashboard,
        AppDestination.ShoppingLists,
        AppDestination.Services,
        AppDestination.Incomes,
        AppDestination.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(AppDestination.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            when (destination) {
                                AppDestination.Dashboard -> Icon(Icons.Default.Home, contentDescription = stringResource(destination.labelRes))
                                AppDestination.ShoppingLists -> Icon(Icons.Default.ShoppingCart, contentDescription = stringResource(destination.labelRes))
                                AppDestination.Services -> Icon(Icons.Default.Receipt, contentDescription = stringResource(destination.labelRes))
                                AppDestination.Incomes -> Icon(Icons.Default.AttachMoney, contentDescription = stringResource(destination.labelRes))
                                AppDestination.Settings -> Icon(Icons.Default.Settings, contentDescription = stringResource(destination.labelRes))
                            }
                        },
                        label = {
                            Text(text = stringResource(destination.labelRes))
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.Dashboard.route) {
                DashboardScreen()
            }
            composable(AppDestination.ShoppingLists.route) {
                ShoppingListsScreen()
            }
            composable(AppDestination.Services.route) {
                ServicesScreen()
            }
            composable(AppDestination.Incomes.route) {
                IncomesScreen()
            }
            composable(AppDestination.Settings.route) {
                SettingsScreen(appState = appState)
            }
        }
    }
}