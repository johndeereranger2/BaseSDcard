package com.deerbrain.basesdcard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.cardreader.*
import java.io.File

class CardReader : AppCompatActivity() {

    var directory: String = ""
    private val dataSource = CardReaderAdapter()
    private var itemsFromSDCard = mutableListOf<FileItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cardreader)

        updateRecyclerView()

    }

    private fun updateRecyclerView() {
//        var itemtwo = FileItem(
//            fileName = "File Name Zero",
//            fileDateString = "filezeroDatestring"
//        )
//        itemsFromSDCard.add(itemtwo)

        itemsFromSDCard = CardFileManager().getFilesWithPath("")
        //updateDisplayItems(directory)
        CardReaderItemList.layoutManager = LinearLayoutManager(this)
        val cardReaderInfo = CardReaderAdapter()
        CardReaderItemList.adapter = cardReaderInfo
        cardReaderInfo.update(itemsFromSDCard)

    }

    private fun updateDisplayItems(path: String){
        val basePath = "baseCardPath/"
        var pathToShow =  basePath + path

        itemsFromSDCard = CardFileManager().getFilesWithPath(pathToShow)
    }

}