package com.quyetdev.bankforwarder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BankNotificationService : NotificationListenerService() {

    private lateinit var settings: SettingsManager
    private val client = OkHttpClient()
    private val CHANNEL_ID = "BankMonitorChannel"
    private val NOTIFICATION_ID = 99
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        settings = SettingsManager(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createForegroundNotification())
        sendStatusBroadcast("Hệ thống QSync Elite đã sẵn sàng.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Giám sát Ngân hàng",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Kênh này giúp App chạy ngầm vĩnh viễn."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, flag)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("QSync Elite is ACTIVE")
            .setContentText("Đang giám sát biến động số dư 24/7.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!settings.isServiceEnabled) return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val fullContent = "$title $text"
        
        val contentLower = fullContent.lowercase()
        
        // 1. BÓC TÁCH SỐ TIỀN (Ưu tiên các mẫu phát sinh tiền vào)
        val amountMatch = Regex("(?:[+]|vua nhan duoc|phat sinh co|giao dich co)\\s*([\\d,.]+)").find(contentLower)
            ?: Regex("(?:so du [\\w]+:)\\s*([\\d,.]+)").find(contentLower)
        val amount = if (amountMatch != null) {
            amountMatch.groupValues[1].replace(".", "").replace(",", "")
        } else "0"

        if (amount == "0") return // Không phải tin nhắn giao dịch

        // Ghi log
        settings.appendLogToStorage("🔍 Bắt được: ${amount}đ - $text")

        // Cập nhật doanh thu ngày
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        if (settings.lastTotalDate != today) {
            settings.lastTotalDate = today
            settings.dailyTotal = 0L
        }
        settings.dailyTotal += amount.toLongOrNull() ?: 0L

        // 2. XỬ LÝ NẠP TIỀN CHO APP (Sử dụng Prefix từ cấu hình)
        val dynamicPrefix = settings.bankPrefix.lowercase().trim()
        val appTopupPattern = "$dynamicPrefix\\s*${settings.userPhone}".lowercase()
        if (contentLower.contains(appTopupPattern)) {
            settings.balance += amount.toLongOrNull() ?: 0L
            settings.appendLogToStorage("⚡ Đã cộng tiền vào App (Prefix: $dynamicPrefix): +${amount}đ")
            return
        }

        // 3. KIỂM TRA LƯỢT GỬI
        if (settings.remainingTurns <= 0 && settings.currentPlan != "Vô hạn") {
            settings.appendLogToStorage("⚠️ HẾT LƯỢT GỬI!")
            return
        }

        // 4. GỬI TELEGRAM
        sendToTelegram(amount, fullContent)

        // 5. GỬI WEBHOOKS
        val configJson = settings.webhookConfigsJson
        val type = object : TypeToken<List<WebhookConfig>>() {}.type
        val configs: List<WebhookConfig> = try { gson.fromJson(configJson, type) } catch (e: Exception) { emptyList() }

        for (config in configs) {
            if (config.url.isBlank()) continue
            val keywords = config.keywords.split(",").map { it.trim().lowercase() }
            if (keywords.isEmpty() || keywords.any { it.isNotEmpty() && contentLower.contains(it) }) {
                sendWebhook(config.url, amount, fullContent)
            }
        }
    }

    private fun sendToTelegram(amount: String, content: String) {
        val token = settings.telegramBotToken
        val chatId = settings.telegramChatId
        if (token.isEmpty() || chatId.isEmpty()) return

        val message = "🔔 *QSync Elite - Giao dịch mới*\n\n💰 Số tiền: *${String.format("%,d", amount.toLongOrNull() ?: 0)}đ*\n📝 Nội dung: `${content}`\n⏰ Thời gian: ${java.text.SimpleDateFormat("HH:mm:ss dd/MM").format(java.util.Date())}"
        val url = "https://api.telegram.org/bot$token/sendMessage?chat_id=$chatId&text=${java.net.URLEncoder.encode(message, "UTF-8")}&parse_mode=Markdown"
        
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) { response.close() }
        })
    }

    private fun sendWebhook(url: String, amount: String, content: String) {
        val json = JSONObject().apply {
            put("amount", amount)
            put("content", content)
            put("bank", "Notification")
        }
        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${settings.apiToken}")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                saveToOfflineBuffer(url, amount, content)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    if (settings.currentPlan != "Vô hạn") settings.remainingTurns--
                    checkAndRetryBuffer()
                } else {
                    saveToOfflineBuffer(url, amount, content)
                }
                response.close()
            }
        })
    }

    private fun saveToOfflineBuffer(url: String, amount: String, content: String) {
        try {
            val buffer = settings.prefs.getString("offline_buffer", "[]") ?: "[]"
            val array = JSONArray(buffer)
            val item = JSONObject().apply {
                put("url", url)
                put("amount", amount)
                put("content", content)
            }
            array.put(item)
            settings.prefs.edit().putString("offline_buffer", array.toString()).apply()
            settings.appendLogToStorage("📂 Đã lưu hàng chờ Offline.")
        } catch (e: Exception) {}
    }

    private fun checkAndRetryBuffer() {
        val buffer = settings.prefs.getString("offline_buffer", "[]") ?: "[]"
        if (buffer == "[]") return
        try {
            val array = JSONArray(buffer)
            settings.prefs.edit().putString("offline_buffer", "[]").apply()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                sendWebhook(item.getString("url"), item.getString("amount"), item.getString("content"))
            }
        } catch (e: Exception) {}
    }

    private fun sendStatusBroadcast(message: String) {
        val intent = Intent("com.quyetdev.LOG_UPDATE")
        intent.putExtra("message", message)
        sendBroadcast(intent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }
}
