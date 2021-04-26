package com.androidinspain.otgviewer.fragments

import android.app.Activity
import android.hardware.usb.UsbDevice
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.ListFragment
import com.androidinspain.otgviewer.R

class HomeFragment : ListFragment() {
    private val TAG = "HomeFragment"
    private val DEBUG = false
    private var mMainActivity: HomeCallback? = null
    private var mDetectedDevices: List<UsbDevice>? = null

    interface HomeCallback {
        val usbDevices: List<UsbDevice>?
        fun requestPermission(pos: Int)
        fun setABTitle(title: String?, showMenu: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        updateUI()

        // Inflate the layout for this fragment
        return rootView
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (DEBUG) Log.d(TAG, "onAttach")
        try {
            mMainActivity = activity as HomeCallback
        } catch (castException: ClassCastException) {
            /** The activity does not implement the listener.  */
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (DEBUG) Log.d(TAG, "onDetach")
    }

    fun updateUI() {
        if (DEBUG) Log.d(TAG, "updateUI in HomeFragment")
        mMainActivity!!.setABTitle(getString(R.string.home_title), false)
        mDetectedDevices = mMainActivity!!.usbDevices
        val showDevices: MutableList<String?> =
            ArrayList()
        for (i in mDetectedDevices!!.indices) {
            // API level 21
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                showDevices.add(mDetectedDevices!![i].productName)
            } else {
                showDevices.add(mDetectedDevices!![i].deviceName)
            }
        }
        val myAdapter = ArrayAdapter(
            this!!.activity!!,
            R.layout.row_listdevices,
            R.id.listText,
            showDevices
        )

        // assign the list adapter
        listAdapter = myAdapter
    }

    // when an item of the list is clicked
    override fun onListItemClick(
        list: ListView,
        view: View,
        position: Int,
        id: Long
    ) {
        super.onListItemClick(list, view, position, id)
        val selectedDevice =
            listView.getItemAtPosition(position) as String
        mMainActivity!!.requestPermission(position)
        if (DEBUG) Log.d(
            TAG,
            "You clicked $selectedDevice at position $position"
        )
    }
}