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
import com.adam.ecolens.databinding.FragmentRegisterBinding
import com.adam.ecolens.ui.ViewModelFactory

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString()
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.register(username, password, fullName)
        }

        binding.tvToLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        }

        viewModel.registerState.observe(viewLifecycleOwner) { result ->
            result?.let {
                when (it) {
                    is AuthResult.Success -> {
                        Toast.makeText(requireContext(), "Akun berhasil dibuat! Selamat datang, ${it.user.fullName}!", Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                        findNavController().navigate(R.id.action_register_to_home)
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
