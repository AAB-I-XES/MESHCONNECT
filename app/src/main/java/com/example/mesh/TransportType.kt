package com.example.mesh

enum class TransportType {
    BLE,          // Low energy, long battery, text & control (~10-50 kbps)
    WIFI_DIRECT,  // High speed, low latency, voice & file transfers (~50-250 Mbps)
    INTERNET_FALLBACK // Optional backup
}

enum class PacketPayloadType {
    CHAT_TEXT,
    CHAT_VOICE_CHUNK,
    CHAT_FILE_CHUNK,
    ROUTE_ANNOUNCE,
    CONTROL_HANDSHAKE,
    VOICE_CALL_SIGNAL,
    VOICE_STREAM_DATA,
    DELIVERY_ACK,
    PING_HEARTBEAT,
    SYSTEM_COMMAND
}
