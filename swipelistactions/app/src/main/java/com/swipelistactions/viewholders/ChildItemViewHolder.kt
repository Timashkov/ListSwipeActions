package com.swipelistactions.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View
import com.swipelistactions.IListCallback
import com.swipelistactions.ViewHolderSwipeHelper
import com.swipelistactions.animations.IOnMove
import com.swipelistactions.listitems.ListItem
import kotlinx.android.synthetic.main.v_child_item.view.*
import kotlin.math.absoluteValue

class ChildItemViewHolder(val view: View, private val mListCallback: IListCallback) : RecyclerView.ViewHolder(view) {

    var viewHolderSwipeHelper: ViewHolderSwipeHelper? = null
    val viewForeground = view.viewForeground

    fun setupItem(listItem: ListItem) {
        view.child_text.text = listItem.getText()
        view.child_text_background.text = listItem.getBackText()

        viewHolderSwipeHelper = ViewHolderSwipeHelper(listItem, this, viewForeground, object : IOnMove {

            val thresholdToHalf = viewForeground.width.toFloat() * 0.2f
            val thresholdToFull = viewForeground.width.toFloat() * 0.7f

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
                    targetTranslateX = Math.signum(dx) * viewForeground.width

                } else if (selectedFlags and dirFlag != 0 && Math.abs(dx) > thresholdToHalf) {
                    targetTranslateX = Math.signum(dx) * viewForeground.width * 0.5f
                }

                return targetTranslateX
            }
        })

        if (listItem.getStateHalfSwiped()) {
            viewForeground.translationX = -viewForeground.width * 0.5f
        } else {
            viewForeground.translationX = 0f
        }
    }
}

