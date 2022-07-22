package dev.geoit.android.storyapp.model

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dev.geoit.android.storyapp.R
import dev.geoit.android.storyapp.databinding.ItemListStoryBinding
import dev.geoit.android.storyapp.view.activity.detailstory.DetailStoryActivity

class ListStoryAdapter :
    PagingDataAdapter<StoryItem, ListStoryAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        val binding =
            ItemListStoryBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    class MyViewHolder(private val binding: ItemListStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryItem) {
            val minDescLength = 25
            binding.itemListStoryTVDate.text = story.createdAt
            binding.itemListStoryTVName.text = story.name
            binding.itemListStoryTVDescription.text =
                if (story.description.length < minDescLength) story.description else story.description.take(
                    minDescLength
                ) + "..."
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .apply(
                    RequestOptions()
                        .error(R.drawable.ic_baseline_broken_image_24)
                        .placeholder(R.drawable.ic_baseline_image_search_24)
                        .centerCrop()
                )
                .into(binding.itemListStoryIMG)

            itemView.setOnClickListener {
                val setObjectIntent = Intent(it.context, DetailStoryActivity::class.java)
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        it.context as Activity,
                        Pair(binding.itemListStoryIMG, "photo"),
                        Pair(binding.itemListStoryTVName, "name"),
                        Pair(binding.itemListStoryTVDescription, "description"),
                    )
                val storyModel = StoryModel(
                    story.id,
                    story.name,
                    story.description,
                    story.photoUrl, story.createdAt, story.lat, story.lon
                )
                setObjectIntent.putExtra(DetailStoryActivity.DETAIL_STORY, storyModel)
                it.context.startActivity(setObjectIntent, optionsCompat.toBundle())
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryItem>() {
            override fun areItemsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}

