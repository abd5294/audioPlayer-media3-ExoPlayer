package com.abdur.audioplayer

import android.net.Uri

data class Audio(
    var id : Long? = null,
    val name : String,
    var uri : Uri? = null
)