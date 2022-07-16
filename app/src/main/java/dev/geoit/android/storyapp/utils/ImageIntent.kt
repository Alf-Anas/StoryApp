package dev.geoit.android.storyapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import dev.geoit.android.storyapp.R

class ImageIntent(private val activity: Activity) {

    companion object {
        const val RC_ACTION_IMAGE_CAPTURE = 1021
        const val RC_ACTION_IMAGE_PICK = 1022
    }

    var response = MutableLiveData<Int>()

    fun initImageIntent() {
        val builder = AlertDialog.Builder(activity)
        val view = activity.layoutInflater.inflate(R.layout.popup_image_intent, null)
        val llCamera = view.findViewById<LinearLayout>(R.id.imageIntentLLCamera)
        val llGallery = view.findViewById<LinearLayout>(R.id.imageIntentLLGallery)
        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        llCamera.setOnClickListener {
            response.postValue(RC_ACTION_IMAGE_CAPTURE)
            dialog.dismiss()
        }
        llGallery.setOnClickListener {
            response.postValue(RC_ACTION_IMAGE_PICK)
            dialog.dismiss()
        }
        dialog.show()
    }

}