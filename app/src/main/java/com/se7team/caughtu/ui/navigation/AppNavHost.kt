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
import com.se7team.caughtu.AppContainer
import com.se7team.caughtu.ui.auth.AuthViewModel
import com.se7team.caughtu.ui.auth.LoginScreen
import com.se7team.caughtu.ui.auth.RegisterScreen
import com.se7team.caughtu.ui.home.HomeScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

@Composable
fun AppNavHost(session: SessionViewModel = viewModel { SessionViewModel(AppContainer.authRepository) }) {
    val navController = rememberNavController()
    val isLoggedIn by session.isLoggedIn.collectAsStateWithLifecycle()
    // Auto-login: decided once, from the cached session.
    val startDestination = remember { if (isLoggedIn) Routes.HOME else Routes.LOGIN }

    // Navigation reacts to auth state; screens never navigate after login/logout themselves.
    LaunchedEffect(isLoggedIn) {
        val current = navController.currentDestination?.route
        val onAuthScreen = current == Routes.LOGIN || current == Routes.REGISTER
        if (isLoggedIn && onAuthScreen) navController.navigateClearingBackStack(Routes.HOME)
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
        composable(Routes.HOME) {
            HomeScreen(session.currentUserEmail, onLogout = session::logout)
        }
    }
}

private fun NavHostController.navigateClearingBackStack(route: String) = navigate(route) {
    popUpTo(graph.id) { inclusive = true }
    launchSingleTop = true
}
