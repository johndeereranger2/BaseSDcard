package com.androidinspain.otgviewer.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by roberto on 6/09/17.
 */
class EmptyRecyclerView : RecyclerView {
    private val TAG = javaClass.name
    private val DEBUG = false
    private var mEmptyView: View? = null
    private var mSortByLL: View? = null
    private val mDataObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            updateEmptyView()
        }
    }

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context!!, attrs, defStyle) {
    }

    /**
     * Designate a view as the empty view. When the backing adapter has no
     * data this view will be made visible and the recycler view hidden.
     */
    fun setEmptyView(emptyView: View?, sortByLL: View?) {
        mEmptyView = emptyView
        mSortByLL = sortByLL
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (getAdapter() != null) {
            getAdapter()!!.unregisterAdapterDataObserver(mDataObserver)
        }
        adapter?.registerAdapterDataObserver(mDataObserver)
        super.setAdapter(adapter)
        updateEmptyView()
    }

    private fun updateEmptyView() {
        if (mEmptyView != null && adapter != null) {
            val showEmptyView = adapter!!.itemCount == 0
            mEmptyView!!.visibility = if (showEmptyView) View.VISIBLE else View.GONE
            mSortByLL!!.visibility = if (showEmptyView) View.GONE else View.VISIBLE
            visibility = if (showEmptyView) View.GONE else View.VISIBLE
        }
    }
}