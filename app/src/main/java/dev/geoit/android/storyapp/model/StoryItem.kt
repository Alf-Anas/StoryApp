package dev.geoit.android.storyapp.model

import com.google.gson.annotations.SerializedName

data class StoryItem(
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("name")
    val name: String = "",

    @field:SerializedName("description")
    val description: String = "",

    @field:SerializedName("photoUrl")
    val photoUrl: String = "",

    @field:SerializedName("createdAt")
    val createdAt: String = "",

    @field:SerializedName("lat")
    val lat: Double? = null,

    @field:SerializedName("lon")
    val lon: Double? = null
)
