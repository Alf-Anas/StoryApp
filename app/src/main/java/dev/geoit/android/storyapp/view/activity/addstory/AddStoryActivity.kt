package dev.geoit.android.storyapp.view.activity.addstory

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dev.geoit.android.storyapp.R
import dev.geoit.android.storyapp.constant.Configuration.PACKAGE_NAME
import dev.geoit.android.storyapp.databinding.ActivityAddStoryBinding
import dev.geoit.android.storyapp.model.UserPreferences
import dev.geoit.android.storyapp.utils.*
import dev.geoit.android.storyapp.utils.ImageIntent.Companion.RC_ACTION_IMAGE_CAPTURE
import dev.geoit.android.storyapp.utils.ImageIntent.Companion.RC_ACTION_IMAGE_PICK
import dev.geoit.android.storyapp.view.LoadingDialog
import dev.geoit.android.storyapp.view.ViewModelFactory
import dev.geoit.android.storyapp.view.activity.auth.AuthActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var viewModel: AddStoryViewModel
    private lateinit var loadingDialog: AlertDialog
    private lateinit var currentPhotoPath: String
    private var getFile: File? = null
    private lateinit var mToken: String
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        const val REFRESH_LIST_STORY = 111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadingDialog = LoadingDialog().initLoadingDialog(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupImagePicker()
        setupViewModel()

        binding.addStoryBTNUpload.setOnClickListener {
            if (getFile == null || binding.addStoryEDTDescription.text.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.msg_invalid_story_upload),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                showUploadDialog()
            }
        }

        binding.addStoryBTNGetCoordinate.setOnClickListener {
            getMyLastLocation()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreferences.getInstance(dataStore))
        )[AddStoryViewModel::class.java]

        viewModel.getUser().observe(this) { user ->
            if (user.isLogin) {
                mToken = user.token
            } else {
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }

        viewModel.statusModel.observe(this) { status ->
            loadingDialog.dismiss()
            Toast.makeText(this, status.message, Toast.LENGTH_SHORT).show()
            if (!status.isError) {
                setResult(REFRESH_LIST_STORY, Intent())
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    private fun showUploadDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.title_upload).setMessage(R.string.msg_upload_story)
            .setPositiveButton(R.string.btn_yes) { dialog, _ ->
                loadingDialog.show()
                Toast.makeText(this, getString(R.string.msg_uploading), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                uploadingStory()
            }
            .setNegativeButton(R.string.btn_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        builder.create()
        builder.show()
    }

    private fun uploadingStory() {
        val file = reduceFileImage(getFile as File)
        val mDesc = binding.addStoryEDTDescription.text.toString()
        val description = mDesc.toRequestBody("text/plain".toMediaType())
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )
        var lat: RequestBody? = null
        var lon: RequestBody? = null
        if (mLatitude != 0.0 && mLongitude != 0.0) {
            lat = mLatitude.toString().toRequestBody("text/plain".toMediaType())
            lon = mLongitude.toString().toRequestBody("text/plain".toMediaType())
        }
        viewModel.uploadNewStory(mToken, imageMultipart, description, lat, lon)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    getString(R.string.msg_permission_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupImagePicker() {
        binding.addStoryIMG.setOnClickListener {
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
            } else {
                val imageIntent = ImageIntent(this)
                imageIntent.initImageIntent()

                imageIntent.response.observe(this) { response ->
                    when (response) {
                        RC_ACTION_IMAGE_CAPTURE -> {
                            startTakePhoto()
                        }
                        RC_ACTION_IMAGE_PICK -> {
                            startGallery()
                        }
                    }
                }
            }
        }

        binding.addStoryFABDelete.setOnClickListener {
            getFile = null
            binding.addStoryFABDelete.visibility = View.GONE
            binding.addStoryIMG.setImageResource(R.drawable.ic_baseline_add_photo_alternate_24)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            val result = rotateBitmap(
                BitmapFactory.decodeFile(myFile.path),
                true
            )
            setFile(myFile)
            binding.addStoryIMG.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this)
            setFile(myFile)
            binding.addStoryIMG.setImageURI(selectedImg)
        }
    }

    private fun setFile(myFile: File) {
        getFile = myFile
        binding.addStoryFABDelete.visibility = View.VISIBLE
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.msg_choose_a_picture))
        launcherIntentGallery.launch(chooser)
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                PACKAGE_NAME,
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLastLocation()
                }
                else -> {
                    // No location access granted.
                    Toast.makeText(
                        this,
                        getString(R.string.msg_coordinate_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    mLatitude = location.latitude
                    mLongitude = location.longitude
                    val mLCoordinate = "[$mLatitude, $mLongitude]"
                    binding.addStoryTVCorrdinate.text = mLCoordinate
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.msg_coordinate_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

}