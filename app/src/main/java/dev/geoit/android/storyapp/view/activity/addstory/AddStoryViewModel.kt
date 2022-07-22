package dev.geoit.android.storyapp.view.activity.addstory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.gson.JsonElement
import dev.geoit.android.storyapp.constant.Configuration
import dev.geoit.android.storyapp.model.StatusModel
import dev.geoit.android.storyapp.model.UserModel
import dev.geoit.android.storyapp.model.UserPreferences
import dev.geoit.android.storyapp.retrofit.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddStoryViewModel(private val pref: UserPreferences) : ViewModel() {

    private val baseUrl = Configuration.DICODING_BASE_URL
    var statusModel = MutableLiveData<StatusModel>()

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun uploadNewStory(
        token: String,
        imageMultipart: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody? = null,
        lon: RequestBody? = null,
    ) {
        RetrofitClient(baseUrl).instanceDicoding.postNewStories(
            "Bearer $token",
            imageMultipart,
            description,
            lat, lon
        )
            .enqueue(
                object : Callback<JsonElement> {
                    override fun onResponse(
                        call: Call<JsonElement>,
                        response: Response<JsonElement>
                    ) {
                        if (response.isSuccessful) {
                            try {
                                val jsonObject = JSONObject(response.body().toString())

                                val isError = jsonObject.getBoolean("error")
                                val message = jsonObject.getString("message")
                                statusModel.postValue(StatusModel(isError, message))
                            } catch (e: JSONException) {
                                onJSONException(e)
                            }
                        } else {
                            onResponseFailed(response)
                        }
                    }

                    override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                        onFailureResponse(t)
                    }
                })
    }

    fun onJSONException(e: JSONException) {
        Log.d("Exception", e.message.toString())
        statusModel.postValue(
            StatusModel(
                true,
                "Exception - " + e.message.toString()
            )
        )
    }

    fun onResponseFailed(response: Response<JsonElement>) {
        try {
            val jsonObject = JSONObject(response.errorBody()!!.string())

            val isError = jsonObject.getBoolean("error")
            val message = jsonObject.getString("message")
            statusModel.postValue(StatusModel(isError, message))
        } catch (e: JSONException) {
            Log.d("Exception", e.message.toString())
            statusModel.postValue(
                StatusModel(
                    true,
                    "Response Not Successful - Exception - " + e.message.toString()
                )
            )
        }
    }

    fun onFailureResponse(t: Throwable) {
        Log.d("onFailure", t.message.toString())
        statusModel.postValue(
            StatusModel(
                true,
                "onFailure - " + t.message.toString()
            )
        )
    }

}