package com.quyetdev.bankforwarder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.quyetdev.bankforwarder.databinding.ActivityMainBinding
import com.quyetdev.bankforwarder.fragments.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
        checkNotificationPermission()
        
        // LUÔN HIỆN ADMIN ĐỂ BẠN CẤU HÌNH (Sẽ khóa lại sau theo yêu cầu)
        binding.bottomNavigation.menu.findItem(R.id.nav_admin).isVisible = true
        
        val settings = SettingsManager(this)
        binding.btnLogout.setOnClickListener {
            settings.isLoggedIn = false
            settings.userPhone = ""
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        
        // Mặc định mở trang chủ
        if (savedInstanceState == null) {
            loadFragment(FragmentHome(), "HOME")
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(FragmentHome(), "HOME")
                    true
                }
                R.id.nav_nap -> {
                    loadFragment(FragmentNap(), "NAP")
                    true
                }
                R.id.nav_api -> {
                    loadFragment(FragmentAPI(), "API")
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(FragmentProfile(), "PROFILE")
                    true
                }
                R.id.nav_admin -> {
                    loadFragment(FragmentAdmin(), "ADMIN")
                    true
                }
                else -> false
            }
        }
    }

    private fun checkNotificationPermission() {
        val enabledListeners = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val isPermissionGranted = enabledListeners != null && enabledListeners.contains(packageName)
        
        if (!isPermissionGranted) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("🔔 Yêu cầu cấp quyền")
            builder.setMessage("Để QUYET ELITE có thể tự động bắt biến động số dư, bạn cần cấp quyền 'Truy cập thông báo' cho ứng dụng.")
            builder.setPositiveButton("CẤP QUYỀN") { _, _ ->
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
            builder.setCancelable(false)
            builder.show()
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        try {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment, tag)
            transaction.commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
