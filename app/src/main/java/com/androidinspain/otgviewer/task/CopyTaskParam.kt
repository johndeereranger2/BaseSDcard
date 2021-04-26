package com.androidinspain.otgviewer.task

import com.github.mjdev.libaums.fs.UsbFile
import java.io.File

/**
 * Created by roberto on 23/08/15.
 */
class CopyTaskParam {
    var from: UsbFile? = null
    var to: File? = null
    var position = 0
}