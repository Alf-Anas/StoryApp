package dev.geoit.android.storyapp.view.fragment.auth

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AuthPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private var listFragment = listOf(SignInFragment(), SignUpFragment())

    override fun getItemCount(): Int = listFragment.size

    override fun createFragment(position: Int): Fragment {
        return listFragment[position]
    }
}