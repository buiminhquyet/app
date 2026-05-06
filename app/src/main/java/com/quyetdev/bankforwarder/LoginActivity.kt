package com.quyetdev.bankforwarder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quyetdev.bankforwarder.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = SettingsManager(this)
        
        // KIỂM TRA SESSION 30 PHÚT
        val currentTime = System.currentTimeMillis()
        val lastLogin = settings.lastLoginTime
        val isSessionValid = (currentTime - lastLogin) < (30 * 60 * 1000)

        if (settings.isLoggedIn && isSessionValid) {
            settings.lastLoginTime = currentTime // Gia hạn session
            startMainActivity()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val phone = binding.editPhone.text.toString()
            val pass = binding.editPassword.text.toString()

            if (phone.isNotEmpty() && pass.isNotEmpty()) {
                settings.isLoggedIn = true
                settings.userPhone = phone
                settings.lastLoginTime = System.currentTimeMillis()
                startMainActivity()
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
