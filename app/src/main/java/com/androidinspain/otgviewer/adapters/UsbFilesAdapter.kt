package com.androidinspain.otgviewer.adapters

import android.content.Context
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidinspain.otgviewer.R
import com.androidinspain.otgviewer.fragments.ExplorerFragment
import com.androidinspain.otgviewer.recyclerview.RecyclerItemClickListener
import com.androidinspain.otgviewer.util.IconUtils
import com.androidinspain.otgviewer.util.Utils
import com.github.mjdev.libaums.fs.UsbFile
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UsbFilesAdapter(
    private val mContext: Context,
    var currentDir: UsbFile,
    private val mRecyclerItemClickListener: RecyclerItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val DEBUG = false
    private val TAG = javaClass.simpleName
    var files: List<UsbFile?>
        private set
    private val mInflater: LayoutInflater

    inner class MyViewHolder(var globalView: View) : RecyclerView.ViewHolder(globalView) {
        var title: TextView
        var summary: TextView
        var type: ImageView

        init {
            title = globalView.findViewById<View>(android.R.id.title) as TextView
            summary = globalView.findViewById<View>(android.R.id.summary) as TextView
            type =
                globalView.findViewById<View>(android.R.id.icon) as ImageView
            globalView.setOnKeyListener { view, keyCode, keyEvent ->
                var handled = false
                Log.d(TAG, "onKey $keyEvent")
                handled = mRecyclerItemClickListener.handleDPad(view, keyCode, keyEvent)
                handled
            }
        }
    }

    @Throws(IOException::class)
    fun refresh() {
        files = Arrays.asList(*currentDir.listFiles())
        if (DEBUG) {
            Log.d(TAG, "files size: " + files.size)
            Log.d(
                TAG,
                "REFRESH.  mSortByCurrent: " + ExplorerFragment.mSortByCurrent + ", mSortAsc: " + ExplorerFragment.mSortAsc
            )
        }
        Collections.sort(files, Utils.comparator)
        if (!ExplorerFragment.mSortAsc) Collections.reverse(files)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View
        itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        position: Int
    ) {

        //if(DEBUG)
        //    Log.d(TAG, "onBindViewHolder. Position: " + position);
        if (viewHolder is MyViewHolder) {
            val file = files[position]
            val holder = viewHolder
            if (file!!.isDirectory) {
                holder.type.setImageResource(R.drawable.ic_folder_alpha)
            } else {
                val index = file.name.lastIndexOf(".")
                if (index > 0) {
                    val prefix = file.name.substring(0, index)
                    val ext = file.name.substring(index + 1)
                    //if (DEBUG)
                    //    Log.d(TAG, "mimetype: " + Utils.getMimetype(ext.toLowerCase()) + ". ext is: " + ext);
                    holder.type.setImageResource(
                        IconUtils.loadMimeIcon(
                            Utils.getMimetype(
                                ext.toLowerCase()
                            )
                        )
                    )
                }
            }
            holder.title.text = file.name
            val date_format = SimpleDateFormat.getDateInstance(
                SimpleDateFormat.SHORT,
                Locale.getDefault()
            )
            val date = date_format.format(Date(file.lastModified()))

            // If it's a directory, we can't get size info
            try {
                holder.summary.text = "Last modified: " + date + " - " +
                        Formatter.formatFileSize(mContext, file.length)
            } catch (e: Exception) {
                holder.summary.text = "Last modified: $date"
            }
        }
    }

    override fun getItemCount(): Int {
        return files.size
    }

    fun getItem(position: Int): UsbFile? {
        return files[position]
    }

    val imageFiles: ArrayList<UsbFile?>
        get() {
            val result = ArrayList<UsbFile?>()
            for (file in files) {
                if (Utils.isImage(file!!)) result.add(file)
            }
            return result
        }

    init {
        files = ArrayList()
        mInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        refresh()
    }
}