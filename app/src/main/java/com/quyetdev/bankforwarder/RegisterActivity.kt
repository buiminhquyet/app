package com.quyetdev.bankforwarder

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quyetdev.bankforwarder.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegisterSubmit.setOnClickListener {
            val phone = binding.editRegPhone.text.toString()
            val pass = binding.editRegPassword.text.toString()
            val confirm = binding.editRegConfirmPassword.text.toString()

            if (phone.isNotEmpty() && pass.isNotEmpty() && confirm.isNotEmpty()) {
                if (pass == confirm) {
                    Toast.makeText(this, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLoginBack.setOnClickListener {
            finish()
        }
    }
}
