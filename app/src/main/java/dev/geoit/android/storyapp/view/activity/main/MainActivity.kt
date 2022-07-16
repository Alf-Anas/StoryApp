package dev.geoit.android.storyapp.view.activity.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import dev.geoit.android.storyapp.model.UserPreferences
import dev.geoit.android.storyapp.view.LoadingDialog
import dev.geoit.android.storyapp.view.ViewModelFactory
import dev.geoit.android.storyapp.view.activity.addstory.AddStoryActivity
import dev.geoit.android.storyapp.view.activity.addstory.AddStoryActivity.Companion.REFRESH_LIST_STORY
import dev.geoit.android.storyapp.view.activity.auth.AuthActivity


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ListStoryAdapter
    private lateinit var loadingDialog: AlertDialog
    private var mPage = 1
    private lateinit var mToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog().initLoadingDialog(this)
        setupViewModel()
        setupListStories()

        binding.mainFABAdd.setOnClickListener {
            startAddStoryActivity.launch(Intent(this, AddStoryActivity::class.java))
        }

        binding.mainBTNNext.setOnClickListener {
            mPage++
            triggerGetListStories()
        }

        binding.mainBTNBefore.setOnClickListener {
            if (mPage > 1) {
                mPage--
                triggerGetListStories()
            }
        }
    }

    private val startAddStoryActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == REFRESH_LIST_STORY) {
                triggerGetListStories()
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

        viewModel.statusModel.observe(this) { status ->
            if (status.isError) {
                Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
            }
            loadingDialog.dismiss()
        }
    }

    private fun triggerGetListStories() {
        if (this::mToken.isInitialized) {
            loadingDialog.show()
            viewModel.getListStories(mToken, mPage)
            binding.mainTVPage.text = mPage.toString()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupListStories() {
        adapter = ListStoryAdapter()
        adapter.notifyDataSetChanged()
        binding.mainRV.layoutManager = LinearLayoutManager(this)
        binding.mainRV.adapter = adapter

        viewModel.listStories.observe(this) { listStories ->
            if (listStories != null) {
                adapter.setData(listStories)
            }
        }
    }

}