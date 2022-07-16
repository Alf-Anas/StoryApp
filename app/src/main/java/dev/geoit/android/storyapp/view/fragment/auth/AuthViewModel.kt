package dev.geoit.android.storyapp.view.fragment.auth

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonElement
import dev.geoit.android.storyapp.constant.Configuration
import dev.geoit.android.storyapp.model.StatusModel
import dev.geoit.android.storyapp.model.UserModel
import dev.geoit.android.storyapp.model.UserPreferences
import dev.geoit.android.storyapp.retrofit.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel(private val pref: UserPreferences) : ViewModel() {

    private val baseUrl = Configuration.DICODING_BASE_URL
    var statusModel = MutableLiveData<StatusModel>()

    fun login(email: String, password: String) {
        RetrofitClient(baseUrl).instanceDicoding.postLogin(email, password)
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
                                if (!isError) {
                                    val loginResult = jsonObject.getJSONObject("loginResult")
                                    val userModel = UserModel(
                                        loginResult.getString("userId"),
                                        loginResult.getString("name"),
                                        email,
                                        loginResult.getString("token"),
                                        true
                                    )
                                    viewModelScope.launch { pref.saveUser(userModel) }
                                }
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

    fun register(name: String, email: String, password: String) {
        RetrofitClient(baseUrl).instanceDicoding.postRegister(name, email, password)
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