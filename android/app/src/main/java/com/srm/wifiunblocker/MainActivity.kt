package com.srm.wifiunblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.srm.wifiunblocker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isConnected = false

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startStealthVpn()
        } else {
            Toast.makeText(this, "VPN permission required to bypass Wi-Fi block", Toast.LENGTH_SHORT).show()
            updateUiState(false)
        }
    }

    private val vpnStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val connected = intent?.getBooleanExtra("connected", false) ?: false
            updateUiState(connected)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        updateUiState(StealthVpnService.isConnected)
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("com.srm.wifiunblocker.VPN_STATUS_CHANGED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(vpnStatusReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(vpnStatusReceiver, filter)
        }
        updateUiState(StealthVpnService.isConnected)
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(vpnStatusReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
    }

    private fun setupListeners() {
        binding.btnToggleContainer.setOnClickListener {
            if (isConnected) {
                stopStealthVpn()
            } else {
                prepareAndStartVpn()
            }
        }
    }

    private fun prepareAndStartVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startStealthVpn()
        }
    }

    private fun startStealthVpn() {
        setConnectingState()
        val serviceIntent = Intent(this, StealthVpnService::class.java).apply {
            action = StealthVpnService.ACTION_CONNECT
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopStealthVpn() {
        val serviceIntent = Intent(this, StealthVpnService::class.java).apply {
            action = StealthVpnService.ACTION_DISCONNECT
        }
        startService(serviceIntent)
    }

    private fun setConnectingState() {
        binding.tvStatusText.text = getString(R.string.status_connecting)
        binding.tvStatusText.setTextColor(ContextCompat.getColor(this, R.color.status_connecting))
        binding.viewStatusDot.setBackgroundColor(ContextCompat.getColor(this, R.color.status_connecting))
        binding.tvDetailStatus.text = "Connecting..."
        binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_connecting))
    }

    private fun updateUiState(connected: Boolean) {
        isConnected = connected
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
            binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_secondary_muted))
        }
    }

    private val R.color.status_secondary_muted: Int
        get() = R.color.text_secondary
}
