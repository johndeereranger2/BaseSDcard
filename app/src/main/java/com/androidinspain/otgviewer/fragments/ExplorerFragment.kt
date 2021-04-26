package com.androidinspain.otgviewer.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidinspain.otgviewer.ImageViewer
import com.androidinspain.otgviewer.ImageViewerActivity
import com.androidinspain.otgviewer.R
import com.androidinspain.otgviewer.adapters.UsbFilesAdapter
import com.androidinspain.otgviewer.recyclerview.EmptyRecyclerView
import com.androidinspain.otgviewer.recyclerview.RecyclerItemClickListener
import com.androidinspain.otgviewer.task.CopyTaskParam
import com.androidinspain.otgviewer.util.Constants
import com.androidinspain.otgviewer.util.Utils
import com.github.mjdev.libaums.UsbMassStorageDevice
import com.github.mjdev.libaums.fs.UsbFile
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
class ExplorerFragment : Fragment() {
    private val TAG = javaClass.simpleName
    private val DEBUG = false
    private var mMainActivity: ExplorerCallback? = null
    private var mSelectedDevice: UsbMassStorageDevice? = null

    /* package */
    var mAdapter: UsbFilesAdapter? = null
    private val dirs: Deque<UsbFile> = ArrayDeque()
    private var mEmptyView: LinearLayout? = null
    private var mErrorView: TextView? = null
    private var mIsShowcase = false
    private var mError = false
    private var mRecyclerView: EmptyRecyclerView? = null
    private var mRecyclerItemClickListener: RecyclerItemClickListener? = null

