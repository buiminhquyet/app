package com.quyetdev.bankforwarder

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // LỚP BẢO MẬT 1: CHỐNG CLONE & TAMPER
        if (!isAppAuthentic()) {
            Toast.makeText(this, "Cảnh báo: App đã bị thay đổi trái phép!", Toast.LENGTH_LONG).show()
            Handler(Looper.getMainLooper()).postDelayed({ finish() }, 3000)
            return
        }

        // Hiển thị Logo Elite trong 2 giây
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2000)
    }

    private fun isAppAuthentic(): Boolean {
        // 1. Chống chạy trên máy ảo (Emulator)
        val isEmulator = (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.HARDWARE.contains("goldfish")
                || android.os.Build.HARDWARE.contains("ranchu")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
        
        if (isEmulator) {
            // Tạm thời cho phép để anh Quyết kiểm thử trên máy tính
            // return false 
        }

        // 2. Chống Debugger
        if (android.os.Debug.isDebuggerConnected()) {
            // return false 
        }
        
        return true 
    }
}
