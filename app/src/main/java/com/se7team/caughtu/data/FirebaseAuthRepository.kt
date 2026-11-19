package com.se7team.caughtu.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** The only class that talks to FirebaseAuth. */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = Firebase.auth,
) : AuthRepository {

    override val currentUserEmail: String?
        get() = auth.currentUser?.email

    override val isLoggedInNow: Boolean
        get() = auth.currentUser != null

    override val isLoggedIn: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser != null) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun register(email: String, password: String): Result<Unit> = call {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun login(email: String, password: String): Result<Unit> = call {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override fun logout() = auth.signOut()

    private inline fun call(block: () -> Unit): Result<Unit> =
        runCatching(block).recoverCatching { throw DataException(friendlyAuthMessage(it), it) }
}
