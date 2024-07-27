package com.abdur.audioplayer

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class PlayerManager(
    context : Context
) {
    val player = ExoPlayer.Builder(context).build()
}