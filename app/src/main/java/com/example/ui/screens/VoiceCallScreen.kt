package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MeshLinkViewModel
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.voice.CallState

@Composable
fun VoiceCallScreen(
    viewModel: MeshLinkViewModel,
    onEndCallClick: () -> Unit
) {
    val callSession by viewModel.voiceCallManager.currentCall.collectAsState()
    val waveform by viewModel.voiceCallManager.audioWaveform.collectAsState()

    val session = callSession ?: return

    val statusText = when (session.state) {
        CallState.OUTGOING_RINGING -> "Calling via Wi-Fi Direct Mesh..."
        CallState.INCOMING_RINGING -> "Incoming Mesh Voice Call..."
        CallState.WALKIE_TALKIE_ACTIVE -> "Walkie-Talkie Active (Push To Talk)"
        CallState.CONNECTED -> "Connected • ${formatCallTime(session.durationSec)}"
        CallState.ENDED -> "Call Ended"
        else -> "Connecting..."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .systemBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(CyberCyan, ElectricBlue))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = session.peerName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        color = NavyBackground
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = session.peerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    color = CyberCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "E2EE Opus 48kHz • ${session.bitrateKbps} kbps • Wi-Fi Direct",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Waveform Display
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(DarkSurface, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp)
            ) {
                waveform.forEach { heightRatio ->
                    val h = (heightRatio * 60).dp
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(h)
                            .clip(CircleShape)
                            .background(if (session.isVoiceActive) CyberCyan else TextSecondary.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Push To Talk or Standard Call Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (session.isPushToTalk) {
                    Surface(
                        color = if (session.isTransmittingAudio) NeonAmber else DarkSurface,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(88.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        viewModel.voiceCallManager.setPushToTalkTransmitting(true)
                                        tryAwaitRelease()
                                        viewModel.voiceCallManager.setPushToTalkTransmitting(false)
                                    }
                                )
                            }
                            .testTag("push_to_talk_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Radio,
                                    contentDescription = "Hold to Speak",
                                    tint = if (session.isTransmittingAudio) NavyBackground else CyberCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = if (session.isTransmittingAudio) "TRANSMITTING" else "HOLD TO TALK",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (session.isTransmittingAudio) NavyBackground else TextSecondary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Action Bar
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { viewModel.voiceCallManager.toggleMute() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                    ) {
                        Icon(
                            imageVector = if (session.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (session.isMuted) CoralRed else TextPrimary
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.voiceCallManager.endCall()
                            onEndCallClick()
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(CoralRed)
                            .testTag("end_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.voiceCallManager.toggleSpeaker() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaker",
                            tint = if (session.isSpeakerphone) CyberCyan else TextPrimary
                        )
                    }
                }
            }
        }
    }
}

fun formatCallTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
