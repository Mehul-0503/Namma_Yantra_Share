package com.nammayantra.share.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nammayantra.share.databinding.FragmentProfileBinding
import com.nammayantra.share.ui.auth.LoginActivity
import com.nammayantra.share.ui.owner.AddMachineActivity
import com.nammayantra.share.ui.owner.OwnerDashboardActivity
import com.nammayantra.share.util.Resource

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.cardDashboard.setOnClickListener {
            startActivity(Intent(requireContext(), OwnerDashboardActivity::class.java))
        }

        binding.cardAddMachine.setOnClickListener {
            startActivity(Intent(requireContext(), AddMachineActivity::class.java))
        }

        viewModel.user.observe(viewLifecycleOwner) { res ->
            when (res) {
                is Resource.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.tvName.text = ""
                    binding.tvEmail.text = ""
                }
                is Resource.Success -> {
                    binding.progress.visibility = View.GONE
                    val user = res.data
                    binding.tvName.text = user.name
                    binding.tvEmail.text = user.email
                    binding.tvAvatar.text = user.name.firstOrNull()?.uppercase() ?: "U"
                    binding.tvRole.text = if (user.role == "owner") "🚜  Equipment Owner" else "🌾  Farmer"

                    val isOwner = user.role == "owner"
                    binding.cardDashboard.visibility = if (isOwner) View.VISIBLE else View.GONE
                    binding.cardAddMachine.visibility = if (isOwner) View.VISIBLE else View.GONE
                }
                is Resource.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.tvAvatar.text = "?"
                    binding.tvName.text = "Unknown User"
                    binding.tvEmail.text = ""
                    binding.tvRole.text = "🌾  Farmer"
                }
            }
        }
    }

    private fun showLogoutConfirmation() {
        // MaterialAlertDialogBuilder uses the app's Material3 theme correctly
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sign out?")
            .setMessage("You will be taken back to the login screen.")
            .setPositiveButton("Sign Out") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        viewModel.logout()
        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
