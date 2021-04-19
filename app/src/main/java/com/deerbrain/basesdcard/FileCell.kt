package com.deerbrain.basesdcard

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.file_tableviewcell.view.*

class FileCell(termView: View):RecyclerView.ViewHolder(termView) {
    val fileLabel: TextView = termView.fileLabel
    val fileDate: TextView = termView.fileTimeStampLabel
}