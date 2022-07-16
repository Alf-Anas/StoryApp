package dev.geoit.android.storyapp.model

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dev.geoit.android.storyapp.R
import dev.geoit.android.storyapp.databinding.ItemListStoryBinding
import dev.geoit.android.storyapp.view.activity.detailstory.DetailStoryActivity

@SuppressLint("NotifyDataSetChanged")
class ListStoryAdapter : RecyclerView.Adapter<ListStoryAdapter.ListViewHolder>() {

    private val mData = ArrayList<StoryModel>()
    fun setData(items: ArrayList<StoryModel>) {
        mData.clear()
        mData.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_list_story, viewGroup, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(mData[position])
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemListStoryBinding.bind(itemView)
        fun bind(story: StoryModel) {
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
                setObjectIntent.putExtra(DetailStoryActivity.DETAIL_STORY, story)
                it.context.startActivity(setObjectIntent, optionsCompat.toBundle())
            }
        }
    }
}

