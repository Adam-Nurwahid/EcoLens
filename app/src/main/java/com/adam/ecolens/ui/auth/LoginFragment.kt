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
import com.adam.ecolens.data.repository.AuthResult
import com.adam.ecolens.databinding.FragmentLoginBinding
import com.adam.ecolens.ui.ViewModelFactory

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

        // If already logged in, navigate straight to Home
        if (viewModel.isLoggedIn()) {
            findNavController().navigate(R.id.action_login_to_home)
            return
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(username, password)
        }

        binding.tvToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }

        viewModel.loginState.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (it) {
                    is AuthResult.Success -> {
                        Toast.makeText(requireContext(), "Selamat datang, ${it.user.fullName}!", Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                        findNavController().navigate(R.id.action_login_to_home)
                    }
                    is AuthResult.Error -> {
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
