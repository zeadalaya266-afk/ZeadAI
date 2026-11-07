package com.zead.ai.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zead.ai.R
import com.zead.ai.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        observeState()
    }

    private fun setupUi() {
        binding.apiKeyInput.setText(viewModel.loadSavedApiKey())

        binding.saveButton.setOnClickListener {
            val apiKey = binding.apiKeyInput.text?.toString() ?: ""
            viewModel.saveApiKey(apiKey)
        }

        binding.testButton.setOnClickListener {
            val apiKey = binding.apiKeyInput.text?.toString() ?: ""
            viewModel.testConnection(apiKey)
        }
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SettingsUiState.Idle -> hideStatus()
                is SettingsUiState.Loading -> showLoading()
                is SettingsUiState.Saved -> showStatus(
                    message = getString(R.string.api_key_saved),
                    colorRes = R.color.success_green
                )
                is SettingsUiState.Success -> showStatus(
                    message = getString(R.string.test_success),
                    colorRes = R.color.success_green
                )
                is SettingsUiState.Error -> showStatus(
                    message = state.message,
                    colorRes = R.color.error_red
                )
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.statusText.visibility = View.GONE
        binding.saveButton.isEnabled = false
        binding.testButton.isEnabled = false
    }

    private fun hideStatus() {
        binding.progressBar.visibility = View.GONE
        binding.statusText.visibility = View.GONE
        binding.saveButton.isEnabled = true
        binding.testButton.isEnabled = true
    }

    private fun showStatus(message: String, colorRes: Int) {
        binding.progressBar.visibility = View.GONE
        binding.statusText.visibility = View.VISIBLE
        binding.statusText.text = message
        binding.statusText.setTextColor(
            ContextCompat.getColor(requireContext(), colorRes)
        )
        binding.saveButton.isEnabled = true
        binding.testButton.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}