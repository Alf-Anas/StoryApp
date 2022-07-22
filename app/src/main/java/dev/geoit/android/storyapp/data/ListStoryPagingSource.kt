package dev.geoit.android.storyapp.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.JsonElement
import dev.geoit.android.storyapp.model.StoryItem
import dev.geoit.android.storyapp.retrofit.DicodingEndpoint
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ListStoryPagingSource(
    private val dicodingEndpoint: DicodingEndpoint,
    private val token: String
) :
    PagingSource<Int, StoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, StoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryItem> =
        suspendCoroutine { continuation ->
            try {
                val page = params.key ?: INITIAL_PAGE_INDEX

                dicodingEndpoint.getPagingStories(
                    "Bearer $token", page, params.loadSize
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
                                        val listStory = jsonObject.getJSONArray("listStory")

                                        val list = arrayListOf<StoryItem>()
                                        for (i in 0 until listStory.length()) {
                                            val dataJSON = listStory.getJSONObject(i)
                                            val storyModel = StoryItem(
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
                                        continuation.resume(
                                            LoadResult.Page(
                                                data = list,
                                                prevKey = if (page == 1) null else page - 1,
                                                nextKey = if (list.isNullOrEmpty()) null else page + 1
                                            )
                                        )
                                    } catch (e: JSONException) {
                                        continuation.resume(LoadResult.Error(e))
                                    }
                                } else {
                                    try {
                                        val jsonObject = JSONObject(response.errorBody()!!.string())
                                        val message = jsonObject.getString("message")
                                        Log.d("AAAAAAA-4", message)
                                        continuation.resume(LoadResult.Error(Exception(message)))
                                    } catch (e: JSONException) {
                                        Log.d("AAAAAAA-3", e.toString())
                                        continuation.resume(LoadResult.Error(e))
                                    }
                                }
                            }

                            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                                Log.d("AAAAAAA-2", t.toString())
                                continuation.resume(LoadResult.Error(Exception( t.toString())))
                            }
                        })
            } catch (exception: Exception) {
                Log.d("AAAAAAA-1", exception.toString())
                continuation.resume(LoadResult.Error(exception))
            }
        }

}