package com.srm.wifiunblocker

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.srm.wifiunblocker.databinding.ActivityMainBinding
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), Tunnel {

    private lateinit var binding: ActivityMainBinding
    private var backend: Backend? = null
    private var isTunnelUp = false

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            connectTunnel()
        } else {
            Toast.makeText(this, "VPN permission required to unblock Wi-Fi", Toast.LENGTH_SHORT).show()
            updateUiState(false)
        }
    }

    override fun getName(): String = "SRMWiFiUnblocker"

    override fun onStateChange(newState: Tunnel.State) {
        runOnUiThread {
            isTunnelUp = (newState == Tunnel.State.UP)
            updateUiState(isTunnelUp)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                backend = GoBackend(applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                setupListeners()
                updateUiState(isTunnelUp)
            }
        }
    }

    private fun setupListeners() {
        binding.btnToggleContainer.setOnClickListener {
            if (isTunnelUp) {
                disconnectTunnel()
            } else {
                prepareAndConnect()
            }
        }
    }

    private fun prepareAndConnect() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            connectTunnel()
        }
    }

    private fun connectTunnel() {
        setConnectingState()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val warpManager = WarpManager(applicationContext)
                val config = warpManager.getOrGenerateConfig()
                backend?.setState(this@MainActivity, Tunnel.State.UP, config)
                withContext(Dispatchers.Main) {
                    isTunnelUp = true
                    updateUiState(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Connection error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    updateUiState(false)
                }
            }
        }
    }

    private fun disconnectTunnel() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                backend?.setState(this@MainActivity, Tunnel.State.DOWN, null)
                withContext(Dispatchers.Main) {
                    isTunnelUp = false
                    updateUiState(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setConnectingState() {
        binding.tvStatusText.text = getString(R.string.status_connecting)
        binding.tvStatusText.setTextColor(ContextCompat.getColor(this, R.color.status_connecting))
        binding.viewStatusDot.setBackgroundColor(ContextCompat.getColor(this, R.color.status_connecting))
        binding.tvDetailStatus.text = "Connecting WireGuard..."
        binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_connecting))
    }

    private fun updateUiState(connected: Boolean) {
        isTunnelUp = connected
        if (connected) {
            binding.tvStatusText.text = getString(R.string.status_connected)
            binding.tvStatusText.setTextColor(ContextCompat.getColor(this, R.color.status_connected))
            binding.viewStatusDot.setBackgroundColor(ContextCompat.getColor(this, R.color.status_connected))
            
            binding.btnToggleContainer.background = ContextCompat.getDrawable(this, R.drawable.bg_button_on)
            binding.tvBtnLabel.text = getString(R.string.btn_disconnect)
            
            binding.tvDetailStatus.text = "Unblocked & Protected"
            binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_connected))
        } else {
            binding.tvStatusText.text = getString(R.string.status_disconnected)
            binding.tvStatusText.setTextColor(ContextCompat.getColor(this, R.color.status_disconnected))
            binding.viewStatusDot.setBackgroundColor(ContextCompat.getColor(this, R.color.status_disconnected))
            
            binding.btnToggleContainer.background = ContextCompat.getDrawable(this, R.drawable.bg_button_off)
            binding.tvBtnLabel.text = getString(R.string.btn_unblock)
            
            binding.tvDetailStatus.text = "Ready to Connect"
            binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }
}
