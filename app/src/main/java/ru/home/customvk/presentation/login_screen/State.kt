package ru.home.customvk.presentation.login_screen

data class State(
    val isStartedLoginState: Boolean = false,
    val isSuccessfulLoginState: Boolean = false
) {
    fun reduce(action: Action): State {
        return when (action) {
            Action.StartLogin -> copy(
                isStartedLoginState = true,
                isSuccessfulLoginState = false
            )
            Action.SuccessLogin -> copy(
                isStartedLoginState = false,
                isSuccessfulLoginState = true
            )
        }
    }
}