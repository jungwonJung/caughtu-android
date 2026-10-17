package com.se7team.caughtu.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(viewModel: AuthViewModel, onRegisterClick: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("CaughtU", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("Log in to see your snapshots", color = MaterialTheme.colorScheme.onSurfaceVariant)
        AuthTextField(state.email, viewModel::onEmailChange, "Email", state.emailError, KeyboardType.Email)
        AuthTextField(state.password, viewModel::onPasswordChange, "Password", state.passwordError, KeyboardType.Password, isPassword = true)
        state.generalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LoadingButton("Log in", state.isLoading, viewModel::login)
        TextButton(onClick = onRegisterClick) { Text("No account? Register") }
    }
}
