package com.example.voice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CallState {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTED,
    WALKIE_TALKIE_ACTIVE,
    ENDED
}

data class CallSession(
    val callId: String,
    val peerMeshId: String,
    val peerName: String,
    val isGroupCall: Boolean = false,
    val state: CallState = CallState.IDLE,
    val durationSec: Int = 0,
    val bitrateKbps: Int = 48,
    val transport: String = "Wi-Fi Direct (5GHz)",
    val codec: String = "Opus 48kHz Stereo",
    val echoCancellation: Boolean = true,
    val noiseSuppression: Boolean = true,
    val isVoiceActive: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeakerphone: Boolean = true,
    val isPushToTalk: Boolean = false,
    val isTransmittingAudio: Boolean = false
)

class MeshVoiceCallManager {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _currentCall = MutableStateFlow<CallSession?>(null)
    val currentCall: StateFlow<CallSession?> = _currentCall.asStateFlow()

    private val _audioWaveform = MutableStateFlow<List<Float>>(List(24) { 0.1f })
    val audioWaveform: StateFlow<List<Float>> = _audioWaveform.asStateFlow()

    fun startCall(peerMeshId: String, peerName: String, isGroup: Boolean = false, isWalkieTalkie: Boolean = false) {
        val callId = "call_" + System.currentTimeMillis()
        val session = CallSession(
            callId = callId,
            peerMeshId = peerMeshId,
            peerName = peerName,
            isGroupCall = isGroup,
            state = if (isWalkieTalkie) CallState.WALKIE_TALKIE_ACTIVE else CallState.OUTGOING_RINGING,
            isPushToTalk = isWalkieTalkie
        )
        _currentCall.value = session

        scope.launch {
            if (!isWalkieTalkie) {
                delay(2000) // Simulate ringing
                if (_currentCall.value?.callId == callId) {
                    _currentCall.value = _currentCall.value?.copy(state = CallState.CONNECTED)
                    startTimerAndWaveform(callId)
                }
            } else {
                startTimerAndWaveform(callId)
            }
        }
    }

    fun acceptCall() {
        val current = _currentCall.value ?: return
        _currentCall.value = current.copy(state = CallState.CONNECTED)
        startTimerAndWaveform(current.callId)
    }

    fun endCall() {
        _currentCall.value = _currentCall.value?.copy(state = CallState.ENDED)
        scope.launch {
            delay(500)
            _currentCall.value = null
        }
    }

    fun toggleMute() {
        val current = _currentCall.value ?: return
        _currentCall.value = current.copy(isMuted = !current.isMuted)
    }

    fun toggleSpeaker() {
        val current = _currentCall.value ?: return
        _currentCall.value = current.copy(isSpeakerphone = !current.isSpeakerphone)
    }

    fun setPushToTalkTransmitting(isTransmitting: Boolean) {
        val current = _currentCall.value ?: return
        _currentCall.value = current.copy(
            isTransmittingAudio = isTransmitting,
            isVoiceActive = isTransmitting
        )
    }

    private fun startTimerAndWaveform(callId: String) {
        scope.launch {
            var seconds = 0
            while (_currentCall.value?.callId == callId && (_currentCall.value?.state == CallState.CONNECTED || _currentCall.value?.state == CallState.WALKIE_TALKIE_ACTIVE)) {
                delay(1000)
                seconds++
                // Randomize audio wave heights for call visualization
                val waves = List(24) {
                    if (_currentCall.value?.isMuted == true) 0.05f
                    else (0.1f..0.95f).random()
                }
                _audioWaveform.value = waves

                val newBitrate = (32..64).random()
                _currentCall.value = _currentCall.value?.copy(
                    durationSec = seconds,
                    bitrateKbps = newBitrate,
                    isVoiceActive = waves.average() > 0.3
                )
            }
        }
    }

    private fun ClosedRange<Float>.random() = (start + Math.random() * (endInclusive - start)).toFloat()
}
