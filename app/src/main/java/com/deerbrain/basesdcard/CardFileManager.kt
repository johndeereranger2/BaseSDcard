package com.deerbrain.basesdcard

import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class CardFileManager {
    //this is the file that will communicate directly with the SD card


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

}