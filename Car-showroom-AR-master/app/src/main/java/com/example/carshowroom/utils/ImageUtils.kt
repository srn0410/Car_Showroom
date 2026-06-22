package com.example.carshowroom.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaActionSound
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.PixelCopy
import android.view.Window
import android.widget.Toast
import java.io.OutputStream

object ImageUtils {

    private fun findSurfaceView(view: android.view.View): android.view.SurfaceView? {
        if (view is android.view.SurfaceView) return view
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val found = findSurfaceView(view.getChildAt(i))
                if (found != null) return found
            }
        }
        return null
    }

    fun captureArSnapshot(context: Context, window: Window) {
        // Play shutter sound
        val sound = MediaActionSound()
        sound.play(MediaActionSound.SHUTTER_CLICK)

        val surfaceView = findSurfaceView(window.decorView)

        if (surfaceView != null) {
            val bitmap = Bitmap.createBitmap(surfaceView.width, surfaceView.height, Bitmap.Config.ARGB_8888)
            PixelCopy.request(surfaceView, bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    saveBitmapAndShare(context, bitmap)
                } else {
                    Toast.makeText(context, "Failed to capture snapshot.", Toast.LENGTH_SHORT).show()
                }
            }, Handler(Looper.getMainLooper()))
        } else {
            val bitmap = Bitmap.createBitmap(window.decorView.width, window.decorView.height, Bitmap.Config.ARGB_8888)
            PixelCopy.request(window, bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    saveBitmapAndShare(context, bitmap)
                } else {
                    Toast.makeText(context, "Failed to capture snapshot.", Toast.LENGTH_SHORT).show()
                }
            }, Handler(Looper.getMainLooper()))
        }
    }

    private fun saveBitmapAndShare(context: Context, bitmap: Bitmap) {
        val filename = "CarShowroom_${System.currentTimeMillis()}.png"
        var imageUri: Uri? = null
        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CarShowroom")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        try {
            imageUri = resolver.insert(collection, contentValues)
            imageUri?.let { uri ->
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                outputStream?.use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }

                shareImage(context, uri)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareImage(context: Context, uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Car via..."))
    }
}