    // Sorting related
    private var mSortByLL: LinearLayout? = null
    private var mFilterByTV: Button? = null
    private var mOrderByIV: ImageButton? = null
    private val REQUEST_IMAGEVIEWER = 0
    private val REQUEST_FOCUS = 0
    private val REQUEST_FOCUS_DELAY = 200 //ms
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                REQUEST_FOCUS -> if (mRecyclerView != null) mRecyclerView!!.requestFocus()
            }
        }
    }

    interface ExplorerCallback {
        fun setABTitle(title: String?, showMenu: Boolean)
        val coordinatorLayout: CoordinatorLayout?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_showcase).isVisible = true
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings ->                 // Not implemented here
                return false
            R.id.action_showcase -> {
                // This is only handled in this fragment
                checkShowcase()
                return true
            }
            else -> {
            }
        }
        return false
    }

    private fun checkShowcase() {
        if (mError) return
        val directory = mAdapter!!.currentDir
        if (DEBUG) Log.d(
            TAG,
            "Checking showcase. Current directory: " + directory.isDirectory
        )
        var available = false
        val files: List<UsbFile?>
        try {
            files = mAdapter!!.files
            var firstImageIndex = 0
            for (file in files) {
                if (Utils.isImage(file!!)) {
                    available = true
                    break
                }
                firstImageIndex++
            }
            if (available && !files.isEmpty()) {
                mIsShowcase = true
                copyFileToCache(files[firstImageIndex])
            } else {
                Snackbar.make(
                    mMainActivity!!.coordinatorLayout!!,
                    getString(R.string.toast_no_images), Snackbar.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openFilterDialog() {
        val alertDialogBuilder =
            AlertDialog.Builder(activity!!)
        alertDialogBuilder.setTitle(getString(R.string.sort_by))
        alertDialogBuilder.setItems(R.array.sortby) { dialog, which ->
            mSortByCurrent = which
            updateSortUI(true)
            dialog.dismiss()
        }
        val ad = alertDialogBuilder.create()
        ad.show()
    }

    private fun doSort() {
        saveSortFilter()
        doRefresh()
    }

    private fun doRefresh(entry: UsbFile?) {
        mAdapter!!.currentDir = entry!!
        doRefresh()
    }

    private fun doRefresh() {
        try {
            if (mAdapter != null) mAdapter!!.refresh()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mRecyclerView!!.scrollToPosition(0)
        mHandler.sendEmptyMessageDelayed(REQUEST_FOCUS, REQUEST_FOCUS_DELAY.toLong())
    }

    private fun saveSortFilter() {
        val sharedPref = activity!!.getSharedPreferences(
            Constants.SORT_FILTER_PREF,
            Context.MODE_PRIVATE
        )
        val editor = sharedPref.edit()
        editor.putInt(
            Constants.SORT_FILTER_KEY,
            mSortByCurrent
        )
        editor.putBoolean(
            Constants.SORT_ASC_KEY,
            mSortAsc
        )
        editor.commit()
    }

    private fun orderByTrigger() {
        mSortAsc = !mSortAsc
        updateSortUI(true)
    }

    private fun updateSortUI(doSort: Boolean) {
        mSortByLL!!.visibility = View.VISIBLE
        mFilterByTV!!.text = Utils.getHumanSortBy(activity!!)
        if (mSortAsc) mOrderByIV!!.setImageResource(R.drawable.sort_order_asc) else mOrderByIV!!.setImageResource(
            R.drawable.sort_order_desc
        )
        if (doSort) doSort()
    }

     override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView =
            inflater.inflate(R.layout.fragment_explorer, container, false)
        val sharedPref = activity!!.getSharedPreferences(
            Constants.SORT_FILTER_PREF
            , Context.MODE_PRIVATE
        )
        mSortAsc =
            sharedPref.getBoolean(Constants.SORT_ASC_KEY, true)
        mSortByCurrent =
            sharedPref.getInt(Constants.SORT_FILTER_KEY, 0)
        mRecyclerView = rootView.findViewById<View>(R.id.list_rv) as EmptyRecyclerView
        mRecyclerView!!.layoutManager = LinearLayoutManager(activity)
        mSortByLL = rootView.findViewById<View>(R.id.sortby_layout) as LinearLayout
        mFilterByTV =
            rootView.findViewById<View>(R.id.filterby) as Button
        mOrderByIV = rootView.findViewById<View>(R.id.orderby) as ImageButton
        mFilterByTV!!.setOnClickListener { openFilterDialog() }
        mOrderByIV!!.setOnClickListener { orderByTrigger() }
        mSelectedDevice = null
        val devices =
            UsbMassStorageDevice.getMassStorageDevices(activity!!)
        mError = false
        if (devices.size > 0) mSelectedDevice = devices[0]
        updateUI()
        try {
            mSelectedDevice!!.init()

            // we always use the first partition of the device
            val fs =
                mSelectedDevice!!.partitions[0].fileSystem
            val root = fs.rootDirectory
            setupRecyclerView()
            mAdapter = UsbFilesAdapter(
                activity!!, root,
                mRecyclerItemClickListener!!
            )
            mRecyclerView!!.adapter = mAdapter
            updateSortUI(false)
            if (DEBUG) Log.d(TAG, "root getCurrentDir: " + mAdapter!!.currentDir)
        } catch (e: Exception) {
            Log.e(TAG, "error setting up device", e)
            rootView.findViewById<View>(R.id.error).visibility = View.VISIBLE
            mError = true
        }
        if (mError) {
            mErrorView = rootView.findViewById<View>(R.id.error) as TextView
            mErrorView!!.visibility = View.VISIBLE
        } else {
            mEmptyView = rootView.findViewById<View>(R.id.empty) as LinearLayout
            mRecyclerView!!.setEmptyView(mEmptyView, mSortByLL)
        }

        // Inflate the layout for this fragment
        return rootView
    }

    private fun setupRecyclerView() {
        mRecyclerItemClickListener = RecyclerItemClickListener(
            activity, this!!.mRecyclerView!!, object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    onListItemClick(position)
                }

                override fun onLongItemClick(view: View?, position: Int) {
                    onItemLongClick(position)
                }
            })
        mRecyclerView!!.addOnItemTouchListener(mRecyclerItemClickListener!!)
    }

    private fun onListItemClick(position: Int) {
        val entry = mAdapter!!.getItem(position)
        try {
            if (entry!!.isDirectory) {
                dirs.push(mAdapter!!.currentDir)
                doRefresh(entry)
            } else {
                mIsShowcase = false
                copyFileToCache(entry)
            }
        } catch (e: IOException) {
            Log.e(TAG, "error starting to copy!", e)
        }
    }

    private fun onItemLongClick(position: Int): Boolean {
        if (DEBUG) Log.d(TAG, "Long click on position: $position")
        val entry = mAdapter!!.getItem(position)
        if (Utils.isImage(entry!!)) {
            showLongClickDialog(entry)
        }
        return true
    }

    override fun onActivityCreated(savedState: Bundle?) {
        super.onActivityCreated(savedState)
        if (DEBUG) Log.d(TAG, "onActivityCreated")
        try {
            if (mError) {
                mRecyclerView!!.visibility = View.GONE
                mRecyclerView!!.adapter = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Content view is not yet created!", e)
        }
    }

    private fun showLongClickDialog(entry: UsbFile?) {
        // We already checked it's an image
        val dialogAlert =
            AlertDialog.Builder(activity!!)
        dialogAlert.setTitle(R.string.showcase_longclick_dialog_title)
        dialogAlert.setNegativeButton(
            getString(android.R.string.cancel)
        ) { dialog, which -> dialog.cancel() }
        dialogAlert.setPositiveButton(
            android.R.string.ok
        ) { dialog, which ->
            try {
                mIsShowcase = true
                copyFileToCache(entry)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        dialogAlert.show()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (DEBUG) Log.d(TAG, "onAttach")
        try {
            mMainActivity = activity as ExplorerCallback
        } catch (castException: ClassCastException) {
            /** The activity does not implement the listener.  */
        }
    }

    private fun updateUI() {
        mMainActivity!!.setABTitle(getString(R.string.explorer_title), true)
    }

    override fun onDetach() {
        super.onDetach()
        mSelectedDevice = null
        if (DEBUG) Log.d(TAG, "onDetach")
    }

    fun popUsbFile(): Boolean {
        try {
            val dir = dirs.pop()
            doRefresh(dir)
            return true
        } catch (e: NoSuchElementException) {
            Log.e(TAG, "we should remove this fragment!")
        } catch (e: Exception) {
            Log.e(TAG, "error initializing mAdapter!", e)
        }
        return false
    }

    @Throws(IOException::class)
    private fun copyFileToCache(entry: UsbFile?) {
        val param = CopyTaskParam()
        param.from = entry


        if (!Utils.otgViewerCachePath.exists()) {
            Utils.otgViewerCachePath.mkdirs()
        }else
        {

        }
        val index = entry!!.name.lastIndexOf(".")
        var prefix: String
        var ext = ""
        if (index < 0) prefix = entry.name else {
            prefix = entry.name.substring(0, index)
            ext = entry.name.substring(index)
        }

        // prefix must be at least 3 characters
        if (DEBUG) Log.d(TAG, "ext: $ext")
        if (prefix.length < 3) {
            prefix += "pad"
        }
        val fileName = prefix + ext
        val downloadedFile =
            File(Utils.otgViewerPath, fileName)
        val cacheFile =
            File(Utils.otgViewerCachePath, fileName)
        param.to = cacheFile
        ImageViewer.instance!!.currentFile = entry
        if (!cacheFile.exists() && !downloadedFile.exists()) CopyTask(
            this,
            Utils.isImage(entry)
        ).execute(param) else launchIntent(cacheFile)
    }

    private fun launchIntent(f: File) {
        if (Utils.isImage(f)) {
            ImageViewer.instance?.adapter = mAdapter
            if (mAdapter!!.currentDir == null) {
                val fs =
                    mSelectedDevice!!.partitions[0].fileSystem
                val root = fs.rootDirectory
                ImageViewer.instance?.currentDirectory = root
            } else {
                ImageViewer.instance?.currentDirectory = mAdapter!!.currentDir
            }
            val intent = Intent(activity, ImageViewerActivity::class.java)
            intent.putExtra("SHOWCASE", mIsShowcase)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivityForResult(intent, REQUEST_IMAGEVIEWER)
            return
        }
        val intent = Intent(Intent.ACTION_VIEW)
        val file = File(f.absolutePath)
        val mimetype = Utils.getMimetype(file)
        intent.setDataAndType(
            FileProvider.getUriForFile(
                activity!!,
                activity!!.packageName + ".provider",
                file
            ), mimetype
        )
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(
                mMainActivity!!.coordinatorLayout!!,
                getString(R.string.toast_no_app_match), Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private inner class CopyTask(private val parent: ExplorerFragment, isImage: Boolean) :
        AsyncTask<CopyTaskParam?, Int?, Void?>() {
        private var dialog: ProgressDialog? = null
        private var param: CopyTaskParam? = null
        private val cp: CopyTask
        private fun showImageDialog() {
            dialog = ProgressDialog(parent.activity)
            dialog!!.setTitle(getString(R.string.dialog_image_title))
            dialog!!.setMessage(getString(R.string.dialog_image_message))
            dialog!!.isIndeterminate = true
            dialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        }

        private fun showDialog() {
            dialog = ProgressDialog(parent.activity)
            dialog!!.setTitle(getString(R.string.dialog_default_title))
            dialog!!.isIndeterminate = false
            dialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        }

        override fun onCancelled(result: Void?) {
            // Remove uncompleted data file
            if (DEBUG) Log.d(TAG, "Removing uncomplete file transfer")
            if (param != null) param!!.to?.delete()
        }

        override fun onPreExecute() {
            dialog!!.setOnCancelListener {
                if (DEBUG) Log.d(TAG, "Dialog canceled")
                cp.cancel(true)
            }
            dialog!!.show()
        }

        protected override fun doInBackground(vararg params: CopyTaskParam?): Void? {
            val time = System.currentTimeMillis()
            val buffer = ByteBuffer.allocate(4096)
            param = params[0]
            val length = params[0]?.from?.length
            try {
                val out = FileOutputStream(param!!.to)
                var i: Long = 0
                while (i < length!!) {
                    if (!isCancelled) {
                        buffer.limit(
                            Math.min(
                                buffer.capacity().toLong(),
                                length - i
                            ).toInt()
                        )
                        params[0]?.from!!.read(i, buffer)
                        out.write(buffer.array(), 0, buffer.limit())
                        publishProgress(i.toInt())
                        buffer.clear()
                    }
                    i += buffer.limit().toLong()
                }
                out.close()
            } catch (e: IOException) {
                Log.e(TAG, "error copying!", e)
            }
            if (DEBUG) Log.d(
                TAG,
                "copy time: " + (System.currentTimeMillis() - time)
            )
            return null
        }

        override fun onPostExecute(result: Void?) {
            dialog!!.dismiss()
            parent.launchIntent(param!!.to!!)
        }

        protected override fun onProgressUpdate(vararg values: Int?) {
            dialog!!.max = param!!.from?.length!!.toInt()
            dialog!!.progress = values[0]!!
        }

        init {
            cp = this
            if (isImage) showImageDialog() else showDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGEVIEWER) {
            if (DEBUG) Log.d(TAG, "Scrolling to position: $resultCode")
            mRecyclerView!!.scrollToPosition(resultCode)
        }
    }

    companion object {
        @JvmField
        var mSortByCurrent = 0
        var mSortAsc = false
    }
}