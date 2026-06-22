package com.example.carshowroom.audio

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var player: ExoPlayer? = null

    fun initialize() {
        if (player != null) return
        player = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            // Using the BMW XM sound as it's the most complete recording
            val mediaItem = MediaItem.fromUri("asset:///audio/mizanstock-bmw-xm-car-sound-2023-165995.mp3")
            setMediaItem(mediaItem)
            prepare()
        }
    }

    fun start() {
        player?.playWhenReady = true
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    fun setPitch(pitch: Float) {
        player?.playbackParameters = PlaybackParameters(pitch)
    }

    fun setVolume(volume: Float) {
        player?.volume = volume
    }

    fun release() {
        player?.release()
        player = null
    }
}
