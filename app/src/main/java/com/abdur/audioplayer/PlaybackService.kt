package com.abdur.audioplayer

import android.media.MediaSession2
import android.media.MediaSession2Service
import android.media.session.MediaSessionManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.launch

class PlaybackService(
    private var viewmodel : AudioViewModel = AudioViewModel(),
) : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private var audioProvider: AudioProvider = AudioProvider(this)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        val exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
        viewmodel.viewModelScope.launch {
            val audios = audioProvider.getAudioList()
            viewmodel.updateAudios(audios)
        }
        val audioList = viewmodel.audioList
        audioList.forEach {
            val mediaItem = MediaItem.fromUri(it.uri!!)
            exoPlayer.addMediaItem(mediaItem)
        }
        exoPlayer.prepare()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession
}