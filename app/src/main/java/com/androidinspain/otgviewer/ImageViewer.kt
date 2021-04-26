package com.androidinspain.otgviewer

import com.androidinspain.otgviewer.adapters.UsbFilesAdapter
import com.github.mjdev.libaums.fs.UsbFile

/**
 * Created by roberto on 23/08/15.
 */
class ImageViewer private constructor() {
    var currentDirectory: UsbFile? = null
    var currentFile: UsbFile? = null
    var adapter: UsbFilesAdapter? = null

    companion object {
        private var mInstance: ImageViewer? = null
        val instance: ImageViewer?
            get() {
                if (mInstance == null) {
                    mInstance = ImageViewer()
                }
                return mInstance
            }
    }

}