package com.quyetdev.bankforwarder

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var userPhone: String
        get() = prefs.getString("user_phone", "") ?: ""
        set(value) = prefs.edit().putString("user_phone", value).apply()

    var webhookConfigsJson: String
        get() = prefs.getString("webhook_configs_v2", "[]") ?: "[]"
        set(value) = prefs.edit().putString("webhook_configs_v2", value).apply()

    var bankPrefix: String
        get() = prefs.getString("bank_prefix", "QUYETDEV") ?: "QUYETDEV"
        set(value) = prefs.edit().putString("bank_prefix", value).apply()

    var activeLogs: String
        get() = prefs.getString("active_logs", "[QSync Elite v5.0 Ready]\n") ?: ""
        set(value) = prefs.edit().putString("active_logs", value).apply()

    fun appendLogToStorage(message: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val newLog = "[$time] $message\n${activeLogs.take(5000)}" // Lưu tối đa 5000 ký tự gần nhất
        activeLogs = newLog
    }

    var apiToken: String
        get() = prefs.getString("api_token", "QUYET_PRIVATE_API_SECURE_7788") ?: ""
        set(value) = prefs.edit().putString("api_token", value).apply()

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean("service_enabled", true)
        set(value) = prefs.edit().putBoolean("service_enabled", value).apply()

    // Quản lý Đăng nhập 30p
    var lastLoginTime: Long
        get() = prefs.getLong("last_login_time", 0L)
        set(value) = prefs.edit().putLong("last_login_time", value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    // Quản lý Lượt (Turns)
    var remainingTurns: Int
        get() {
            val encrypted = prefs.getString("rem_turns_enc", "") ?: ""
            if (encrypted.isEmpty()) return 15
            return try {
                // Giải mã đơn giản để che giấu con số thực
                android.util.Base64.decode(encrypted, android.util.Base64.DEFAULT).decodeToString().toInt()
            } catch (e: Exception) { 15 }
        }
        set(value) {
            val encrypted = android.util.Base64.encodeToString(value.toString().toByteArray(), android.util.Base64.DEFAULT)
            prefs.edit().putString("rem_turns_enc", encrypted).apply()
        }

    var isAutoRenew: Boolean
        get() = prefs.getBoolean("is_auto_renew", false)
        set(value) = prefs.edit().putBoolean("is_auto_renew", value).apply()

    // Cấu hình Admin
    var bankCode: String
        get() = prefs.getString("bank_code", "MB") ?: "MB"
        set(value) = prefs.edit().putString("bank_code", value).apply()

    var accountNumber: String
        get() = prefs.getString("account_number", "0328489573") ?: "0328489573"
        set(value) = prefs.edit().putString("account_number", value).apply()

    var accountName: String
        get() = prefs.getString("account_name", "BUI MINH QUYET") ?: "BUI MINH QUYET"
        set(value) = prefs.edit().putString("account_name", value).apply()

    var fbLink: String
        get() = prefs.getString("fb_link", "https://facebook.com/pro") ?: "https://facebook.com/pro"
        set(value) = prefs.edit().putString("fb_link", value).apply()

    var zaloLink: String
        get() = prefs.getString("zalo_link", "0328489573") ?: "0328489573"
        set(value) = prefs.edit().putString("zalo_link", value).apply()

    var supportPhone: String
        get() = prefs.getString("support_phone", "0328489573") ?: "0328489573"
        set(value) = prefs.edit().putString("support_phone", value).apply()

    // Cấu hình Telegram
    var telegramBotToken: String
        get() = prefs.getString("tele_token", "") ?: ""
        set(value) = prefs.edit().putString("tele_token", value).apply()

    var telegramChatId: String
        get() = prefs.getString("tele_chat_id", "") ?: ""
        set(value) = prefs.edit().putString("tele_chat_id", value).apply()

    // Profile Info
    var balance: Long
        get() = prefs.getLong("user_balance", 0L)
        set(value) = prefs.edit().putLong("user_balance", value).apply()

    var currentPlan: String
        get() = prefs.getString("current_plan", "Miễn phí") ?: "Miễn phí"
        set(value) = prefs.edit().putString("current_plan", value).apply()

    // Thống kê doanh thu ngày
    var dailyTotal: Long
        get() = prefs.getLong("daily_total", 0L)
        set(value) = prefs.edit().putLong("daily_total", value).apply()

    var lastTotalDate: String
        get() = prefs.getString("last_total_date", "") ?: ""
        set(value) = prefs.edit().putString("last_total_date", value).apply()

    // CẤU HÌNH GÓI CƯỚC (DÀNH CHO ADMIN)
    var pack1Price: Long
        get() = prefs.getLong("pack1_price", 50000L)
        set(value) = prefs.edit().putLong("pack1_price", value).apply()
    var pack1Turns: Int
        get() = prefs.getInt("pack1_turns", 100)
        set(value) = prefs.edit().putInt("pack1_turns", value).apply()

    var pack2Price: Long
        get() = prefs.getLong("pack2_price", 100000L)
        set(value) = prefs.edit().putLong("pack2_price", value).apply()
    var pack2Turns: Int
        get() = prefs.getInt("pack2_turns", 250)
        set(value) = prefs.edit().putInt("pack2_turns", value).apply()

    var pack3Price: Long
        get() = prefs.getLong("pack3_price", 300000L)
        set(value) = prefs.edit().putLong("pack3_price", value).apply()
    var pack3Turns: Int
        get() = prefs.getInt("pack3_turns", 999999)
        set(value) = prefs.edit().putInt("pack3_turns", value).apply()
}
