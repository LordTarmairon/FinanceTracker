package com.gorthaur.financetracker.navigation

import androidx.annotation.StringRes
import com.gorthaur.financetracker.R

sealed class AppDestination (
    val route: String,
    @param:StringRes val labelRes: Int
){
    data object Dashboard: AppDestination("dashboard",  R.string.nav_dashboard)
    data object ShoppingLists: AppDestination("shopping_lists", R.string.nav_shopping)
    data object Services: AppDestination("services", R.string.nav_services)
    data object Incomes: AppDestination("income", R.string.nav_incomes)
    data object Settings: AppDestination("settings", R.string.nav_settings)

}