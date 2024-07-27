package com.abdur.audioplayer

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi

class AudioProvider(
    private var context: Context,
) {
    private var audioList = mutableListOf<Audio>()
    private var cursor: Cursor? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAudioList(): List<Audio> {

        val projection = arrayOf(
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media._ID
        )

        cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null
        )
        cursor?.use { cursor ->
            val nameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn)
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val audio = Audio(
                    id = id,
                    name = name,
                    uri = contentUri,
                )
                audioList.add(audio)
            }
        }
        return audioList
    }
}