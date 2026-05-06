package com.quyetdev.bankforwarder

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quyetdev.bankforwarder.databinding.ActivityApiDocsBinding

class ApiDocsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApiDocsBinding
    private lateinit var settings: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApiDocsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        settings = SettingsManager(this)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCopySnippet.setOnClickListener {
            val masterToken = settings.apiToken
            val d = "$"
            val phpCode = """
<?php
/**
 * QSYNC ELITE - SIÊU TÍCH HỢP (V5.0)
 * Kết hợp 3 hệ thống: Ghi Log, Bóc tách và Cộng tiền tự động.
 */

include __DIR__ . '/db_connect.php'; // Kết nối Database của bạn

// --- 1. GHI NHẬT KÝ DEBUG (Như hệ thống của bạn) ---
${d}logData = [
    'time' => date('Y-m-d H:i:s'),
    'ip' => ${d}_SERVER['REMOTE_ADDR'] ?? '',
    'headers' => function_exists('getallheaders') ? getallheaders() : [],
    'input' => file_get_contents('php://input'),
    'post' => ${d}_POST
];
@file_put_contents(__DIR__ . '/bank_debug.txt', json_encode(${d}logData, JSON_UNESCAPED_UNICODE) . "\n", FILE_APPEND);

// --- 2. XÁC THỰC MASTER TOKEN ---
define('PRIVATE_TOKEN', '$masterToken');
${d}headers = function_exists('getallheaders') ? getallheaders() : [];
${d}receivedToken = ${d}headers['X-Private-Token'] ?? ${d}headers['token'] ?? ${d}_POST['token'] ?? '';

if (${d}receivedToken !== PRIVATE_TOKEN) {
    header('HTTP/1.1 401 Unauthorized');
    exit('Unauthorized access.');
}

// --- 3. BÓC TÁCH THÔNG MINH (Amount & User ID) ---
${d}content = ${d}_POST['content'] ?? '';
${d}amount = 0;
if (preg_match('/(?:[+]|vua nhan duoc|so du [\w]+:)\s*([\d,.]+)/i', ${d}content, ${d}matches)) {
    ${d}amount = str_replace([',', '.'], '', ${d}matches[1]);
}

${d}user_id = 0;
${d}PREFIX = 'QUYETDEV'; // Bạn có thể đổi tiền tố này tùy ý
if (preg_match('/' . ${d}PREFIX . '\s*(\d+)/i', ${d}content, ${d}matches)) {
    ${d}user_id = intval(${d}matches[1]);
}

// --- 4. XỬ LÝ CỘNG TIỀN (Logic Database chuyên sâu) ---
if (${d}amount > 0 && ${d}user_id > 0) {
    try {
        ${d}pdo->beginTransaction();
        
        // Cập nhật số dư User
        ${d}stmt = ${d}pdo->prepare("UPDATE users SET balance = balance + ? WHERE id = ?");
        ${d}stmt->execute([${d}amount, ${d}user_id]);

        // Ghi lịch sử giao dịch
        ${d}pdo->prepare("INSERT INTO transactions (user_id, amount, notes, created_at) VALUES (?, ?, ?, NOW())")
             ->execute([${d}user_id, ${d}amount, "Nạp tiền tự động qua QSync Elite"]);

        ${d}pdo->commit();
        echo "SUCCESS: Đã cộng ".number_format(${d}amount)."đ cho User #".${d}user_id;

    } catch (Exception ${d}e) {
        if (${d}pdo->inTransaction()) ${d}pdo->rollBack();
        exit("Error: " . ${d}e->getMessage());
    }
} else {
    echo "IGNORE: Không tìm thấy số tiền hoặc mã nạp " . ${d}PREFIX;
}
?>
            """.trimIndent()
            
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("PHP Code", phpCode)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "✅ Đã sao chép mã PHP Linh hoạt!", Toast.LENGTH_SHORT).show()
        }
    }
}
