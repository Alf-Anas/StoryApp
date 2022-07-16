package dev.geoit.android.storyapp.view

import android.app.Activity
import android.app.AlertDialog
import dev.geoit.android.storyapp.R

class LoadingDialog {
    fun initLoadingDialog(activity: Activity): AlertDialog {
        val dialog: AlertDialog
        val builder = AlertDialog.Builder(activity)
        val loadingView = activity.layoutInflater.inflate(R.layout.popup_loading_circle, null)
        builder.setView(loadingView)
        dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }
}