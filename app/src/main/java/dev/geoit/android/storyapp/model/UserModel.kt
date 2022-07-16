package dev.geoit.android.storyapp.model

data class UserModel(
    val userID: String,
    val name: String,
    val email: String,
    val token: String,
    val isLogin: Boolean
)