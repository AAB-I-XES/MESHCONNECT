package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ContactEntity
import com.example.data.database.ConversationEntity
import com.example.data.database.MessageEntity
import com.example.data.database.MeshDatabase
import com.example.data.database.UserEntity
import com.example.mesh.MeshDiscoveryManager
import com.example.mesh.MeshPacket
import com.example.mesh.MeshRoutingEngine
import com.example.mesh.MeshSecurityEngine
import com.example.mesh.NetworkMonitor
import com.example.mesh.PacketPayloadType
import com.example.mesh.RouteStrategy
import com.example.mesh.TransportManager
import com.example.security.CryptoManager
import com.example.voice.MeshVoiceCallManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MeshLinkViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MeshDatabase.getDatabase(application)
    val meshRoutingEngine = MeshRoutingEngine(application)
    val discoveryManager = MeshDiscoveryManager(application, meshRoutingEngine.bleManager, meshRoutingEngine.wifiDirectManager)
    val transportManager = TransportManager(application)
    val securityEngine = MeshSecurityEngine()
    val networkMonitor = NetworkMonitor(application)
    val voiceCallManager = MeshVoiceCallManager()

    val currentUser: StateFlow<UserEntity?> = db.userDao().getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val contacts: StateFlow<List<ContactEntity>> = db.contactDao().getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<ConversationEntity>> = db.conversationDao().getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routes = db.routeDao().getAllRoutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val packetLogs = db.packetLogDao().getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedConversationId = MutableStateFlow<String?>(null)
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val currentMessages: StateFlow<List<MessageEntity>> = _selectedConversationId.flatMapLatest { convId ->
        if (convId == null) flowOf(emptyList())
        else db.messageDao().getMessagesForConversation(convId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<MessageEntity>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) flowOf(emptyList())
        else db.messageDao().searchMessages(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        ensureInitialSetup()
    }

    fun startHardwareNetworking() {
        meshRoutingEngine.startHardwareEngine()
    }

    private fun ensureInitialSetup() {
        viewModelScope.launch {
            val user = db.userDao().getUserOnce()
            if (user == null) {
                val (pub, priv) = CryptoManager.generateKeyPair()
                val meshId = CryptoManager.generateMeshId(pub)
                val newUser = UserEntity(
                    id = 1,
                    meshId = meshId,
                    displayName = "Alex (Self Node)",
                    publicKey = pub,
                    privateKey = priv,
                    bio = "Offline mesh node ready for P2P connection",
                    avatarId = 1
                )
                db.userDao().insertUser(newUser)
                meshRoutingEngine.myMeshId = meshId
                meshRoutingEngine.myName = newUser.displayName
            } else {
                meshRoutingEngine.myMeshId = user.meshId
                meshRoutingEngine.myName = user.displayName
            }
        }
    }

    fun selectConversation(conversationId: String?) {
        _selectedConversationId.value = conversationId
        if (conversationId != null) {
            viewModelScope.launch {
                db.conversationDao().markAsRead(conversationId)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun sendTextMessage(conversationId: String, text: String, recipientMeshId: String, expirySec: Int = 0) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val msgId = "msg_" + UUID.randomUUID().toString().take(8)
            val expiryTime = if (expirySec > 0) System.currentTimeMillis() + (expirySec * 1000L) else 0L

            val targetRecipientId = if (recipientMeshId.isNotBlank() && recipientMeshId != "mesh_peer") {
                recipientMeshId
            } else if (conversationId.startsWith("conv_")) {
                conversationId.removePrefix("conv_")
            } else {
                recipientMeshId
            }

            val encryptedPayload = securityEngine.encryptPayload(text)

            val newMsg = MessageEntity(
                messageId = msgId,
                conversationId = conversationId,
                senderMeshId = user.meshId,
                recipientMeshId = targetRecipientId,
                content = text,
                timestamp = System.currentTimeMillis(),
                status = "SENDING",
                isEncrypted = true,
                routeHops = 1,
                expiryTimestamp = expiryTime,
                messageType = "TEXT"
            )
            db.messageDao().insertMessage(newMsg)

            // Ensure conversation exists or update last message
            var conv = db.conversationDao().getConversationById(conversationId)
            if (conv == null) {
                val contact = db.contactDao().getContactById(targetRecipientId)
                val peerTitle = contact?.displayName ?: if (targetRecipientId.startsWith("mesh_")) "Mesh Peer (${targetRecipientId.take(12)})" else "Mesh Peer"
                conv = ConversationEntity(
                    conversationId = conversationId,
                    title = peerTitle,
                    lastMessage = "You: $text",
                    lastTimestamp = System.currentTimeMillis(),
                    unreadCount = 0,
                    isGroup = false,
                    isPinned = false,
                    participantMeshIds = targetRecipientId
                )
                db.conversationDao().insertConversation(conv)
            } else {
                db.conversationDao().insertConversation(
                    conv.copy(lastMessage = "You: $text", lastTimestamp = System.currentTimeMillis())
                )
            }

            // Transmit over real BLE / Wi-Fi Direct hardware connections!
            val packet = MeshPacket(
                packetId = "pkt_$msgId",
                sourceMeshId = user.meshId,
                destinationMeshId = targetRecipientId,
                payloadType = PacketPayloadType.CHAT_TEXT,
                encryptedData = encryptedPayload,
                ttl = 7,
                hopCount = 1
            )
            val success = meshRoutingEngine.routeAndSendPacket(packet)
            if (success) {
                db.messageDao().updateStatus(msgId, "DELIVERED")
            }
        }
    }

    fun sendVoiceNote(conversationId: String, recipientMeshId: String, durationSec: Int) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val msgId = "msg_v_" + UUID.randomUUID().toString().take(8)
            val targetRecipientId = if (recipientMeshId.isNotBlank() && recipientMeshId != "mesh_peer") {
                recipientMeshId
            } else if (conversationId.startsWith("conv_")) {
                conversationId.removePrefix("conv_")
            } else {
                recipientMeshId
            }

            val newMsg = MessageEntity(
                messageId = msgId,
                conversationId = conversationId,
                senderMeshId = user.meshId,
                recipientMeshId = targetRecipientId,
                content = "Voice Message ($durationSec sec)",
                timestamp = System.currentTimeMillis(),
                status = "DELIVERED",
                isEncrypted = true,
                routeHops = 1,
                messageType = "VOICE",
                voiceDurationSec = durationSec
            )
            db.messageDao().insertMessage(newMsg)

            val packet = MeshPacket(
                packetId = "pkt_$msgId",
                sourceMeshId = user.meshId,
                destinationMeshId = targetRecipientId,
                payloadType = PacketPayloadType.CHAT_VOICE_CHUNK,
                encryptedData = "OPUS_VOICE_ENCRYPTED_STREAM_CHUNK_DATA",
                ttl = 7,
                hopCount = 1
            )
            meshRoutingEngine.routeAndSendPacket(packet)
        }
    }

    fun sendFile(conversationId: String, recipientMeshId: String, fileName: String, fileSizeMb: Double) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val msgId = "msg_f_" + UUID.randomUUID().toString().take(8)
            val targetRecipientId = if (recipientMeshId.isNotBlank() && recipientMeshId != "mesh_peer") {
                recipientMeshId
            } else if (conversationId.startsWith("conv_")) {
                conversationId.removePrefix("conv_")
            } else {
                recipientMeshId
            }

            val sizeBytes = (fileSizeMb * 1024 * 1024).toLong()
            val newMsg = MessageEntity(
                messageId = msgId,
                conversationId = conversationId,
                senderMeshId = user.meshId,
                recipientMeshId = targetRecipientId,
                content = "Shared file: $fileName (${"%.1f".format(fileSizeMb)} MB)",
                timestamp = System.currentTimeMillis(),
                status = "DELIVERED",
                isEncrypted = true,
                routeHops = 1,
                messageType = "FILE",
                mediaName = fileName,
                mediaSize = sizeBytes
            )
            db.messageDao().insertMessage(newMsg)

            val packet = MeshPacket(
                packetId = "pkt_$msgId",
                sourceMeshId = user.meshId,
                destinationMeshId = targetRecipientId,
                payloadType = PacketPayloadType.CHAT_FILE_CHUNK,
                encryptedData = "FILE_CHUNK_DATA_AES_256",
                ttl = 7,
                hopCount = 1
            )
            meshRoutingEngine.routeAndSendPacket(packet)
        }
    }

    fun addReaction(messageId: String, reaction: String) {
        viewModelScope.launch {
            db.messageDao().updateReaction(messageId, reaction)
        }
    }

    fun togglePinMessage(messageId: String, currentPinned: Boolean) {
        viewModelScope.launch {
            db.messageDao().updatePinned(messageId, !currentPinned)
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        viewModelScope.launch {
            db.messageDao().editMessage(messageId, newContent)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            db.messageDao().deleteMessage(messageId)
        }
    }

    fun clearConversation(conversationId: String) {
        viewModelScope.launch {
            db.messageDao().clearConversationMessages(conversationId)
        }
    }

    fun startVoiceCall(peerMeshId: String, peerName: String, isWalkieTalkie: Boolean = false) {
        voiceCallManager.startCall(peerMeshId, peerName, isWalkieTalkie = isWalkieTalkie)
    }

    fun addContactFromQr(qrData: String) {
        val parsed = CryptoManager.parseQrPayload(qrData) ?: return
        val (meshId, displayName, pubKey) = parsed
        viewModelScope.launch {
            val contact = ContactEntity(
                meshId = meshId,
                displayName = displayName,
                publicKey = pubKey,
                status = "ONLINE",
                hopsAway = 1,
                rssi = -55,
                transportType = "BLE"
            )
            db.contactDao().insertContact(contact)
        }
    }

    fun updateRouteStrategy(strategy: RouteStrategy) {
        meshRoutingEngine.setStrategy(strategy)
    }

    fun injectDiagnosticPacket(targetMeshId: String, type: PacketPayloadType) {
        viewModelScope.launch {
            meshRoutingEngine.injectDiagnosticPacket(targetMeshId, type)
        }
    }

    fun updateUserProfile(name: String, bio: String) {
        viewModelScope.launch {
            val current = currentUser.value ?: return@launch
            val updated = current.copy(displayName = name, bio = bio)
            db.userDao().insertUser(updated)
            meshRoutingEngine.myName = name
        }
    }
}
