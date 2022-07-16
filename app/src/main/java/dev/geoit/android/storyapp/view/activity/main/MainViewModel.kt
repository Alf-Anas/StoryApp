package dev.geoit.android.storyapp.view.activity.main

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.JsonElement
import dev.geoit.android.storyapp.constant.Configuration
import dev.geoit.android.storyapp.model.StatusModel
import dev.geoit.android.storyapp.model.StoryModel
import dev.geoit.android.storyapp.model.UserModel
import dev.geoit.android.storyapp.model.UserPreferences
import dev.geoit.android.storyapp.retrofit.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: UserPreferences) : ViewModel() {

    private val baseUrl = Configuration.DICODING_BASE_URL
    var statusModel = MutableLiveData<StatusModel>()
    var listStories = MutableLiveData<ArrayList<StoryModel>>()

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }

    fun getListStories(token: String, page: Int) {
        RetrofitClient(baseUrl).instanceDicoding.getAllStories("Bearer $token", page)
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
                                val listStory = jsonObject.getJSONArray("listStory")

                                val list = arrayListOf<StoryModel>()
                                for (i in 0 until listStory.length()) {
                                    val dataJSON = listStory.getJSONObject(i)
                                    val storyModel = StoryModel(
                                        id = dataJSON.getString("id"),
                                        name = dataJSON.getString("name"),
                                        description = dataJSON.getString("description"),
                                        photoUrl = dataJSON.getString("photoUrl"),
                                        createdAt = dataJSON.getString("createdAt"),
                                        lat = if (dataJSON.has("lat") && !dataJSON.isNull("lat")
                                        ) dataJSON.getDouble("lat") else null,
                                        lon = if (dataJSON.has("lon") && !dataJSON.isNull("lon")
                                        ) dataJSON.getDouble("lon") else null,
                                    )
                                    list.add(storyModel)
                                }
                                listStories.postValue(list)
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