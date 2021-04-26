package com.androidinspain.otgviewer.util

import android.content.Context
import android.graphics.BitmapFactory
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbEndpoint
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.webkit.MimeTypeMap
import com.androidinspain.otgviewer.R
import com.androidinspain.otgviewer.fragments.ExplorerFragment
import com.github.mjdev.libaums.fs.UsbFile
import java.io.File
import java.util.*

/**
 * Created by roberto on 23/08/15.
 */
object Utils {
    val otgViewerPath = File(
        Environment.getExternalStorageDirectory().absolutePath + "/OTGViewer"
    )
    val otgViewerCachePath = File(
        Environment.getExternalStorageDirectory().absolutePath + "/OTGViewer/cache"
    )
    private const val TAG = "Utils"
    private const val DEBUG = false
    fun calculateInSampleSize(f: File, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(f.absolutePath, options)
        // String imageType = options.outMimeType;

        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        if (DEBUG) {
            Log.d(
                TAG,
                "checkImageSize. X: $width, Y: $height"
            )
            Log.d(
                TAG,
                "Screen is. X: $reqWidth, Y: $reqHeight"
            )
        }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight
                && halfWidth / inSampleSize > reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(
                    File(
                        dir,
                        children[i]
                    )
                )
                if (!success) {
                    return false
                }
            }
        }
        // The directory is now empty or this is a file so delete it
        return dir.delete()
    }

    fun getDirSize(dir: File): Long {
        var size: Long = 0
        for (file in dir.listFiles()) {
            if (file != null && file.isDirectory) {
                size += getDirSize(file)
            } else if (file != null && file.isFile) {
                size += file.length()
            }
        }
        return size
    }

    // We remove the app's cache folder if threshold is exceeded
    fun deleteCache(cachePath: File) {
        val cacheSize = getDirSize(cachePath)
        if (DEBUG) Log.d(
            TAG,
            "cacheSize: $cacheSize"
        )
        if (getDirSize(cachePath) > Constants.CACHE_THRESHOLD) {
            if (DEBUG) Log.d(
                TAG,
                "Erasing cache folder"
            )
            deleteDir(cachePath)
        }
        deleteDir(otgViewerCachePath)
    }

    fun getMimetype(f: File?): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(
            Uri
                .fromFile(f).toString().toLowerCase()
        )
        return getMimetype(extension)
    }

    fun getMimetype(extension: String?): String? {
        val mimetype =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                extension
            )
        if (DEBUG) Log.d(
            TAG,
            "mimetype is: $mimetype"
        )
        return mimetype
    }

    var comparator: Comparator<UsbFile?> = object : Comparator<UsbFile?> {
        override fun compare(lhs: UsbFile?, rhs: UsbFile?): Int {
            if (DEBUG) Log.d(
                TAG,
                "comparator. Sorting by: " + ExplorerFragment.mSortByCurrent
            )
            when (ExplorerFragment.mSortByCurrent) {
                Constants.SORTBY_NAME -> return sortByName(
                    lhs,
                    rhs
                )
                Constants.SORTBY_DATE -> return sortByDate(
                    lhs,
                    rhs
                )
                Constants.SORTBY_SIZE -> return sortBySize(
                    lhs,
                    rhs
                )
                else -> {
                }
            }
            return 0
        }

        fun extractInt(s: String): Int {
            var result = 0
            // return 0 if no digits found
            try {
                val num = s.replace("\\D".toRegex(), "")
                result = if (num.isEmpty()) 0 else num.toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                return result
            }
        }

        fun checkIfDirectory(lhs: UsbFile, rhs: UsbFile): Int {
            if (lhs.isDirectory && !rhs.isDirectory) {
                return -1
            }
            return if (rhs.isDirectory && !lhs.isDirectory) {
                1
            } else 0
        }

        fun sortByName(lhs: UsbFile?, rhs: UsbFile?): Int {
            var result = 0
            val dir = checkIfDirectory(
                lhs!!,
                rhs!!
            )
            if (dir != 0) return dir

            // Check if there is any number
            val lhsNum: String =
                lhs.name.replace("\\D".toRegex(), "")
            val rhsNum: String =
                rhs.name.replace("\\D".toRegex(), "")
            var lhsRes = 0
            var rhsRes = 0
            if (!lhsNum.isEmpty() && !rhsNum.isEmpty()) {
                lhsRes =
                    extractInt(lhs.name)
                rhsRes =
                    extractInt(rhs.name)
                return lhsRes - rhsRes
            }
            result =
                lhs.name.compareTo(
                    rhs.name,
                    ignoreCase = true
                )
            return result
        }

        fun sortByDate(lhs: UsbFile?, rhs: UsbFile?): Int {
            var result: Long = 0
            val dir = checkIfDirectory(
                lhs!!,
                rhs!!
            )
            if (dir != 0) return dir
            result =
                lhs.lastModified() - rhs.lastModified()
            return result as Int
        }

        fun sortBySize(lhs: UsbFile?, rhs: UsbFile?): Int {
            var result: Long = 0
            val dir = checkIfDirectory(
                lhs!!,
                rhs!!
            )
            if (dir != 0) return dir
            try {
                result =
                    lhs.length - rhs.length
            } catch (e: Exception) {
            }
            return result as Int
        }
    }

    fun getHumanSortBy(context: Context): String {
        return when (ExplorerFragment.mSortByCurrent) {
            Constants.SORTBY_NAME -> context.getString(R.string.name)
            Constants.SORTBY_DATE -> context.getString(R.string.date)
            Constants.SORTBY_SIZE -> context.getString(R.string.size)
            else -> context.getString(R.string.name)
        }
    }

    fun isImage(entry: UsbFile): Boolean {
        if (entry.isDirectory) return false
        try {
            return isImageInner(entry.name)
        } catch (e: StringIndexOutOfBoundsException) {
            e.printStackTrace()
        }
        return false
    }

    fun isImage(entry: File): Boolean {
        return isImageInner(entry.name)
    }

    private fun isImageInner(name: String): Boolean {
        var result = false
        val index = name.lastIndexOf(".")
        if (index > 0) {
            val ext = name.substring(index)
            if (ext.equals(".jpg", ignoreCase = true) || ext.equals(".png", ignoreCase = true)
                || ext.equals(".jpeg", ignoreCase = true)
            ) {
                result = true
            }
            Log.d(
                TAG,
                "isImageInner $name: $result"
            )
        }
        return result
    }

    fun isConfirmButton(event: KeyEvent): Boolean {
        return when (event.keyCode) {
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_BUTTON_A -> true
            else -> false
        }
    }

    fun isMassStorageDevice(device: UsbDevice): Boolean {
        var result = false
        val interfaceCount = device.interfaceCount
        for (i in 0 until interfaceCount) {
            val usbInterface = device.getInterface(i)
            Log.i(
                TAG,
                "found usb interface: $usbInterface"
            )

            // we currently only support SCSI transparent command set with
            // bulk transfers only!
            if (usbInterface.interfaceClass != UsbConstants.USB_CLASS_MASS_STORAGE || usbInterface.interfaceSubclass != Constants.INTERFACE_SUBCLASS || usbInterface.interfaceProtocol != Constants.INTERFACE_PROTOCOL
            ) {
                Log.i(
                    TAG,
                    "device interface not suitable!"
                )
                continue
            }

            // Every mass storage device has exactly two endpoints
            // One IN and one OUT endpoint
            val endpointCount = usbInterface.endpointCount
            if (endpointCount != 2) {
                Log.w(
                    TAG,
                    "inteface endpoint count != 2"
                )
            }
            var outEndpoint: UsbEndpoint? = null
            var inEndpoint: UsbEndpoint? = null
            for (j in 0 until endpointCount) {
                val endpoint = usbInterface.getEndpoint(j)
                Log.i(
                    TAG,
                    "found usb endpoint: $endpoint"
                )
                if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (endpoint.direction == UsbConstants.USB_DIR_OUT) {
                        outEndpoint = endpoint
                    } else {
                        inEndpoint = endpoint
                    }
                }
            }
            if (outEndpoint == null || inEndpoint == null) {
                Log.e(
                    TAG,
                    "Not all needed endpoints found!"
                )
                continue
            }
            result = true
        }
        return result
    }
}