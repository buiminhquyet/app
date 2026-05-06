package com.quyetdev.bankforwarder

data class WebhookConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    var url: String = "",
    var token: String = "QUYET_" + java.util.UUID.randomUUID().toString().take(6).uppercase(),
    var keywords: String = "Số dư, VND" // Từ khóa riêng cho ô này
)
