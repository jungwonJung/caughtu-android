package com.se7team.caughtu.domain

/** Pure Kotlin (no Android classes) so it can be unit-tested on the JVM. */
object AuthValidator {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    const val MIN_PASSWORD = 6 // Firebase's own minimum

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !EMAIL.matches(email.trim()) -> "Enter a valid email address"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < MIN_PASSWORD -> "Password must be at least $MIN_PASSWORD characters"
        else -> null
    }

    fun validatePasswordConfirmation(password: String, confirmation: String): String? =
        if (password != confirmation) "Passwords do not match" else null
}
