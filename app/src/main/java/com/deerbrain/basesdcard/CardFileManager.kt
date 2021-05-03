package com.deerbrain.basesdcard

import android.media.Image
import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class CardFileManager {
    //this is the file that will communicate directly with the SD card


//    fun getFilesWithPath(path: String) : MutableList<FileItem> {
//
//        var itemsFromSDCard = mutableListOf<FileItem>()
//
//        //Do work to populate itemsFromSDCard
//
//        return itemsFromSDCard
//    }

    var parentDirectory:

    fun getFilesWithPath(path: String) : MutableList<FileItem> {
        var itemsFromSDCard = mutableListOf<FileItem>()
        var date = Date()
        val formatter = SimpleDateFormat("MMM dd yyyy")

        var itemOne = FileItem(
            fileName = "FileOneName.JPG",
            fileDateString = "Date String One"

        )
        itemsFromSDCard.add(itemOne)

        return itemsFromSDCard
    }

    fun getPicsAtPath(path: String) : MutableList<FileItem> {
        //goes through card and builds up an array of FileItems that are only pictures

    }

    fun getMoviesAtPath(path: String) : MutableList<FileItem> {
        //goes through card and builds up an array of FileItems that are only Movies

    }


    fun receivedDeviceStatusChanged(status: DeviceStatus) {
        //this may take a parameter

        when (status) {
            success -> {
                //notify status is connected
            }
            errorStatus -> {
                //reportouterr
            }
            disconnect -> {
                //report disconnection
            }
        }

    }

    fun removeFilesFromPaths(paths: ArrayList<String>) -> Boolean {
        //remove multiple files from the SD card reader and return true if successful or return some value of success or error
    }


    fun removeFileFromPath(path: String) -> Bool {
        //remove single file from SD card Reader
    }

    fun showImage(isBig: Boolean = false, fileName: String, atDirectory: String) : Image {
        // this will take a file name at a directory and return an image.

        //if isBIG is true return full size image
        //if isBig is false return a rezied Image to CGSize of 60x60

    }
}