package dev.geoit.android.storyapp.view.activity.detailstory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dev.geoit.android.storyapp.R
import dev.geoit.android.storyapp.databinding.ActivityDetailStoryBinding
import dev.geoit.android.storyapp.model.StoryModel

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding

    companion object {
        const val DETAIL_STORY = "detail_story"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val storyModel = intent.getParcelableExtra<StoryModel>(DETAIL_STORY) as StoryModel

        Glide.with(this)
            .load(storyModel.photoUrl)
            .apply(
                RequestOptions()
                    .error(R.drawable.ic_baseline_broken_image_24)
                    .placeholder(R.drawable.ic_baseline_image_search_24)
            )
            .into(binding.detailStoryIMG)
        binding.detailStoryTVDate.text = storyModel.createdAt
        binding.detailStoryTVName.text = storyModel.name
        binding.detailStoryTVDescription.text = storyModel.description
        binding.detailStoryTVCoordinate.text =
            if (storyModel.lat != null && storyModel.lon != null) "[" + storyModel.lon + ", " + storyModel.lat + "]" else ""
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}