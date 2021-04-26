package com.androidinspain.otgviewer

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.androidinspain.otgviewer.ImageViewerActivity
import com.androidinspain.otgviewer.adapters.UsbFilesAdapter
import com.androidinspain.otgviewer.fragments.SettingsFragment.Companion.areTransitionsEnabled
import com.androidinspain.otgviewer.fragments.SettingsFragment.Companion.getShowcaseSpeed
import com.androidinspain.otgviewer.fragments.SettingsFragment.Companion.isLowRamEnabled
import com.androidinspain.otgviewer.fragments.SettingsFragment.Companion.isShakeEnabled
import com.androidinspain.otgviewer.task.CopyTaskParam
import com.androidinspain.otgviewer.ui.TouchImageView
import com.androidinspain.otgviewer.util.Utils
import com.github.mjdev.libaums.fs.UsbFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by roberto on 21/08/15.
 */
class ImageViewerActivity : AppCompatActivity(), SensorEventListener {
    private val TAG = javaClass.simpleName
    private val DEBUG = false
    private var mCurrentDirectory: UsbFile? = null
    private var mCurrentFile: UsbFile? = null
    private var mImagesFiles: ArrayList<UsbFile?>? = null
    private val TAG_NEXT_SHOWCASE = "NEXT_SHOWCASE"
    private var mTotalCount = 0
    private var mCurrentCount = 0
    private var mLayoutHeight = 0
    private var mLayoutWidth = 0
    private var mAdapter: ImagePagerAdapter? = null
    private var mViewPager: ViewPager? = null
    private val copyArray: MutableList<CopyTask> =
        ArrayList()
    private val mToolbar: Toolbar? = null
    private var mGestureDetector: GestureDetector? = null
    private var mImmersive = false
    private var mFooter: TextView? = null
    private var mPausePlay: ImageView? = null
    private var mDecorView: View? = null
    private var mIsShowcase = false
    private var mShowcaseRunning = false

    // Settings
    private var mTransitionsEnabled = false
    private var mLowRam = false
    private var mShakeEnabled = false
    private var mShowcaseSpeed = 0
    private val NEXT_SHOWCASE = 0
    private val SHOW_TUTORIAL = 1
    private val SHOW_TUTORIAL_DELAY = 4000
    private val NEXT_SHOWCASE_RETRY_TIMEOUT_MS = 400
    private val BUFFER = 2
    private val TUTORIAL_SP = "tutorialPassed"

