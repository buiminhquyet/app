package com.quyetdev.bankforwarder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quyetdev.bankforwarder.SettingsManager
import com.quyetdev.bankforwarder.WebhookConfig
import com.quyetdev.bankforwarder.R
import com.quyetdev.bankforwarder.databinding.FragmentApiBinding
import com.quyetdev.bankforwarder.databinding.ItemWebhookBinding
import java.util.*

class FragmentAPI : Fragment() {
    private var _binding: FragmentApiBinding? = null
    private val binding get() = _binding!!
    private lateinit var settings: SettingsManager
    private val gson = Gson()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settings = SettingsManager(requireContext())
        
        loadWebhooks()
        binding.txtToken.text = settings.apiToken
        
        binding.btnApiDocs.setOnClickListener {
            val intent = android.content.Intent(requireContext(), com.quyetdev.bankforwarder.ApiDocsActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnAddWebhook.setOnClickListener {
            addWebhookView(WebhookConfig())
        }
        
        binding.btnSaveAll.setOnClickListener {
            saveWebhooks()
        }
        
        binding.btnNewToken.setOnClickListener {
            val newToken = "QUYET_" + UUID.randomUUID().toString().take(8).uppercase()
            settings.apiToken = newToken
            binding.txtToken.text = newToken
            Toast.makeText(context, "Đã tạo Token mới!", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnCopyToken.setOnClickListener {
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("API Token", settings.apiToken)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "✅ Đã sao chép mã Token!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadWebhooks() {
        val json = settings.webhookConfigsJson
        val type = object : TypeToken<List<WebhookConfig>>() {}.type
        val configs: List<WebhookConfig> = try { gson.fromJson(json, type) } catch (e: Exception) { emptyList() }
        
        binding.webhookContainer.removeAllViews()
        if (configs.isEmpty()) {
            addWebhookView(WebhookConfig())
        } else {
            configs.forEach { addWebhookView(it) }
        }
    }

    private fun addWebhookView(config: WebhookConfig) {
        val itemBinding = ItemWebhookBinding.inflate(layoutInflater, binding.webhookContainer, false)
        itemBinding.editWebhookUrl.setText(config.url)
        itemBinding.editWebhookToken.setText(config.token)
        itemBinding.editWebhookKeywords.setText(config.keywords)

        itemBinding.btnRemoveWebhook.setOnClickListener {
            binding.webhookContainer.removeView(itemBinding.root)
        }
        binding.webhookContainer.addView(itemBinding.root)
    }

    private fun saveWebhooks() {
        val configs = mutableListOf<WebhookConfig>()
        for (i in 0 until binding.webhookContainer.childCount) {
            val view = binding.webhookContainer.getChildAt(i)
            val url = view.findViewById<EditText>(R.id.editWebhookUrl).text.toString().trim()
            val token = view.findViewById<EditText>(R.id.editWebhookToken).text.toString().trim()
            val keywords = view.findViewById<EditText>(R.id.editWebhookKeywords).text.toString().trim()
            
            if (url.isNotEmpty()) {
                configs.add(WebhookConfig(url = url, token = token, keywords = keywords))
            }
        }
        
        settings.webhookConfigsJson = gson.toJson(configs)
        
        // Thử nghiệm lưu thêm một bản copy cho chắc chắn
        val context = requireContext()
        val prefs = context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("webhook_configs_v2", gson.toJson(configs)).commit() 
        
        Toast.makeText(context, "✅ ĐÃ LƯU ${configs.size} API. Hãy sang Tab Home để Test!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
