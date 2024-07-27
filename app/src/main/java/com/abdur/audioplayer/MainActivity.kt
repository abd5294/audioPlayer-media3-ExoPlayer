package com.abdur.audioplayer

import android.Manifest
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Global
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 0
        )
        lateinit var controller: MediaController
        val viewmodel by viewModels<AudioViewModel>()
        val audioProvider = AudioProvider(this)
        var currentAudioIndex : Int = 0

        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controlFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controlFuture.addListener({
            controller = controlFuture.get()
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    viewmodel.isAudioPlaying.value = isPlaying
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    currentAudioIndex = controller.currentMediaItemIndex
                }
            }
            controller.addListener(listener)
        }, MoreExecutors.directExecutor())

        viewmodel.viewModelScope.launch {
            val audioList = audioProvider.getAudioList()
            viewmodel.updateAudios(audioList)
        }

        setContent {

            val bgColors = listOf(
                Color(181, 247, 247, 200),
                Color(248, 216, 184, 205),
                Color(242, 247, 184, 255),
                Color(213, 252, 185, 255),
                Color(187, 252, 209, 255),
                Color(189, 255, 255, 255),
                Color(187, 211, 253, 255),
                Color(211, 184, 250, 255),
                Color(250, 197, 242, 255),
                Color(248, 197, 197, 255),
            )

            var colorState by remember {
                mutableIntStateOf(0)
            }
            val animatedColors = animateColorAsState(
                targetValue = bgColors[colorState],
                label = "background colors",
                animationSpec = tween(2000)
            )

            LaunchedEffect(key1 = colorState) {
                delay(2000)
                if (colorState < bgColors.size - 1) colorState += 1 else colorState = 0
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                containerColor = Color.DarkGray
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(viewmodel.audioList) { index, audio ->
                            AudioItem(audio = audio) {
                                controller.seekTo(index, 0)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Color(184, 184, 184, 255)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                modifier = Modifier.padding(top = 12.dp),
                                text = viewmodel.audioList[currentAudioIndex].name.dropLast(4),
                                fontSize = 20.sp,
                                fontFamily = FontFamily(Font(R.font.abel_regular))
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(
                                onClick = {
                                    controller.seekToPrevious()
                                    Log.d("jazila", controller.currentPosition.toString())
                                },
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(
                                        CircleShape
                                    )
                                    .background(Color.DarkGray)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_skip_previous_24),
                                    contentDescription = "previous",
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewmodel.isAudioPlaying.value = !viewmodel.isAudioPlaying.value
                                    if (viewmodel.isAudioPlaying.value) controller.play() else controller.pause()
                                },
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(
                                        CircleShape
                                    )
                                    .background(Color.DarkGray)
                            ) {
                                if (viewmodel.isAudioPlaying.value) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_pause_24),
                                        contentDescription = "pause",
                                        modifier = Modifier.size(40.dp),
                                        tint = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "play",
                                        modifier = Modifier.size(40.dp),
                                        tint = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    controller.seekToNext()
                                },
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(
                                        CircleShape
                                    )
                                    .background(Color.DarkGray)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_skip_next_24),
                                    contentDescription = "next",
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}