    // UI
    private val FADE_TIMEOUT = 1000
    private var mUsbAdapter: UsbFilesAdapter? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_viewer)
        mCurrentFile = ImageViewer.Companion.instance?.currentFile
        mCurrentDirectory = ImageViewer.Companion.instance?.currentDirectory
        mUsbAdapter = ImageViewer.Companion.instance?.adapter
        mImagesFiles = ArrayList()
        mFooter = findViewById<View>(R.id.titleActivity) as TextView
        mPausePlay =
            findViewById<View>(R.id.pause_play_icon) as ImageView
        mIsShowcase = intent.getBooleanExtra("SHOWCASE", false)
        if (mIsShowcase) {
            mImmersive = true
            mHandler.sendEmptyMessageDelayed(SHOW_TUTORIAL, SHOW_TUTORIAL_DELAY.toLong())
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        var localCount = 0
        try {
            mImagesFiles = mUsbAdapter!!.imageFiles
            mTotalCount = mImagesFiles!!.size
            for (file in mImagesFiles!!) {
                Log.d(TAG, "localCount $localCount")
                if (file!!.name.equals(mCurrentFile!!.name, ignoreCase = true)) {
                    mCurrentCount = localCount
                }
                localCount++
            }
        } catch (e: Exception) {
            Log.e(TAG, "error setting up device", e)
        }
        if (DEBUG) {
            Log.d(TAG, "mCurrentFile " + mCurrentFile!!.name)
            Log.d(TAG, "mTotalCount $mTotalCount")
            Log.d(TAG, "mCurrentCount $mCurrentCount")
        }
        mViewPager = findViewById<View>(R.id.view_pager) as ViewPager
        mAdapter = ImagePagerAdapter()
        val onPageChangeListener: OnPageChangeListener = VPOnPageChangeListener()
        mViewPager!!.addOnPageChangeListener(onPageChangeListener)
        mViewPager!!.adapter = mAdapter
        mViewPager!!.currentItem = mCurrentCount
        onPageChangeListener.onPageSelected(mCurrentCount)
        mViewPager!!.offscreenPageLimit = BUFFER
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        mLayoutWidth = size.x
        mLayoutHeight = size.y
        mDecorView = window.decorView
        mGestureDetector = GestureDetector(this, GestureTap())
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (DEBUG) Log.d(
            TAG,
            "ViewPager width: $mLayoutWidth, height: $mLayoutHeight"
        )
    }

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                NEXT_SHOWCASE -> {
                    val nextImage = mCurrentCount + 1
                    if (DEBUG) {
                        Log.d(TAG_NEXT_SHOWCASE, "mCurrentCount: $mCurrentCount")
                        Log.d(
                            TAG_NEXT_SHOWCASE,
                            "mAdapter.getCount(): " + mAdapter!!.count
                        )
                        Log.d(TAG_NEXT_SHOWCASE, "nextImage: $nextImage")
                    }
                    if (nextImage < mAdapter!!.count) {
                        if (isLayoutReady(nextImage)) {
                            mCurrentCount++
                            mViewPager!!.setCurrentItem(mCurrentCount, mTransitionsEnabled)
                            if (DEBUG) Log.d(
                                TAG_NEXT_SHOWCASE,
                                "isImageReady($nextImage) is true. Showing photo $mCurrentCount. Sending NEXT_SHOWCASE message in $mShowcaseSpeed"
                            )
                            sendEmptyMessageDelayed(NEXT_SHOWCASE, mShowcaseSpeed.toLong())
                        } else {
                            if (DEBUG) Log.d(
                                TAG_NEXT_SHOWCASE,
                                "isImageReady( " + nextImage + ") is false. Trying again in " + NEXT_SHOWCASE_RETRY_TIMEOUT_MS + "ms"
                            )
                            sendEmptyMessageDelayed(
                                NEXT_SHOWCASE,
                                NEXT_SHOWCASE_RETRY_TIMEOUT_MS.toLong()
                            )
                        }
                    }
                }
                SHOW_TUTORIAL -> showToastTutorial()
            }
            super.handleMessage(msg)
        }
    }

    private fun isLayoutReady(position: Int): Boolean {
        val container =
            mViewPager!!.findViewWithTag<View>("pos$position") as RelativeLayout
        var visibility = View.VISIBLE
        if (container != null) visibility =
            container.findViewById<View>(R.id.loading).visibility
        return visibility == View.GONE
    }

    // We show it only the first time
    private fun showToastTutorial() {
        val editor = getPreferences(Context.MODE_PRIVATE).edit()
        editor.putBoolean(TUTORIAL_SP, true)
        editor.apply()
        Toast.makeText(this, getString(R.string.showcase_tutorial), Toast.LENGTH_LONG).show()
    }

    /*
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean tutorialPassed = prefs.getBoolean(TUTORIAL_SP, false);

        if(!tutorialPassed){
            return true;
        }

        return false;
        */
    private val isTutorialNeeded: Boolean
        private get() =/*
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean tutorialPassed = prefs.getBoolean(TUTORIAL_SP, false);

        if(!tutorialPassed){
            return true;
        }

        return false;
        */
            true

    private fun isImageReady(f: File): Boolean {
        return if (f.exists()) true else false
    }

    private fun setImmersiveMode() {
        val visibility: Int
        if (DEBUG) Log.d(TAG, "setImmersiveMode: $mImmersive")
        if (mImmersive) {
            visibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
            mFooter!!.animate().alpha(0.0f)
            if (mIsShowcase && !mShowcaseRunning) {
                startShowcase()
            }
        } else {
            visibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            mFooter!!.animate().alpha(1.0f)
            if (mIsShowcase && mShowcaseRunning) stopShowcase()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mDecorView!!.systemUiVisibility = visibility
        }
    }

    private fun startShowcase() {
        mHandler.removeMessages(NEXT_SHOWCASE)
        mHandler.sendEmptyMessageDelayed(NEXT_SHOWCASE, mShowcaseSpeed.toLong())
        mShowcaseRunning = true
        showFadeIcon(resources.getDrawable(R.drawable.play_icon))

        /*
        if(isTutorialNeeded())
            mHandler.sendEmptyMessageDelayed(SHOW_TUTORIAL,SHOW_TUTORIAL_DELAY);
        */
    }

    private fun stopShowcase() {
        mHandler.removeMessages(NEXT_SHOWCASE)
        mShowcaseRunning = false
        showFadeIcon(resources.getDrawable(R.drawable.pause_icon))
    }

    private fun showFadeIcon(icon: Drawable) {
        mPausePlay!!.setImageDrawable(icon)
        mPausePlay!!.alpha = 1f
        mPausePlay!!.visibility = View.VISIBLE
        mPausePlay!!.animate().alpha(0f).startDelay = FADE_TIMEOUT.toLong()
    }

    private fun getImageResourceFromPosition(position: Int): Bitmap? {
        if (DEBUG) Log.d(
            TAG,
            "decode file from " + getCacheFullPath(mImagesFiles!![position]!!.name)
        )
        var bitmap: Bitmap? = null
        val f = File(getCacheFullPath(mImagesFiles!![position]!!.name))
        return if (isImageReady(f)) {
            val opts = BitmapFactory.Options()
            opts.inSampleSize = Utils.calculateInSampleSize(
                f,
                mLayoutWidth,
                mLayoutHeight
            )
            if (mLowRam || System.getProperty(
                    "ro.config.low_ram",
                    "false"
                ) == "true"
            ) opts.inSampleSize *= 2
            if (DEBUG) Log.d(TAG, "file exists! inSampleSize: " + opts.inSampleSize)
            opts.inPreferQualityOverSpeed = false
            opts.inMutable = false
            opts.inDither = false
            bitmap = BitmapFactory.decodeFile(f.absolutePath, opts)
            val maxSize = Math.max(mLayoutWidth, mLayoutHeight)
            val outWidth: Int
            val outHeight: Int
            if (bitmap != null) {
                val inWidth = bitmap.width
                val inHeight = bitmap.height
                if (inWidth > inHeight) {
                    outWidth = maxSize
                    outHeight = inHeight * maxSize / inWidth
                } else {
                    outHeight = maxSize
                    outWidth = inWidth * maxSize / inHeight
                }
                if (DEBUG) Log.d(
                    TAG,
                    "outWidth: $outWidth, outHeight: $outHeight"
                )
                bitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, true)
            }
            bitmap
        } else {
            if (DEBUG) Log.d(TAG, "file doesn't exist! Starting asynctask")
            val param = CopyTaskParam()
            param.from = mImagesFiles!![position]
            param.to = f
            param.position = position
            val cp =
                CopyTask(this)
            cp.execute(param)
            copyArray.add(cp)
            null
        }
    }

    private fun getCacheFullPath(fileName: String): String {
        return cacheDir.toString() + File.separator + fileName
    }

    override fun onBackPressed() {
        setResult(mCurrentCount)
        finish()
    }

    override fun onResume() {
        super.onResume()

        // Load configuration
        mTransitionsEnabled = areTransitionsEnabled(this)
        mLowRam = isLowRamEnabled(this)
        mShakeEnabled = isShakeEnabled(this)
        mShowcaseSpeed = getShowcaseSpeed(this)
        if (!mTransitionsEnabled) mViewPager!!.setPageTransformer(false, NoPageTransformer())
        if (DEBUG) Log.d(
            TAG,
            "mLowRam: $mLowRam, mShakeEnabled: $mShakeEnabled"
        )
        if (!mIsShowcase && mShakeEnabled) mSensorManager!!.registerListener(
            this,
            mAccelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        setImmersiveMode()
        /*
        if(mIsShowcase && !mShowcaseRunning)
            startShowcase();
        */
    }

    override fun onPause() {
        super.onPause()
        if (!mIsShowcase && mShakeEnabled) mSensorManager!!.unregisterListener(this)
        if (mIsShowcase && mShowcaseRunning) stopShowcase()
    }

    public override fun onDestroy() {
        super.onDestroy()
        for (cp in copyArray) {
            cp?.cancel(true)
        }
    }

    private inner class VPOnPageChangeListener : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            //Log.d(TAG, "onPageScrolled: " + position);
        }

        override fun onPageSelected(position: Int) {
            if (DEBUG) Log.d(TAG, "onPageSelected: $position")
            mCurrentCount = position
            mFooter!!.text = mImagesFiles!![position]!!.name
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    private inner class ImagePagerAdapter : PagerAdapter() {
        override fun getCount(): Int {
            return mTotalCount
        }

        override fun isViewFromObject(
            view: View,
            `object`: Any
        ): Boolean {
            return view === `object` as RelativeLayout
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val context: Context = this@ImageViewerActivity
            return buildLayout(container, position)
        }

        override fun destroyItem(
            container: ViewGroup,
            position: Int,
            `object`: Any
        ) {
            (container as ViewPager).removeView(`object` as RelativeLayout)
        }
    }

    private inner class CopyTask(private val parent: ImageViewerActivity) :
        AsyncTask<CopyTaskParam?, Int?, Bitmap?>() {
        private val dialog: ProgressDialog? = null
        private var param: CopyTaskParam? = null
        override fun onPreExecute() {
            //dialog.show();
            if (param != null && DEBUG) {
                Log.d(TAG, "Starting CopyTask with " + param!!.from!!.name)
            }
        }

        override fun onCancelled(result: Bitmap?) {
            // Remove uncompleted data file
            if (DEBUG) Log.d(TAG, "Removing uncomplete file transfer")
            if (param != null && param!!.to!!.exists()) param!!.to!!.delete()
        }

        protected override fun doInBackground(vararg params: CopyTaskParam?): Bitmap? {
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
                        params[0]!!.from!!.read(i, buffer)
                        out.write(buffer.array(), 0, buffer.limit())
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
            return getImageResourceFromPosition(param!!.position)
        }

        override fun onPostExecute(result: Bitmap?) {
            if (DEBUG) Log.d(TAG, "CopyTask done!")
            val container =
                mViewPager!!.findViewWithTag<View>("pos" + param!!.position) as RelativeLayout
            if (container != null) {
                if (DEBUG) Log.d(TAG, "container is not null")
                val imageView =
                    container.findViewById<View>(R.id.image) as TouchImageView
                val spinner =
                    container.findViewById<View>(R.id.loading) as ProgressBar
                result?.let { imageView.setImageBitmap(it) }
                spinner.visibility = View.GONE
                imageView.visibility = View.VISIBLE
            }
            if (DEBUG) Log.d(TAG, "onPostExecute. builtLayout: " + param!!.position)
            copyArray.remove(this)
        }

        protected override fun onProgressUpdate(vararg values: Int?) {
            //dialog.setMax((int) param.from.getLength());
            //dialog.setProgress(values[0]);
        }

    }

    private fun buildLayout(container: ViewGroup, position: Int): RelativeLayout {
        val inflater = container.context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val imageLayout =
            inflater.inflate(R.layout.viewpager_layout, null) as RelativeLayout
        val imageView =
            imageLayout.findViewById<View>(R.id.image) as TouchImageView
        val spinner =
            imageLayout.findViewById<View>(R.id.loading) as ProgressBar
        val padding = 0
        imageLayout.setBackgroundColor(Color.BLACK)
        imageView.setPadding(padding, padding, padding, padding)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val image = getImageResourceFromPosition(position)
        if (image == null) {
            spinner.isIndeterminate = true
            imageView.visibility = View.GONE
            spinner.visibility = View.VISIBLE
        } else {
            imageView.setImageBitmap(image)
            spinner.visibility = View.GONE
            imageView.visibility = View.VISIBLE
        }
        imageView.setOnTouchListener { v, event ->
            if (mGestureDetector!!.onTouchEvent(event)) {
                true
            } else false
        }
        (container as ViewPager).addView(imageLayout, 0)
        imageLayout.tag = "pos$position"
        return imageLayout
    }

    private fun decreaseSpeed() {
        if (mShowcaseRunning) {
            mShowcaseSpeed *= SPEED_FACTOR.toInt()
            showFadeIcon(resources.getDrawable(R.drawable.fr_icon))
        }
    }

    private fun increaseSpeed() {
        if (mShowcaseRunning) {
            mShowcaseSpeed /= SPEED_FACTOR.toInt()
            showFadeIcon(resources.getDrawable(R.drawable.ff_icon))
        }
    }

    private val SPEED_FACTOR = 1.5
    private val VELOCITY_THRESHOLD = 4000
    private val SWIPE_TOP = 1
    private val SWIPE_LEFT = 2
    private val SWIPE_DOWN = 3
    private val SWIPE_RIGHT = 4

    private inner class GestureTap : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (DEBUG) Log.d(TAG, "Single tap up detected!")
            mImmersive = !mImmersive
            setImmersiveMode()
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (DEBUG) Log.d(TAG, "Swipe onFling: $velocityX, $velocityY")
            if (Math.abs(velocityY) > VELOCITY_THRESHOLD) {
                when (getSlope(e1.x, e1.y, e2.x, e2.y)) {
                    SWIPE_TOP -> {
                        increaseSpeed()
                        return true
                    }
                    SWIPE_LEFT -> return true
                    SWIPE_DOWN -> {
                        decreaseSpeed()
                        return true
                    }
                    SWIPE_RIGHT -> return true
                }
            }
            return false
        }

        private fun getSlope(
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float
        ): Int {
            val angle = Math.toDegrees(
                Math.atan2(
                    y1 - y2.toDouble(),
                    x2 - x1.toDouble()
                )
            )
            if (angle > 45 && angle <= 135) return SWIPE_TOP
            if (angle >= 135 && angle < 180 || angle < -135 && angle > -180) return SWIPE_LEFT
            if (angle < -45 && angle >= -135) return SWIPE_DOWN
            return if (angle > -45 && angle <= 45) SWIPE_RIGHT else 0
        }
    }

    // Sensor Feature
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastGesture: Long = 0
    private var last_x = 0f
    private var last_y = 0f
    private var last_z = 0f
    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val mySensor = sensorEvent.sensor
        if (mySensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = sensorEvent.values[0]
            val y = sensorEvent.values[1]
            val z = sensorEvent.values[2]
            val curTime = System.currentTimeMillis()
            if (curTime - lastUpdate > 100) {
                val diffTime = curTime - lastUpdate
                lastUpdate = curTime
                val speed =
                    Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000
                if (speed > SHAKE_THRESHOLD && curTime - lastGesture > 1000) {
                    if (DEBUG) Log.d(TAG, "SHAKE DETECTED!")
                    mViewPager!!.setCurrentItem(mViewPager!!.currentItem + 1, mTransitionsEnabled)
                    lastGesture = curTime
                }
                last_x = x
                last_y = y
                last_z = z
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor,
        accuracy: Int
    ) {
    }

    // Used for remotes such as Nexus Player remote controller
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (event.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_DPAD_CENTER -> {
                mImmersive = !mImmersive
                setImmersiveMode()
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                increaseSpeed()
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                decreaseSpeed()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private class NoPageTransformer : ViewPager.PageTransformer {
        override fun transformPage(
            view: View,
            position: Float
        ) {
            if (position < 0) {
                view.scrollX = (view.width.toFloat() * position).toInt()
            } else if (position > 0) {
                view.scrollX = (-(view.width.toFloat() * -position)).toInt()
            } else {
                view.scrollX = 0
            }
        }
    }

    companion object {
        private const val SHAKE_THRESHOLD = 600
    }
}