package ru.home.customvk.presentation.login_screen

sealed class Action {

    object StartLogin : Action()

    object SuccessLogin : Action()

}