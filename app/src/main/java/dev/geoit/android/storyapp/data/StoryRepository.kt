package dev.geoit.android.storyapp.data

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import dev.geoit.android.storyapp.model.StoryItem
import dev.geoit.android.storyapp.retrofit.DicodingEndpoint

class StoryRepository(private val dicodingEndpoint: DicodingEndpoint, private val token:String) {
    fun getStory(): LiveData<PagingData<StoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                ListStoryPagingSource(dicodingEndpoint, token)
            }
        ).liveData
    }
}