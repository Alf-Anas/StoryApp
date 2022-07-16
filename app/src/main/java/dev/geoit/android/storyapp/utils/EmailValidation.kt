package dev.geoit.android.storyapp.utils

import android.util.Patterns

fun isValidEmail(target: CharSequence?): Boolean {
    return target != null && Patterns.EMAIL_ADDRESS.matcher(target).matches()
}