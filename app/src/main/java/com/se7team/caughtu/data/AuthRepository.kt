package com.se7team.caughtu.data

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserEmail: String?
    val isLoggedIn: Flow<Boolean>
    val isLoggedInNow: Boolean
    /** Failures are [DataException]s with a user-readable message. */
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    fun logout()
}
