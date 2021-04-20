package com.deerbrain.basesdcard.Maps

import android.util.Log
import com.deerbrain.basesdcard.utilities.App
import com.google.android.gms.maps.model.Tile
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MapCacheManager {


    companion object {
        val context = App.context
        private fun store(x: Int, y: Int, z: Int): Tile {
            val folder = File("${context.cacheDir}/tiles");
            if (!folder.exists()) {
                folder.mkdir()
            }

            val file = File("${context.cacheDir}/tiles/${x}_${y}_${z}.tile")
            try {
                val inputStream = getTileUrl(x, y, z)?.readBytes()
                val fileOutputStream = FileOutputStream(file)

                fileOutputStream.write(inputStream)

                fileOutputStream.flush()
                fileOutputStream.close()
            } catch (e: Exception) {
                Log.d("TAG", "getTile: " + e.message)
            }

            return Tile(x, y, file.readBytes())
        }

        private fun getTileUrl(x: Int, y: Int, z: Int): URL? {
            return URL("https://mt1.google.com/vt/lyrs=y&x=${x}&y=${y}&z=${z}")
        }

        fun getTile(x: Int, y: Int, z: Int): Tile {
            val file = File("${context.cacheDir}/tiles/${x}_${y}_${z}.tile")
            return if (file.exists()) {
                Tile(x, y, file.readBytes())
            } else {
                store(x, y, z)
            }
        }
    }


}