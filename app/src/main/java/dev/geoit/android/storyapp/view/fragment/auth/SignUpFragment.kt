package dev.geoit.android.storyapp.view.fragment.auth

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.geoit.android.storyapp.R
import dev.geoit.android.storyapp.databinding.FragmentSignUpBinding
import dev.geoit.android.storyapp.model.UserPreferences
import dev.geoit.android.storyapp.view.LoadingDialog
import dev.geoit.android.storyapp.view.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SignUpFragment : Fragment() {

    private lateinit var binding: FragmentSignUpBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var loadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog().initLoadingDialog(requireActivity())
        setupViewModel()

        binding.signUpBTN.setOnClickListener {
            if (binding.signUpEDTEmail.error.isNullOrEmpty() &&
                binding.signUpEDTPassword.error.isNullOrEmpty() &&
                !binding.signUpEDTName.text.isNullOrEmpty() &&
                !binding.signUpEDTEmail.text.isNullOrEmpty() &&
                !binding.signUpEDTPassword.text.isNullOrEmpty()
            ) {
                loadingDialog.show()
                showMessage("", false)
                viewModel.register(
                    binding.signUpEDTName.text.toString(),
                    binding.signUpEDTEmail.text.toString(),
                    binding.signUpEDTPassword.text.toString()
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
            showMessage(status.message, true)
            if (!status.isError) {
                Toast.makeText(requireActivity(), status.message, Toast.LENGTH_SHORT).show()
            }
            loadingDialog.dismiss()
        }
    }

    private fun showMessage(message: String, visibility: Boolean) {
        binding.signUpTVMessage.visibility = if (visibility) View.VISIBLE else View.GONE
        binding.signUpTVMessage.text = message
    }

}