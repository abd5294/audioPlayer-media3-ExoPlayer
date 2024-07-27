package com.abdur.audioplayer

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import kotlinx.coroutines.launch

class AudioViewModel : ViewModel() {

    var audioList : List<Audio> = mutableListOf()
        private set

    fun updateAudios(audios : List<Audio>){
        this.audioList = audios
    }

    var isAudioPlaying = mutableStateOf(false)

    var title by mutableStateOf("")

    var isFirstAudioPlaying by mutableStateOf(true)
}