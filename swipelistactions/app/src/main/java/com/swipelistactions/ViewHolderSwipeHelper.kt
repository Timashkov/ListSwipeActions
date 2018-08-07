package com.swipelistactions

import android.animation.Animator
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import com.swipelistactions.animations.IItemState
import com.swipelistactions.animations.IOnMove
import java.util.ArrayList
import com.swipelistactions.animations.RecoverAnimation

/**
 * Created by Aleksei Timashkov on 06.08.18.
 */
class ViewHolderSwipeHelper(private val mItemState: IItemState, private val mViewHolder: RecyclerView.ViewHolder, private val mMovementView: View, private val mOnMove: IOnMove) {


    /**
     * The reference coordinates for the action start. For drag & drop, this is the time long
     * press is completed vs for swipe, this is the initial touch point.
     */
    private var mInitialTouchX: Float = 0.toFloat()
    private var mInitialTouchY: Float = 0.toFloat()

    /**
     * The diff between the last event and initial touch.
     */
    private var mDx: Float = 0.toFloat()

    private var mActionState = ACTION_STATE_IDLE
    /**
     * The coordinates of the selected view at the time it is selected. We record these values
     * when action starts so that we can consistently position it even if LayoutManager moves the
     * View.
     */
    private var mSelectedStartX: Float = 0.toFloat()
    private var mSelectedStartY: Float = 0.toFloat()

    private var mActivePointerId = ACTIVE_POINTER_ID_NONE

    private var mRecoverAnimations: MutableList<RecoverAnimation> = ArrayList()
    private var mSelected = false
    private var mSlop: Int = 0


