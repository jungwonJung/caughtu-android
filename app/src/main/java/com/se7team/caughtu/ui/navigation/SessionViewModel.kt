package com.se7team.caughtu.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.se7team.caughtu.data.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** App-level session state. Navigation reacts to it; screens never decide where to go after login/logout. */
class SessionViewModel(private val auth: AuthRepository) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = auth.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, auth.isLoggedInNow)

    val currentUserEmail: String? get() = auth.currentUserEmail

    fun logout() = auth.logout()
}
