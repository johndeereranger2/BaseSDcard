package com.deerbrain.basesdcard

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class CardReaderAdapter(): RecyclerView.Adapter<FileCell>() {

    var displayItems = mutableListOf<FileItem>()

    private var onClickListener: View.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileCell {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        return displayItems.size
    }

    override fun onBindViewHolder(holder: FileCell, position: Int) {
        val item = displayItems[position]

        holder.fileLabel.text = item.fileName
        holder.fileDate.text = item.fileDateString
        //holder.fileImage.image = item.fileImage

        holder.setOnClickListener {
            didSelectItem(item)
        }
    }


    fun didSelectItem(item: FileItem) {
        val fileExtensionType: item.fileName //gets extension of file name like jpg/mov etc


        when (fileExtensionType) {
            "JPG" -> {
                val intent = Intent(this, ImageViewer::class.java)
                startActivity(intent)
            }
            "No File Extension" -> {
                //if this is the case then user is opening a folder in the explorer
                val intent = Intent(this, CardReader::class.java)
                //need to pass in the path of this folder that you opening
                startActivity(intent)
            }
            "MOV" -> {
//                val intent = Intent(this, MovieViewer::class.java)
//                startActivity(intent)
            }
            "anything Else" -> {
               // Toast.makeText(this,"Currently not available", Toast.LENGTH_SHORT).show()
            }

        }
    }

}