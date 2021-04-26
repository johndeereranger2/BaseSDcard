package com.androidinspain.otgviewer.recyclerview

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.androidinspain.otgviewer.util.Utils

class RecyclerItemClickListener(
    context: Context?,
    private val mRecyclerView: RecyclerView,
    private val mListener: OnItemClickListener?
) : OnItemTouchListener {
    private val TAG = javaClass.name
    private val DEBUG = false

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onLongItemClick(view: View?, position: Int)
    }

    var mGestureDetector: GestureDetector
    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        if (DEBUG) Log.d(TAG, "onInterceptTouchEvent $e")
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
            if (DEBUG) Log.d(TAG, "onInterceptTouchEvent: singleClick!")
            return true
        }
        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {
        if (DEBUG) Log.d(TAG, "onTouchEvent $motionEvent")
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    fun handleDPad(
        child: View?,
        keyCode: Int,
        keyEvent: KeyEvent
    ): Boolean {
        Log.d(TAG, "handleDPad")
        val position = mRecyclerView.getChildLayoutPosition(child!!)

        // Return false if scrolled to the bounds and allow focus to move off the list
        if (keyEvent.action == KeyEvent.ACTION_DOWN) {
            if (Utils.isConfirmButton(keyEvent)) {
                if (keyEvent.flags and KeyEvent.FLAG_LONG_PRESS == KeyEvent.FLAG_LONG_PRESS) {
                    mListener!!.onLongItemClick(child, position)
                } else {
                    keyEvent.startTracking()
                }
                return true
            }
        } else if (keyEvent.action == KeyEvent.ACTION_UP && Utils.isConfirmButton(
                keyEvent
            )
            && keyEvent.flags and KeyEvent.FLAG_LONG_PRESS != KeyEvent.FLAG_LONG_PRESS
        ) {
            mListener!!.onItemClick(child, position)
            return true
        }
        return false
    }

    init {
        mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child = mRecyclerView.findChildViewUnder(e.x, e.y)
                if (child != null && mListener != null) {
                    if (DEBUG) Log.d(TAG, "onLongPress!")
                    mListener.onLongItemClick(child, mRecyclerView.getChildAdapterPosition(child))
                }
            }
        })
    }
}