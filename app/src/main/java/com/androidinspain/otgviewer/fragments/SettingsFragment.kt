package com.androidinspain.otgviewer.fragments


import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.view.Menu
import com.androidinspain.otgviewer.R


class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var mMainActivity: SettingsCallback? = null
    private val TAG = javaClass.simpleName
    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String
    ) {
        // Change something dinamically
    }

    interface SettingsCallback {
        fun setABTitle(title: String?, showMenu: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        addPreferencesFromResource(R.xml.settings)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_settings).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mMainActivity = activity as SettingsCallback
            updateUI()
        } catch (castException: ClassCastException) {
            /** The activity does not implement the listener.  */
        }
    }

    private fun updateUI() {
        mMainActivity!!.setABTitle(getString(R.string.settings_title), true)
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        private const val enable_transitions = "enable_transitions"
        private const val enable_transitions_def = true
        private const val enable_shake = "enable_shake"
        private const val enable_shake_def = true
        private const val low_ram = "low_ram"
        private const val low_ram_def = false
        private const val showcase_speed = "showcase_speed"
        private const val showcase_speed_def = "5000" // medium
        @JvmStatic
        fun areTransitionsEnabled(context: Context?): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                enable_transitions,
                enable_transitions_def
            )
        }

        @JvmStatic
        fun isShakeEnabled(context: Context?): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                enable_shake,
                enable_shake_def
            )
        }

        @JvmStatic
        fun isLowRamEnabled(context: Context?): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                low_ram,
                low_ram_def
            )
        }

        @JvmStatic
        fun getShowcaseSpeed(context: Context?): Int {
            return PreferenceManager.getDefaultSharedPreferences(context).getString(
                showcase_speed,
                showcase_speed_def
            )!!.toInt()
        }
    }
}