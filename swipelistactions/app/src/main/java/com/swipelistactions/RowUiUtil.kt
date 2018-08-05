package com.swipelistactions

import android.graphics.Canvas
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchUIUtil
import android.view.View

class RowUiUtil : ItemTouchUIUtil {
    override fun onDrawOver(c: Canvas, recyclerView: RecyclerView,
                            view: View, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

    }

    override fun clearView(view: View) {
        val tag = view.getTag(R.id.item_touch_helper_previous_elevation)
        if (tag != null && tag is Float) {
            ViewCompat.setElevation(view, tag)
        }
        view.setTag(R.id.item_touch_helper_previous_elevation, null)
        view.translationX = 0f
        view.translationY = 0f
    }

    fun setHalfSwiped(view: View) {
        val tag = view.getTag(R.id.item_touch_helper_previous_elevation)
        if (tag != null && tag is Float) {
            ViewCompat.setElevation(view, tag)
        }
        view.setTag(R.id.item_touch_helper_previous_elevation, null)
        view.translationX = -view.width * 0.5f
        view.translationY = 0f
    }

    override fun onDraw(c: Canvas, recyclerView: RecyclerView, view: View,
                        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if (isCurrentlyActive) {
            var originalElevation = view.getTag(R.id.item_touch_helper_previous_elevation)
            if (originalElevation == null) {
                originalElevation = ViewCompat.getElevation(view)
                val newElevation = 1f + findMaxElevation(recyclerView, view)
                ViewCompat.setElevation(view, newElevation)
                view.setTag(R.id.item_touch_helper_previous_elevation, originalElevation)
            }
        }
        view.translationX = dX
        view.translationY = dY
    }

    override fun onSelected(view: View) {

    }

    private fun findMaxElevation(recyclerView: RecyclerView, itemView: View): Float {
        val childCount = recyclerView.childCount
        var max = 0f
        for (i in 0 until childCount) {
            val child = recyclerView.getChildAt(i)
            if (child === itemView) {
                continue
            }
            val elevation = ViewCompat.getElevation(child)
            if (elevation > max) {
                max = elevation
            }
        }
        return max
    }

}