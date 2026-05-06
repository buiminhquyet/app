package com.quyetdev.bankforwarder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.quyetdev.bankforwarder.SettingsManager
import com.quyetdev.bankforwarder.databinding.FragmentAdminBinding

class FragmentAdmin : Fragment() {
    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var settings: SettingsManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settings = SettingsManager(requireContext())
        
        loadData()
        
        binding.btnSaveAdmin.setOnClickListener {
            saveData()
        }

        binding.btnSavePacks.setOnClickListener {
            settings.pack1Price = binding.editPack1Price.text.toString().toLongOrNull() ?: 50000L
            settings.pack1Turns = binding.editPack1Turns.text.toString().toIntOrNull() ?: 100
            
            settings.pack2Price = binding.editPack2Price.text.toString().toLongOrNull() ?: 100000L
            settings.pack2Turns = binding.editPack2Turns.text.toString().toIntOrNull() ?: 250
            
            settings.pack3Price = binding.editPack3Price.text.toString().toLongOrNull() ?: 300000L
            settings.pack3Turns = binding.editPack3Turns.text.toString().toIntOrNull() ?: 999999

            Toast.makeText(requireContext(), "✅ Đã cập nhật Gói cước!", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveSupport.setOnClickListener {
            settings.fbLink = binding.editFbLink.text.toString()
            settings.zaloLink = binding.editZaloLink.text.toString()
            settings.supportPhone = binding.editSupportPhone.text.toString()
            Toast.makeText(requireContext(), "✅ Đã lưu thông tin hỗ trợ!", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveTele.setOnClickListener {
            settings.telegramBotToken = binding.editTeleToken.text.toString()
            settings.telegramChatId = binding.editTeleChatId.text.toString()
            Toast.makeText(requireContext(), "✅ Đã lưu cấu hình Telegram!", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddMoney.setOnClickListener {
            val target = binding.editTargetPhone.text.toString()
            if (target == settings.userPhone) {
                settings.balance += 50000
                Toast.makeText(context, "Đã cộng 50,000đ cho $target", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "SĐT không tồn tại trên máy này!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSubMoney.setOnClickListener {
            val target = binding.editTargetPhone.text.toString()
            if (target == settings.userPhone) {
                settings.balance -= 50000
                Toast.makeText(context, "Đã trừ 50,000đ của $target", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "SĐT không tồn tại trên máy này!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadData() {
        binding.editBankCode.setText(settings.bankCode)
        binding.editBankPrefix.setText(settings.bankPrefix)
        binding.editAccountNumber.setText(settings.accountNumber)
        binding.editAccountName.setText(settings.accountName)
        
        // Hiển thị thông số Gói cước hiện tại
        binding.editPack1Price.setText(settings.pack1Price.toString())
        binding.editPack1Turns.setText(settings.pack1Turns.toString())
        binding.editPack2Price.setText(settings.pack2Price.toString())
        binding.editPack2Turns.setText(settings.pack2Turns.toString())
        binding.editPack3Price.setText(settings.pack3Price.toString())
        binding.editPack3Turns.setText(settings.pack3Turns.toString())

        // Hiển thị thông tin liên hệ & Telegram
        binding.editFbLink.setText(settings.fbLink)
        binding.editZaloLink.setText(settings.zaloLink)
        binding.editSupportPhone.setText(settings.supportPhone)
        binding.editTeleToken.setText(settings.telegramBotToken)
        binding.editTeleChatId.setText(settings.telegramChatId)
    }

    private fun saveData() {
        settings.bankCode = binding.editBankCode.text.toString()
        settings.bankPrefix = binding.editBankPrefix.text.toString()
        settings.accountNumber = binding.editAccountNumber.text.toString()
        settings.accountName = binding.editAccountName.text.toString()
        Toast.makeText(context, "✅ Đã lưu cấu hình hệ thống!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
