package dev.geoit.android.storyapp.view.activity.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dev.geoit.android.storyapp.R
import dev.geoit.android.storyapp.databinding.ActivityAuthBinding
import dev.geoit.android.storyapp.view.fragment.auth.AuthPagerAdapter

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mPagerAdapter = AuthPagerAdapter(this)
        binding.authVP.adapter = mPagerAdapter
        TabLayoutMediator(binding.authTab, binding.authVP) { tab: TabLayout.Tab, position: Int ->
            tab.text = resources.getString(
                intArrayOf(
                    R.string.title_sign_in,
                    R.string.title_sign_up
                )[position]
            )
        }.attach()
    }
}