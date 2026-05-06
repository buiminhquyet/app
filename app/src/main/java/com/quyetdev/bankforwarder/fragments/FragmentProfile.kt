package com.quyetdev.bankforwarder.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.quyetdev.bankforwarder.SettingsManager
import com.quyetdev.bankforwarder.databinding.FragmentProfileBinding

class FragmentProfile : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var settings: SettingsManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settings = SettingsManager(requireContext())
        
        updateUI()
        
        // Cập nhật thông tin hiển thị lên các TextView trong CardView
        binding.txtPack1Info.text = "GÓI 1 (${String.format("%,d", settings.pack1Price)}đ)"
        binding.txtPack1Turns.text = "${settings.pack1Turns} Lượt"

        binding.txtPack2Info.text = "GÓI 2 (${String.format("%,d", settings.pack2Price)}đ)"
        binding.txtPack2Turns.text = "${settings.pack2Turns} Lượt"

        binding.txtPack3Info.text = "GÓI 3 (${String.format("%,d", settings.pack3Price)}đ)"
        binding.txtPack3Turns.text = "VÔ HẠN"

        binding.btnPack1.setOnClickListener { buyPack(settings.pack1Price, settings.pack1Turns, "Cơ bản") }
        binding.btnPack2.setOnClickListener { buyPack(settings.pack2Price, settings.pack2Turns, "VIP") }
        binding.btnPack3.setOnClickListener { buyPack(settings.pack3Price, settings.pack3Turns, "Vô hạn") }
        
        binding.switchAutoRenew.isChecked = settings.isAutoRenew
        binding.switchAutoRenew.setOnCheckedChangeListener { _, isChecked ->
            settings.isAutoRenew = isChecked
        }

        binding.btnCallPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${settings.supportPhone}"))
            startActivity(intent)
        }
        
        binding.btnOpenFb.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(settings.fbLink))
            startActivity(intent)
        }

        binding.btnOpenZalo.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://zalo.me/${settings.zaloLink}"))
            startActivity(intent)
        }

        binding.btnNotifyPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        
        binding.btnBatteryOptimization.setOnClickListener {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        }
    }

    private fun updateUI() {
        val isAdmin = settings.userPhone == "0328489573"
        binding.txtBalance.text = String.format("%,dđ", settings.balance)
        binding.txtRemainingTurns.text = if (settings.currentPlan == "Vô hạn") "KHÔNG GIỚI HẠN" else "${settings.remainingTurns} lượt"
        
        val role = if (isAdmin) "[ADMIN]" else "[USER]"
        android.widget.Toast.makeText(context, "Chào $role: ${settings.userPhone}", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun buyPack(price: Long, turns: Int, planName: String) {
        if (settings.balance >= price) {
            settings.balance -= price
            settings.remainingTurns = turns // KHÔNG CỘNG DỒN THEO YÊU CẦU
            settings.currentPlan = planName
            updateUI()
            Toast.makeText(context, "Đã kích hoạt gói $planName thành công!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Số dư không đủ. Vui lòng nạp thêm!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
