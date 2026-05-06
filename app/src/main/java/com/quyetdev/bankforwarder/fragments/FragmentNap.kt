package com.quyetdev.bankforwarder.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.quyetdev.bankforwarder.SettingsManager
import com.quyetdev.bankforwarder.databinding.FragmentNapBinding

class FragmentNap : Fragment() {
    private var _binding: FragmentNapBinding? = null
    private val binding get() = _binding!!
    private lateinit var settings: SettingsManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settings = SettingsManager(requireContext())
        
        loadData()
        setupListeners()
    }

    private fun loadData() {
        binding.txtBankCode.text = settings.bankCode
        binding.txtAccountNumber.text = settings.accountNumber
        binding.txtAccountName.text = settings.accountName
        binding.txtContent.text = "NAPTIEN1"

        // TỰ ĐỘNG TẠO MÃ QR
        val qrUrl = "https://img.vietqr.io/image/${settings.bankCode}-${settings.accountNumber}-compact.png?addInfo=NAPTIEN1&accountName=${settings.accountName}"
        
        com.bumptech.glide.Glide.with(this)
            .load(qrUrl)
            .into(binding.imgQrCode)
    }

    private fun setupListeners() {
        binding.btnCopyAccount.setOnClickListener { copyToClipboard(settings.accountNumber) }
        binding.btnCopyContent.setOnClickListener { copyToClipboard(binding.txtContent.text.toString()) }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Đã sao chép: $text", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
