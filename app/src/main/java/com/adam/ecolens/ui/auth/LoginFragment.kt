package com.adam.ecolens.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.adam.ecolens.R
import com.adam.ecolens.data.repository.FirebaseAuthResult
import com.adam.ecolens.databinding.FragmentLoginBinding
import com.adam.ecolens.ui.ViewModelFactory

/**
 * Passwordless login screen.
 * The user only enters their display name; Firebase Anonymous Auth
 * generates a unique UID behind the scenes.
 *
 * On subsequent launches, if a session is already stored locally, the
 * fragment skips directly to HomeFragment without showing the form.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If an existing session is found, skip straight to Home
        if (viewModel.isLoggedIn()) {
            findNavController().navigate(R.id.action_login_to_home)
            return
        }

        // Trigger anonymous sign-in with the entered name
        binding.btnLogin.setOnClickListener {
            val name = binding.etUsername.text.toString()
            viewModel.signInAnonymously(name)
        }

        // Show/hide loading indicator and disable button during sign-in
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }

        // React to the sign-in result
        viewModel.loginState.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (it) {
                    is FirebaseAuthResult.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Selamat datang, ${it.displayName}! 🌿",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetState()
                        findNavController().navigate(R.id.action_login_to_home)
                    }
                    is FirebaseAuthResult.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
