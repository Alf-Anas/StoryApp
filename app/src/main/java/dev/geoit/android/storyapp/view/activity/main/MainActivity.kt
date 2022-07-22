package dev.geoit.android.storyapp.view.activity.main

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dev.geoit.android.storyapp.R
import dev.geoit.android.storyapp.databinding.ActivityMainBinding
import dev.geoit.android.storyapp.model.ListStoryAdapter
import dev.geoit.android.storyapp.model.LoadingStateAdapter
import dev.geoit.android.storyapp.model.UserPreferences
import dev.geoit.android.storyapp.view.ViewModelFactory
import dev.geoit.android.storyapp.view.activity.addstory.AddStoryActivity
import dev.geoit.android.storyapp.view.activity.addstory.AddStoryActivity.Companion.REFRESH_LIST_STORY
import dev.geoit.android.storyapp.view.activity.auth.AuthActivity
import dev.geoit.android.storyapp.view.activity.map.MapsActivity


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ListStoryAdapter
    private lateinit var mToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListStories()
        setupViewModel()

        binding.mainFABAdd.setOnClickListener {
            startAddStoryActivity.launch(Intent(this, AddStoryActivity::class.java))
        }
        binding.mainFABMap.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
    }

    private val startAddStoryActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == REFRESH_LIST_STORY) {
                val delayTimes = 1500L
                Handler(Looper.getMainLooper()).postDelayed({
                    run {
                        adapter.refresh()
                    }
                }, delayTimes)
            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_bar_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuLanguage -> startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            R.id.menuSignOut -> showSignOutDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSignOutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.title_sign_out).setMessage(R.string.msg_sign_out)
            .setPositiveButton(R.string.btn_yes) { dialog, _ ->
                Toast.makeText(this, getString(R.string.title_sign_out), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                viewModel.logout()
            }
            .setNegativeButton(R.string.btn_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        builder.create()
        builder.show()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreferences.getInstance(dataStore))
        )[MainViewModel::class.java]

        viewModel.getUser().observe(this) { user ->
            if (user.isLogin) {
                mToken = user.token
                triggerGetListStories()
            } else {
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }
    }

    private fun triggerGetListStories() {
        if (this::mToken.isInitialized) {
            viewModel.storyList(mToken).observe(this) { listStories ->
                if (listStories != null) {
                    adapter.submitData(lifecycle, listStories)
                }
            }
        }
    }

    private fun setupListStories() {
        adapter = ListStoryAdapter()
        binding.mainRV.layoutManager = LinearLayoutManager(this)
        binding.mainRV.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
    }

}