    private val mOnItemTouchListener = View.OnTouchListener { v, event ->
        event?.let {
            val action = event.actionMasked

            when (action) {

                MotionEvent.ACTION_DOWN -> {
                    mActivePointerId = event.getPointerId(0)
                    mInitialTouchX = event.x - mMovementView.translationX
                    if (!mSelected) {
                        val animation = findAnimation()
                        if (animation != null) {
                            endRecoverAnimation(true)
                            select(animation.mActionState)
                            updateDxDy(event, 0)
                        }
                    }
                    return@OnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {

                    if (mActivePointerId == ACTIVE_POINTER_ID_NONE) {
                        return@OnTouchListener false
                    }

                    val activePointerIndex = event.findPointerIndex(mActivePointerId)
                    if (activePointerIndex >= 0) {
                        checkSelectForSwipe(action, event, activePointerIndex)
                    }
                    if (!mSelected) return@OnTouchListener false
                    // Find the index of the active pointer and fetch its position
                    if (activePointerIndex >= 0) {
                        updateDxDy(event, activePointerIndex)
                        return@OnTouchListener true
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    select(ACTION_STATE_IDLE)
                    mActivePointerId = ACTIVE_POINTER_ID_NONE
                    return@OnTouchListener true
                }
                // fall through
                MotionEvent.ACTION_UP -> {
                    select(ACTION_STATE_IDLE)
                    mActivePointerId = ACTIVE_POINTER_ID_NONE
                    return@OnTouchListener true
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    val pointerIndex = event.actionIndex
                    val pointerId = event.getPointerId(pointerIndex)
                    if (pointerId == mActivePointerId) {
                        val newPointerIndex = if (pointerIndex == 0) 1 else 0
                        mActivePointerId = event.getPointerId(newPointerIndex)
                        updateDxDy(event, pointerIndex)
                    }
                    return@OnTouchListener true
                }
                else -> return@OnTouchListener false
            }
        }
        false
    }

    private fun getSelectedFlags(): Int {
        var flag = DIRECTION_LEFT
        if (mItemState.getStateHalfSwiped())
            flag = flag or DIRECTION_RIGHT
        return flag
    }

    init {
        mViewHolder.itemView.setOnTouchListener(mOnItemTouchListener)
        val vc = ViewConfiguration.get(mViewHolder.itemView.context)
        mSlop = vc.scaledTouchSlop
    }

    private fun findAnimation(): RecoverAnimation? {
        if (mRecoverAnimations.isEmpty()) {
            return null
        }
        for (i in mRecoverAnimations.indices.reversed()) {
            val anim = mRecoverAnimations[i]
            if (anim.mViewHolder.itemView === mViewHolder.itemView) {
                return anim
            }
        }
        return null
    }


    /**
     * Checks whether we should select a View for swiping.
     */
    private fun checkSelectForSwipe(action: Int, motionEvent: MotionEvent, pointerIndex: Int): Boolean {
        if (mSelected || action != MotionEvent.ACTION_MOVE) {
            return false
        }

        // mDx and mDy are only set in allowed directions. We use custom x/y here instead of
        // updateDxDy to avoid swiping if user moves more in the other direction
        val x = motionEvent.getX(pointerIndex)
        val y = motionEvent.getY(pointerIndex)

        // Calculate the distance moved
        val dx = x - mInitialTouchX
        val dy = y - mInitialTouchY
        // swipe target is chose w/o applying flags so it does not really check if swiping in that
        // direction is allowed. This why here, we use mDx mDy to check slope value again.
        val absDx = Math.abs(dx)
        val absDy = Math.abs(dy)

        if (absDx < mSlop && absDy < mSlop) {
            return false
        }
        if (absDx < absDy) {
            return false
        }

        mActivePointerId = motionEvent.getPointerId(0)
        select(ACTION_STATE_SWIPE)
        return true
    }

    private fun select(actionState: Int) {
        if (actionState == mActionState) {
            return
        }

        val prevActionState = mActionState
        // prevent duplicate animations
        endRecoverAnimation(true)
        mActionState = actionState

        if (mSelected) {

            if (mViewHolder.itemView.parent != null) {

                val targetTranslateX = mOnMove.getSwipeTarget(mDx, getSelectedFlags())

                val tmpPosition = FloatArray(2)
                getSelectedDxDy(tmpPosition)
                val currentTranslateX = tmpPosition[0]

                val rv = object : RecoverAnimation(mViewHolder, mMovementView,
                        prevActionState, currentTranslateX, targetTranslateX) {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        if (this.mOverridden) {
                            return
                        }
                        mOnMove.updateSwipedState(targetTranslateX)
                    }
                }
                rv.setDuration(DEFAULT_SWIPE_ANIMATION_DURATION)
                mRecoverAnimations.add(rv)
                rv.start()
            }
            mSelected = false
        }
        if (actionState == ACTION_STATE_SWIPE) {
            mSelectedStartX = mViewHolder.itemView.left.toFloat()
            mSelectedStartY = mViewHolder.itemView.top.toFloat()
            mSelected = true
        }
    }


    private fun getSelectedDxDy(outPosition: FloatArray) {
        if (getSelectedFlags() and (DIRECTION_LEFT or DIRECTION_RIGHT) != 0) {
            outPosition[0] = mSelectedStartX + mDx - mMovementView.left
        } else {
            outPosition[0] = mMovementView.translationX
        }
    }


    private fun updateDxDy(ev: MotionEvent, pointerIndex: Int) {
        val x = ev.getX(pointerIndex)

        val directionFlags = getSelectedFlags()
        // Calculate the distance moved
        mDx = x - mInitialTouchX

        if (directionFlags and DIRECTION_RIGHT == 0) {
            mDx = Math.min(0f, mDx)
        }

        mMovementView.translationX = mDx
        Log.d(TAG, "New DX $mDx")
    }


    /**
     * Returns the animation type or 0 if cannot be found.
     */
    private fun endRecoverAnimation(override: Boolean) {
        val recoverAnimSize = mRecoverAnimations.size
        for (i in recoverAnimSize - 1 downTo 0) {
            val anim = mRecoverAnimations[i]
            if (anim.mViewHolder === mViewHolder) {
                anim.mOverridden = anim.mOverridden or override
                if (!anim.mEnded) {
                    anim.cancel()
                }
                mRecoverAnimations.removeAt(i)
                return
            }
        }
    }

    companion object {
        const val TAG = "ViewHolderSwipeHelper"
        const val DEFAULT_SWIPE_ANIMATION_DURATION = 250L
        const val DIRECTION_LEFT = 1 shl 2
        const val DIRECTION_RIGHT = 1 shl 3

        const val ACTIVE_POINTER_ID_NONE = -1
        const val ACTION_STATE_IDLE = 0
        const val ACTION_STATE_SWIPE = 1
    }
}
