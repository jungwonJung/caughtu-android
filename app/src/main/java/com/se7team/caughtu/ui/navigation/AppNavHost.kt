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
fun AppNavHost(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    // Auto-login: decided once, from the cached Firebase user.
    val startDestination = remember { if (isLoggedIn) Routes.HOME else Routes.LOGIN }

    // Navigation reacts to auth state; screens never navigate after login/logout themselves.
    LaunchedEffect(isLoggedIn) {
        val target = if (isLoggedIn) Routes.HOME else Routes.LOGIN
        val current = navController.currentDestination?.route
        if (isLoggedIn && current == Routes.HOME) return@LaunchedEffect
        if (!isLoggedIn && current != Routes.HOME) return@LaunchedEffect
        navController.navigateClearingBackStack(target)
    }

    NavHost(navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(authViewModel, onRegisterClick = {
                authViewModel.resetForm()
                navController.navigate(Routes.REGISTER)
            })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(authViewModel, onLoginClick = {
                authViewModel.resetForm()
                navController.popBackStack()
            })
        }
        composable(Routes.HOME) {
            HomeScreen(authViewModel.currentUserEmail, onLogout = authViewModel::logout)
        }
    }
}

private fun NavHostController.navigateClearingBackStack(route: String) = navigate(route) {
    popUpTo(graph.id) { inclusive = true }
    launchSingleTop = true
}
