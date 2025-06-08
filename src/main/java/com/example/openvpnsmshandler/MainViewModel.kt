package com.example.openvpnsmshandler

import android.content.Context
import android.telephony.SmsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class UiState(
    val isVpnConnected: Boolean = false,
    val serverAddress: String = "",
    val serverPort: String = "1194",
    val logs: List<String> = emptyList(),
    val smsSentCount: Int = 0,
    val callsMadeCount: Int = 0,
    val totalRequests: Int = 0
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private val vpnClient = OpenVPNClient()
    private val httpServer = HTTPServer()
    init {
        startHTTPServer()
    }
    fun updateServerAddress(address: String) {
        _uiState.value = _uiState.value.copy(serverAddress = address)
    }
    fun updateServerPort(port: String) {
        _uiState.value = _uiState.value.copy(serverPort = port)
    }
    fun connectVPN(context: Context) {
        viewModelScope.launch {
            try {
                addLog("Connecting to VPN...")
                val success = vpnClient.connect(
                    _uiState.value.serverAddress,
                    _uiState.value.serverPort.toInt()
                )
                if (success) {
                    _uiState.value = _uiState.value.copy(isVpnConnected = true)
                    addLog("VPN connected successfully")
                } else {
                    addLog("VPN connection failed")
                }
            } catch (e: Exception) {
                addLog("VPN connection error: ${e.message}")
            }
        }
    }
    fun disconnectVPN() {
        viewModelScope.launch {
            try {
                vpnClient.disconnect()
                _uiState.value = _uiState.value.copy(isVpnConnected = false)
                addLog("VPN disconnected")
            } catch (e: Exception) {
                addLog("VPN disconnection error: ${e.message}")
            }
        }
    }
    fun clearLogs() {
        _uiState.value = _uiState.value.copy(logs = emptyList())
    }
    private fun startHTTPServer() {
        viewModelScope.launch {
            try {
                httpServer.start(8080) { request ->
                    handleIncomingRequest(request)
                }
                addLog("HTTP server started on port 8080")
            } catch (e: Exception) {
                addLog("Failed to start HTTP server: ${e.message}")
            }
        }
    }
    private fun handleIncomingRequest(request: HTTPRequest) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    totalRequests = _uiState.value.totalRequests + 1
                )
                when (request.type) {
                    "SMS" -> {
                        sendSMS(request.phoneNumber, request.message)
                        _uiState.value = _uiState.value.copy(
                            smsSentCount = _uiState.value.smsSentCount + 1
                        )
                    }
                    "CALL" -> {
                        makeCall(request.phoneNumber)
                        _uiState.value = _uiState.value.copy(
                            callsMadeCount = _uiState.value.callsMadeCount + 1
                        )
                    }
                }
                addLog("Processed ${request.type} request for ${request.phoneNumber}")
            } catch (e: Exception) {
                addLog("Error processing request: ${e.message}")
            }
        }
    }
    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            addLog("SMS sent to $phoneNumber")
        } catch (e: Exception) {
            addLog("SMS sending failed: ${e.message}")
        }
    }
    private fun makeCall(phoneNumber: String) {
        try {
            // Note: This would require CALL_PHONE permission and context
            addLog("Call initiated to $phoneNumber")
        } catch (e: Exception) {
            addLog("Call failed: ${e.message}")
        }
    }
    private fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "[$timestamp] $message"
        val currentLogs = _uiState.value.logs.toMutableList()
        currentLogs.add(0, logEntry)
        if (currentLogs.size > 100) {
            currentLogs.removeAt(currentLogs.size - 1)
        }
        _uiState.value = _uiState.value.copy(logs = currentLogs)
    }
}
