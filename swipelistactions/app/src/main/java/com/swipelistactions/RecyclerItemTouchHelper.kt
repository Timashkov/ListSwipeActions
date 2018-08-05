package com.swipelistactions

import android.graphics.Canvas
import android.support.v7.widget.RecyclerView

class RecyclerItemTouchHelper(dragDirs: Int, swipeDirs: Int, private val listener: RecyclerItemTouchHelperListener) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    val uiUtil = RowUiUtil()

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder?.let {
            if (it is ListViewAdapter.CategoryItemViewHolder) {
                val foregroundView = it.viewForeground
                uiUtil.onSelected(foregroundView)
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is ListViewAdapter.CategoryItemViewHolder) {
//            if (viewHolder.state == 0) {
                val foregroundView = viewHolder.viewForeground
                uiUtil.setHalfSwiped(foregroundView)
//            }
        }
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView,
                                 viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                                 actionState: Int, isCurrentlyActive: Boolean) {
        if (viewHolder is ListViewAdapter.CategoryItemViewHolder) {
//            if (isCurrentlyActive) {
                uiUtil.onDrawOver(c, recyclerView, viewHolder.viewForeground, dX, dY,
                        actionState, isCurrentlyActive)
//            }
        }
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView,
                             viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                             actionState: Int, isCurrentlyActive: Boolean) {
        if (viewHolder is ListViewAdapter.CategoryItemViewHolder) {
//            if (isCurrentlyActive) {
                val foregroundView = viewHolder.viewForeground
                uiUtil.onDraw(c, recyclerView, foregroundView, dX, dY,
                        actionState, isCurrentlyActive)
//            }
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//        Log.d(TAG, "onSwiped:: direction $direction")
//        listener.onSwiped(viewHolder, direction, viewHolder.adapterPosition)
    }

    //swipe threshold 75%
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder?): Float {
        return viewHolder?.let { it.itemView.width * 0.75f } ?: 0f
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    interface RecyclerItemTouchHelperListener {
        fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int)
    }

    companion object {
        const val TAG = "RecyclerItemTouchHelper"
    }
}
