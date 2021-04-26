package com.androidinspain.otgviewer.util

/**
 * Created by roberto on 31/12/17.
 */
object Constants {
    /**
     * subclass 6 means that the usb mass storage device implements the SCSI
     * transparent command set
     */
    const val INTERFACE_SUBCLASS = 6

    /**
     * protocol 80 means the communication happens only via bulk transfers
     */
    const val INTERFACE_PROTOCOL = 80
    const val SORT_FILTER_PREF = "SORT_FILTER_PREF"
    const val SORT_ASC_KEY = "SORT_ASC_KEY"
    const val SORT_FILTER_KEY = "SORT_FILTER_KEY"
    const val SORTBY_NAME = 0
    const val SORTBY_DATE = 1
    const val SORTBY_SIZE = 2
    const val CACHE_THRESHOLD = 20 * 1024 * 1024 // 20 MB
}