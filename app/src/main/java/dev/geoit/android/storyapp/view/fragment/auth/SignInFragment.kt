package dev.geoit.android.storyapp.view.fragment.auth

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.geoit.android.storyapp.R
import dev.geoit.android.storyapp.databinding.FragmentSignInBinding
import dev.geoit.android.storyapp.model.UserPreferences
import dev.geoit.android.storyapp.view.LoadingDialog
import dev.geoit.android.storyapp.view.ViewModelFactory
import dev.geoit.android.storyapp.view.activity.main.MainActivity


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSignInBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var loadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog().initLoadingDialog(requireActivity())
        setupViewModel()

        binding.signInBTN.setOnClickListener {
            if (binding.signInEDTEmail.error.isNullOrEmpty() &&
                binding.signInEDTPassword.error.isNullOrEmpty() &&
                !binding.signInEDTEmail.text.isNullOrEmpty() &&
                !binding.signInEDTPassword.text.isNullOrEmpty()
            ) {
                loadingDialog.show()
                showMessage("", false)
                viewModel.login(
                    binding.signInEDTEmail.text.toString(),
                    binding.signInEDTPassword.text.toString()
                )
            } else {
                showMessage(
                    requireActivity().getString(R.string.msg_invalid_email_address) +
                            "\n" +
                            requireActivity().getString(R.string.msg_invalid_minimum_password),
                    true
                )
            }
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreferences.getInstance(requireActivity().dataStore))
        )[AuthViewModel::class.java]

        viewModel.statusModel.observe(requireActivity()) { status ->
            if (status.isError) {
                showMessage(status.message, true)
            } else {
                showMessage("", false)
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            }
            loadingDialog.dismiss()
        }
    }

    private fun showMessage(message: String, visibility: Boolean) {
        binding.signInTVMessage.visibility = if (visibility) View.VISIBLE else View.GONE
        binding.signInTVMessage.text = message
    }


}