package com.se7team.caughtu.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code

/** Errors leaving the data layer always carry a message that is safe to show to the user. */
class DataException(message: String, cause: Throwable? = null) : Exception(message, cause)

internal fun friendlyAuthMessage(e: Throwable): String = when (e) {
    is FirebaseAuthUserCollisionException -> "An account with this email already exists"
    is FirebaseAuthWeakPasswordException -> "Password is too weak"
    is FirebaseAuthInvalidUserException,
    is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password"
    is FirebaseNetworkException -> "No internet connection. Please try again"
    is FirebaseTooManyRequestsException -> "Too many attempts. Please wait a moment"
    else -> "Something went wrong. Please try again"
}

internal fun friendlyDataMessage(e: Throwable): String = when ((e as? FirebaseFirestoreException)?.code) {
    Code.PERMISSION_DENIED -> "You don't have access to this data"
    Code.UNAVAILABLE -> "No internet connection. Please try again"
    Code.NOT_FOUND -> "This snapshot no longer exists"
    else -> "Something went wrong. Please try again"
}
