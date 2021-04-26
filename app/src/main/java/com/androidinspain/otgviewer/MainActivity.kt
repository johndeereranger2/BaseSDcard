package com.androidinspain.otgviewer


import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.androidinspain.otgviewer.fragments.ExplorerFragment
import com.androidinspain.otgviewer.fragments.ExplorerFragment.ExplorerCallback
import com.androidinspain.otgviewer.fragments.HomeFragment
import com.androidinspain.otgviewer.fragments.HomeFragment.HomeCallback
import com.androidinspain.otgviewer.fragments.SettingsFragment
import com.androidinspain.otgviewer.fragments.SettingsFragment.SettingsCallback
import com.androidinspain.otgviewer.ui.VisibilityManager
import com.androidinspain.otgviewer.util.Utils
import com.github.mjdev.libaums.UsbMassStorageDevice
import java.util.*

class MainActivity : AppCompatActivity(), HomeCallback, ExplorerCallback,
    SettingsCallback {
    private val TAG = "OTGViewer"
    private val DEBUG = false
    private var mDetectedDevices: MutableList<UsbDevice>? = null
    private var mPermissionIntent: PendingIntent? = null
    private var mUsbManager: UsbManager? = null
    private var mUsbMSDevice: UsbMassStorageDevice? = null
    private var mToolbar: Toolbar? = null
    override var coordinatorLayout: CoordinatorLayout? = null
        private set
    private var mHomeFragment: HomeFragment? = null
    private var mSettingsFragment: SettingsFragment? = null
    private var mExplorerFragment: ExplorerFragment? = null
    private var mVisibilityManager: VisibilityManager? = null
    private val HOME_FRAGMENT = 0
    private val SETTINGS_FRAGMENT = 1
    private val EXPLORER_FRAGMENT = 2
    private val mShowIcon = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mToolbar =
            findViewById<View>(R.id.toolbar) as Toolbar
        coordinatorLayout =
            findViewById<View>(R.id.coordinator_layout) as CoordinatorLayout
        setSupportActionBar(mToolbar)
        if (DEBUG) Log.d(TAG, "onCreate")
        mHomeFragment = HomeFragment()
        mSettingsFragment = SettingsFragment()
        mExplorerFragment = ExplorerFragment()
        mPermissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_USB_PERMISSION),
            0
        )
        mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        mDetectedDevices = ArrayList()
        mVisibilityManager = VisibilityManager()
        displayView(HOME_FRAGMENT)
    }

    private fun displayView(position: Int) {
        var fragment: Fragment? = null
        when (position) {
            HOME_FRAGMENT -> fragment = mHomeFragment
//            SETTINGS_FRAGMENT -> fragment = mSettingsFragment
            EXPLORER_FRAGMENT -> fragment = mExplorerFragment
            else -> {
            }
        }
        val tag = Integer.toString(position)
        if (fragmentManager.findFragmentByTag(tag) != null && fragmentManager.findFragmentByTag(
                tag
            ).isVisible
        ) {
            if (DEBUG) Log.d(TAG, "No transition needed. Already in that fragment!")
            return
        }
        if (fragment != null) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction =
                fragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                R.animator.enter,
                R.animator.exit,
                R.animator.pop_enter,
                R.animator.pop_exit
            )
            fragmentTransaction.replace(R.id.container_body, fragment, tag)

            // Home fragment is not added to the stack
            if (position != HOME_FRAGMENT) {
                fragmentTransaction.addToBackStack(null)
            }
            fragmentTransaction.commitAllowingStateLoss()
            getFragmentManager().executePendingTransactions()
        }
    }

    override fun onBackPressed() {
        // Catch back action and pops from backstack
        // (if you called previously to addToBackStack() in your transaction)
        var result = false
        if (mExplorerFragment != null && mExplorerFragment!!.isVisible) {
            if (DEBUG) Log.d(TAG, "we are on ExplorerFragment. Result: $result")
            result = mExplorerFragment!!.popUsbFile()
        }
        if (result) return else if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            if (DEBUG) Log.d(TAG, "Pop fragment")
        } else super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                displayView(SETTINGS_FRAGMENT)
                return true
            }
            R.id.action_showcase -> return false
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun removedUSB() {
        if (mVisibilityManager!!.isVisible) {
            while (fragmentManager.backStackEntryCount != 0) {
                fragmentManager.popBackStackImmediate()
            }
            displayView(HOME_FRAGMENT)
        } else {
            val intent = intent
            finish()
            startActivity(intent)
        }
    }

    var mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            val action = intent.action
            if (DEBUG) Log.d(TAG, "mUsbReceiver triggered. Action $action")
            checkUSBStatus()
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) removedUSB()
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device =
                        intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device == null) {
                        } else {
                            //call method to set up device communication
                            if (DEBUG) Log.d(
                                TAG,
                                "granted permission for device " + device.deviceName + "!"
                            )
                            connectDevice(device)
                        }
                    } else {
                        Log.e(TAG, "permission denied for device $device")
                    }
                }
            }
        }
    }

    private fun checkUSBStatus() {
        if (DEBUG) Log.d(TAG, "checkUSBStatus")
        mDetectedDevices!!.clear()
        mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        if (mUsbManager != null) {
            val deviceList =
                mUsbManager!!.deviceList
            if (!deviceList.isEmpty()) {
                val deviceIterator: Iterator<UsbDevice> =
                    deviceList.values.iterator()
                while (deviceIterator.hasNext()) {
                    val device = deviceIterator.next()
                    if (Utils.isMassStorageDevice(device)) mDetectedDevices!!.add(
                        device
                    )
                }
            }
            updateUI()
        }
    }

    override fun onResume() {
        super.onResume()
        if (DEBUG) Log.d(TAG, "onResume")
        mVisibilityManager!!.isVisible = true
        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_PERMISSION)
        registerReceiver(mUsbReceiver, filter)
        checkUSBStatus()
    }

    override fun onPause() {
        super.onPause()
        if (DEBUG) Log.d(TAG, "onPause")
        mVisibilityManager!!.isVisible = false
    }

    public override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mUsbReceiver)
        Utils.deleteCache(cacheDir)
    }

    private fun updateUI() {
        if (DEBUG) Log.d(TAG, "updateUI")
        if (mHomeFragment != null && mHomeFragment!!.isAdded) {
            mHomeFragment!!.updateUI()
        }
    }

    private fun connectDevice(device: UsbDevice) {
        if (DEBUG) Log.d(TAG, "Connecting to device")
        if (mUsbManager!!.hasPermission(device) && DEBUG) Log.d(TAG, "got permission!")
        val devices =
            UsbMassStorageDevice.getMassStorageDevices(this)
        if (devices.size > 0) {
            mUsbMSDevice = devices[0]
            setupDevice()
        }
    }

    private fun setupDevice() {
        displayView(EXPLORER_FRAGMENT)
    }

    override fun requestPermission(pos: Int) {
        mUsbManager!!.requestPermission(mDetectedDevices!![pos], mPermissionIntent)
    }

    override val usbDevices: List<UsbDevice>?
        get() = mDetectedDevices

    override fun setABTitle(title: String?, showMenu: Boolean) {
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(showMenu)
        // Set logo
        supportActionBar!!.setDisplayShowHomeEnabled(mShowIcon)
        supportActionBar!!.setIcon(R.mipmap.ic_launcher)
    }

    companion object {
        private const val ACTION_USB_PERMISSION =
            "com.androidinspain.otgviewer.USB_PERMISSION"
    }
}