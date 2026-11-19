package com.se7team.caughtu

import com.se7team.caughtu.data.AuthRepository
import com.se7team.caughtu.data.FirebaseAuthRepository
import com.se7team.caughtu.data.FirestoreSnapshotRepository
import com.se7team.caughtu.data.SnapshotRepository

/** Manual dependency injection: the one place that picks the real implementations. Tests pass fakes instead. */
object AppContainer {
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }
    val snapshotRepository: SnapshotRepository by lazy { FirestoreSnapshotRepository() }
}
