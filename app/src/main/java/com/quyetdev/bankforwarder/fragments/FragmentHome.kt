package com.quyetdev.bankforwarder.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.quyetdev.bankforwarder.SettingsManager
import com.quyetdev.bankforwarder.WebhookConfig
import com.quyetdev.bankforwarder.databinding.FragmentHomeBinding
import java.util.*

class FragmentHome : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var settings: SettingsManager

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.quyetdev.LOG_UPDATE") {
                val message = intent.getStringExtra("message") ?: ""
                appendLog(message)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        settings = SettingsManager(requireContext())
        binding.txtLogs.text = settings.activeLogs

        binding.btnClearLogs.setOnClickListener {
            settings.activeLogs = ""
            binding.txtLogs.text = ""
        }

        updateDailyTotal()

        binding.btnTestConn.setOnClickListener {
            testWebhooks()
        }
        
        val filter = IntentFilter("com.quyetdev.LOG_UPDATE")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            requireContext().registerReceiver(receiver, filter)
        }
    }

    private fun testWebhooks() {
        val settings = com.quyetdev.bankforwarder.SettingsManager(requireContext())
        val gson = com.google.gson.Gson()
        val configJson = settings.webhookConfigsJson
        val type = object : com.google.gson.reflect.TypeToken<List<com.quyetdev.bankforwarder.WebhookConfig>>() {}.type
        val configs: List<com.quyetdev.bankforwarder.WebhookConfig> = try { gson.fromJson(configJson, type) } catch (e: Exception) { emptyList() }

        if (configs.isEmpty()) {
            appendLog("Chưa có Webhook nào để kiểm tra.")
            return
        }

        appendLog("Bắt đầu kiểm tra ${configs.size} Webhook...")
        val client = okhttp3.OkHttpClient()

        configs.forEach { config ->
            val masterToken = settings.apiToken.trim() // QUYET_PRIVATE_API_SECURE_7788
            val itemToken = config.token.trim()
            
            val separator = if (config.url.contains("?")) "&" else "?"
            val finalUrl = "${config.url}${separator}token=$masterToken"
            
            val body = okhttp3.FormBody.Builder()
                .add("token", masterToken) // Gửi Master Token để khớp PHP
                .add("item_token", itemToken)
                .add("title", "Vietcombank")
                .add("content", "Số dư TK +100,000VND")
                .build()

            val request = okhttp3.Request.Builder()
                .url(finalUrl)
                .addHeader("token", masterToken)
                .addHeader("X-Private-Token", masterToken)
                .addHeader("Authorization", masterToken)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    activity?.runOnUiThread { appendLog("❌ LỖI MẠNG: ${e.message}") }
                }
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val respBody = response.body?.string() ?: ""
                    val isOk = response.code == 200 || response.code == 201
                    val status = if (isOk) "✅ KẾT NỐI OK" else "⚠️ LỖI ${response.code}"
                    
                    activity?.runOnUiThread { 
                        appendLog("$status [${config.url.takeLast(10)}]")
                        if (!isOk) appendLog("-> Web bảo: ${respBody.take(40)}")
                    }
                    response.close()
                }
            })
        }
    }

    private fun appendLog(message: String) {
        settings.appendLogToStorage(message)
        activity?.runOnUiThread {
            binding.txtLogs.text = settings.activeLogs
        }
    }

    private fun updateDailyTotal() {
        if (_binding != null) {
            binding.txtDailyTotal.text = String.format("%,dđ", settings.dailyTotal)
        }
    }

    override fun onResume() {
        super.onResume()
        updateDailyTotal()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            requireContext().unregisterReceiver(receiver)
        } catch (e: Exception) {}
        _binding = null
    }
}
