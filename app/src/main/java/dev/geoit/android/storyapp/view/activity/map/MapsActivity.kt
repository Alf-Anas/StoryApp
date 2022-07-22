package dev.geoit.android.storyapp.view.activity.map

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import dev.geoit.android.storyapp.R
import dev.geoit.android.storyapp.databinding.ActivityMapsBinding
import dev.geoit.android.storyapp.model.StoryModel
import dev.geoit.android.storyapp.model.UserPreferences
import dev.geoit.android.storyapp.view.LoadingDialog
import dev.geoit.android.storyapp.view.ViewModelFactory
import dev.geoit.android.storyapp.view.activity.auth.AuthActivity
import dev.geoit.android.storyapp.view.activity.detailstory.DetailStoryActivity
import dev.geoit.android.storyapp.view.activity.main.MainViewModel


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var viewModel: MainViewModel
    private lateinit var loadingDialog: AlertDialog
    private var mPage = 1
    private val searchUsingLocation = 1
    private lateinit var mToken: String
    private lateinit var listStory: ArrayList<StoryModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadingDialog = LoadingDialog().initLoadingDialog(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupViewModel()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getMyLocation()
        mMap.setOnInfoWindowClickListener {
            if (listStory.size > 0) {
                for (story in listStory) {
                    if (story.id == it.tag) {
                        val setObjectIntent = Intent(this, DetailStoryActivity::class.java)
                        setObjectIntent.putExtra(DetailStoryActivity.DETAIL_STORY, story)
                        startActivity(setObjectIntent)
                        break
                    }
                }
            }
        }
        setMapStyle()
        setupMapData()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    private fun setupMapData() {
        if (this::mMap.isInitialized && this::listStory.isInitialized) {
            val boundsBuilder = LatLngBounds.Builder()
            val minDescLength = 25
            for (story in listStory) {
                if (story.lat != null && story.lon != null) {
                    val storyLatLng = LatLng(story.lat, story.lon)
                    val marker = mMap.addMarker(
                        MarkerOptions().position(storyLatLng).title(story.name)
                            .snippet(
                                story.description.take(
                                    minDescLength
                                ) + "..."
                            )
                    )
                    marker?.tag = story.id
                    boundsBuilder.include(storyLatLng)
                }
            }
            val bounds: LatLngBounds = boundsBuilder.build()
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels,
                    300
                )
            )
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
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

        viewModel.listStories.observe(this) { listStories ->
            if (listStories != null) {
                listStory = listStories
                setupMapData()
                if (listStories.size == 0) {
                    Toast.makeText(
                        this,
                        getString(R.string.msg_no_data_found),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    private fun triggerGetListStories() {
        if (this::mToken.isInitialized) {
            loadingDialog.show()
            viewModel.getListStories(mToken, mPage, 100, searchUsingLocation)
        }
    }
}