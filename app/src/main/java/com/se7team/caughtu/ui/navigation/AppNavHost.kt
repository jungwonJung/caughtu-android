package com.se7team.caughtu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.se7team.caughtu.AppContainer
import com.se7team.caughtu.ui.auth.AuthViewModel
import com.se7team.caughtu.ui.auth.LoginScreen
import com.se7team.caughtu.ui.auth.RegisterScreen
import com.se7team.caughtu.ui.snapshots.SnapshotListScreen
import com.se7team.caughtu.ui.snapshots.SnapshotListViewModel
import androidx.compose.material3.Text

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val LIST = "snapshots"
    const val EDIT = "edit?id={id}"
    fun edit(id: String? = null) = if (id == null) "edit" else "edit?id=$id"
}

@Composable
fun AppNavHost(session: SessionViewModel = viewModel { SessionViewModel(AppContainer.authRepository) }) {
    val navController = rememberNavController()
    val isLoggedIn by session.isLoggedIn.collectAsStateWithLifecycle()
    // Auto-login: decided once, from the cached session.
    val startDestination = remember { if (isLoggedIn) Routes.LIST else Routes.LOGIN }

    // Navigation reacts to auth state; screens never navigate after login/logout themselves.
    LaunchedEffect(isLoggedIn) {
        val current = navController.currentDestination?.route
        val onAuthScreen = current == Routes.LOGIN || current == Routes.REGISTER
        if (isLoggedIn && onAuthScreen) navController.navigateClearingBackStack(Routes.LIST)
        if (!isLoggedIn && !onAuthScreen && current != null) navController.navigateClearingBackStack(Routes.LOGIN)
    }

    NavHost(navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            val vm = viewModel { AuthViewModel(AppContainer.authRepository) }
            LoginScreen(vm, onRegisterClick = { navController.navigate(Routes.REGISTER) })
        }
        composable(Routes.REGISTER) {
            val vm = viewModel { AuthViewModel(AppContainer.authRepository) }
            RegisterScreen(vm, onLoginClick = { navController.popBackStack() })
        }
        composable(Routes.LIST) {
            val vm = viewModel { SnapshotListViewModel(AppContainer.snapshotRepository, AppContainer.authRepository) }
            SnapshotListScreen(
                vm,
                onAdd = { navController.navigate(Routes.edit()) },
                onOpen = { id -> navController.navigate(Routes.edit(id)) },
            )
        }
        composable(
            Routes.EDIT,
            arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null }),
        ) { Text("Form – see #16") }
    }
}

private fun NavHostController.navigateClearingBackStack(route: String) = navigate(route) {
    popUpTo(graph.id) { inclusive = true }
    launchSingleTop = true
}
