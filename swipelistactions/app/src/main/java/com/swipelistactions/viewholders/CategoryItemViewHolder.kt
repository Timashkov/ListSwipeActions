package com.swipelistactions.viewholders

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.swipelistactions.IListCallback
import com.swipelistactions.ViewHolderSwipeHelper
import com.swipelistactions.animations.IOnMove
import com.swipelistactions.listitems.ListItem
import kotlinx.android.synthetic.main.v_category_item.view.*
import kotlin.math.absoluteValue

class CategoryItemViewHolder(val view: View, private val mListCallback: IListCallback) : RecyclerView.ViewHolder(view) {

    var viewHolderSwipeHelper: ViewHolderSwipeHelper? = null
    var thresholdToHalf: Float = 0.0f
    var thresholdToFull: Float = 0.0f
    private var isHalfState = false
    fun setupItem(listItem: ListItem) {

        view.category_text.text = listItem.getText() + if (listItem.isStateCollapsed()) " Collapsed" else " Expanded"
        view.category_text_background.text = listItem.getBackText()

        if (listItem.getStateHalfSwiped()) {
            view.viewForegroundCategory.translationX = -view.width * 0.5f
        } else {
            view.viewForegroundCategory.translationX = 0f
        }

        viewHolderSwipeHelper = ViewHolderSwipeHelper(listItem, this, view.viewForegroundCategory, object : IOnMove {

            override fun updateSwipedState(translateX: Float) {
                when {
                    translateX.absoluteValue >= thresholdToFull -> mListCallback.removeItem(listItem)
                    translateX.absoluteValue >= thresholdToHalf -> listItem.setStateHalfSwiped(true)
                    else -> listItem.setStateHalfSwiped(false)
                }
            }

            override fun getSwipeTarget(dx: Float, selectedFlags: Int): Float {
                var targetTranslateX = 0f
                val dirFlag = if (dx > 0) ViewHolderSwipeHelper.DIRECTION_RIGHT else ViewHolderSwipeHelper.DIRECTION_LEFT

                if (selectedFlags and dirFlag != 0 && Math.abs(dx) > thresholdToFull) {
                    targetTranslateX = Math.signum(dx) * view.width
                    Log.d("Category item vh" ,  " Full remove (threshold $thresholdToFull)")
                } else if (selectedFlags and dirFlag != 0 && Math.abs(dx) > thresholdToHalf) {
                    targetTranslateX = Math.signum(dx) * view.width * 0.5f
                    Log.d("Category item vh" ,  " Half swipe (threshold $thresholdToHalf)")
                }

                return targetTranslateX
            }

            override fun onClick() {
                mListCallback.onItemClicked(listItem)
            }
        })
    }

    fun stopCallbacks(){
        viewHolderSwipeHelper?.stopCallbacks()
        viewHolderSwipeHelper = null
    }

    fun refreshView(){
        if (isHalfState) {
            view.viewForegroundCategory.translationX = -view.viewForegroundCategory.width * 0.5f
            view.invalidate()
        } else {
            view.viewForegroundCategory.translationX = 0f
        }
        thresholdToHalf = view.viewForegroundCategory.width.toFloat() * 0.2f
        thresholdToFull = view.viewForegroundCategory.width.toFloat() * 0.7f
    }

}

