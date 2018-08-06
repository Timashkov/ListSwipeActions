package com.swipelistactions

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.support.v4.view.GestureDetectorCompat
import android.support.v7.recyclerview.R
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import com.swipelistactions.ItemTouchHelper.ACTIVE_POINTER_ID_NONE
import kotlin.math.absoluteValue

class CustomItemTouchHelper : RecyclerView.OnChildAttachStateChangeListener {
    override fun onChildViewDetachedFromWindow(view: View?) {

    }

    override fun onChildViewAttachedToWindow(view: View?) {

    }

//    var mLastKnownPositionX = -1.0f
//    var mLastKnownPositionY = -1.0f
//    var isUserActionTracking = false
//
//    /**
//     * Currently selected view holder
//     */
//    internal var mSelected: RecyclerView.ViewHolder? = null
//
//    /**
//     * Used to detect long press.
//     */
//    internal var mGestureDetector: GestureDetectorCompat? = null
//    internal var mRecyclerView: RecyclerView? = null
//
//    /**
//     * Set when ItemTouchHelper is assigned to a RecyclerView.
//     */
//    internal var mSwipeEscapeVelocity: Float = 0.toFloat()
//
//    /**
//     * Set when ItemTouchHelper is assigned to a RecyclerView.
//     */
//    internal var mMaxSwipeVelocity: Float = 0.toFloat()
//
//    /**
//     * The reference coordinates for the action start. For drag & drop, this is the time long
//     * press is completed vs for swipe, this is the initial touch point.
//     */
//    internal var mInitialTouchX: Float = 0.toFloat()
//
//    internal var mInitialTouchY: Float = 0.toFloat()
//
//    /**
//     * The diff between the last event and initial touch.
//     */
//    internal var mDx: Float = 0.toFloat()
//
//    internal var mDy: Float = 0.toFloat()
//
//    /**
//     * The coordinates of the selected view at the time it is selected. We record these values
//     * when action starts so that we can consistently position it even if LayoutManager moves the
//     * View.
//     */
//    internal var mSelectedStartX: Float = 0.toFloat()
//
//    internal var mSelectedStartY: Float = 0.toFloat()
//
//    /**
//     * The pointer we are tracking.
//     */
//    internal var mActivePointerId = ACTIVE_POINTER_ID_NONE
//
//    /**
//     * Used for detecting fling swipe
//     */
//    internal var mVelocityTracker: VelocityTracker? = null
//
//    internal fun obtainVelocityTracker() {
//        mVelocityTracker?.recycle()
//        mVelocityTracker = VelocityTracker.obtain()
//    }
//
//    private fun releaseVelocityTracker() {
//        mVelocityTracker?.recycle()
//        mVelocityTracker = null
//    }
//
//    private val mOnItemTouchListener = object : RecyclerView.OnItemTouchListener {
//        override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
//            mGestureDetector?.onTouchEvent(event)
//
//            val action = event.actionMasked
//            if (action == MotionEvent.ACTION_DOWN) {
//                mActivePointerId = event.getPointerId(0)
//                mInitialTouchX = event.x
//                mInitialTouchY = event.y
//                obtainVelocityTracker()
//                if (mSelected == null) {
//                    val animation = findAnimation(event)
//                    animation?.let{
//                        mInitialTouchX -= it.mX
//                        mInitialTouchY -= it.mY
//                        endRecoverAnimation(it.mViewHolder, true)
//                        if (mPendingCleanup.remove(it.mViewHolder.itemView)) {
//                            mCallback.clearView(mRecyclerView, it.mViewHolder)
//                        }
//                        select(it.mViewHolder, it.mActionState)
//                        updateDxDy(event, mSelectedFlags, 0)
//                    }
//                }
//            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
//                mActivePointerId = ACTIVE_POINTER_ID_NONE
//                select(null, ACTION_STATE_IDLE)
//            } else if (mActivePointerId != ACTIVE_POINTER_ID_NONE) {
//                // in a non scroll orientation, if distance change is above threshold, we
//                // can select the item
//                val index = event.findPointerIndex(mActivePointerId)
//                if (index >= 0) {
//                    checkSelectForSwipe(action, event, index)
//                }
//            }
//
//            mVelocityTracker?.addMovement(event)
//            return mSelected != null
//        }
//
//        override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
//            mGestureDetector?.onTouchEvent(event)
//            mVelocityTracker?.addMovement(event)
//
//            if (mActivePointerId == ACTIVE_POINTER_ID_NONE) {
//                return
//            }
//            val action = event.actionMasked
//            val activePointerIndex = event.findPointerIndex(mActivePointerId)
//            if (activePointerIndex >= 0) {
//                checkSelectForSwipe(action, event, activePointerIndex)
//            }
//            val viewHolder = mSelected ?: return
//            when (action) {
//                MotionEvent.ACTION_MOVE -> {
//                    // Find the index of the active pointer and fetch its position
//                    if (activePointerIndex >= 0) {
//                        updateDxDy(event, mSelectedFlags, activePointerIndex)
//                        moveIfNecessary(viewHolder)
//                        mRecyclerView.removeCallbacks(mScrollRunnable)
//                        mScrollRunnable.run()
//                        mRecyclerView.invalidate()
//                    }
//                }
//                MotionEvent.ACTION_CANCEL -> {
//                        mVelocityTracker?.clear()
//                    select(null, ACTION_STATE_IDLE)
//                    mActivePointerId = ACTIVE_POINTER_ID_NONE
//                }
//                // fall through
//                MotionEvent.ACTION_UP -> {
//                    select(null, ACTION_STATE_IDLE)
//                    mActivePointerId = ACTIVE_POINTER_ID_NONE
//                }
//                MotionEvent.ACTION_POINTER_UP -> {
//                    val pointerIndex = event.actionIndex
//                    val pointerId = event.getPointerId(pointerIndex)
//                    if (pointerId == mActivePointerId) {
//                        // This was our active pointer going up. Choose a new
//                        // active pointer and adjust accordingly.
//                        val newPointerIndex = if (pointerIndex == 0) 1 else 0
//                        mActivePointerId = event.getPointerId(newPointerIndex)
//                        updateDxDy(event, mSelectedFlags, pointerIndex)
//                    }
//                }
//            }
//        }
//
//        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
//            if (!disallowIntercept) {
//                return
//            }
//            select(null, ACTION_STATE_IDLE)
//        }
//    }
//
//    /**
//     * Returns the animation type or 0 if cannot be found.
//     */
//    internal fun endRecoverAnimation(viewHolder: RecyclerView.ViewHolder, override: Boolean): Int {
//        val recoverAnimSize = mRecoverAnimations.size
//        for (i in recoverAnimSize - 1 downTo 0) {
//            val anim = mRecoverAnimations.get(i)
//            if (anim.mViewHolder === viewHolder) {
//                anim.mOverridden = anim.mOverridden or override
//                if (!anim.mEnded) {
//                    anim.cancel()
//                }
//                mRecoverAnimations.removeAt(i)
//                return anim.mAnimationType
//            }
//        }
//        return 0
//    }
//
//    internal fun findAnimation(event: MotionEvent): RecoverAnimation? {
//        if (mRecoverAnimations.isEmpty()) {
//            return null
//        }
//        val target = findChildView(event)
//        for (i in mRecoverAnimations.indices.reversed()) {
//            val anim = mRecoverAnimations.get(i)
//            if (anim.mViewHolder.itemView === target) {
//                return anim
//            }
//        }
//        return null
//    }
//
//    override fun onChildViewAttachedToWindow(view: View) {}
//
//    override fun onChildViewDetachedFromWindow(view: View) {
//        removeChildDrawingOrderCallbackIfNecessary(view)
//        val holder = mRecyclerView?.getChildViewHolder(view) ?: return
//        if (mSelected != null && holder === mSelected) {
//            select(null, ACTION_STATE_IDLE)
//        } else {
//            endRecoverAnimation(holder, false) // this may push it into pending cleanup list.
//            if (mPendingCleanup.remove(holder.itemView)) {
//                mCallback.clearView(mRecyclerView, holder)
//            }
//        }
//    }
//
////    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
////        motionEvent?.let {
////
////            when (motionEvent.action) {
////                MotionEvent.ACTION_DOWN -> {
////                    isUserActionTracking = true
////                    mLastKnownPositionX = motionEvent.x
////                    mLastKnownPositionY = motionEvent.y
////                    return true
////                }
////                MotionEvent.ACTION_MOVE -> {
////                    val diffX = diffX(motionEvent.x)
////                    val diffY = diffY(motionEvent.y)
////
////                    view?.let {
////                        if (isUserActionTracking && diffX.absoluteValue > 0 && diffY.absoluteValue < it.height) {
////                            it.translationX = diffX
////
////                        }
////                    }
////
////                    return true
////                }
////                MotionEvent.ACTION_UP -> {
////                    isUserActionTracking = false
////                    val diffX = diffX(motionEvent.x)
////                    val diffY = diffY(motionEvent.y)
////                    if (diffX.absoluteValue < 10f && diffY.absoluteValue < 10f) {
////                        view?.callOnClick()
////                    } else {
////                        view?.let {
////                            if (it.translationX.absoluteValue > it.width * 0.4 && it.translationX.absoluteValue <= it.width * 0.75) {
////                                val anim = RecoverAnimation(it, it.translationX, it.translationY, -it.width * 0.5f, 0f)
////                                anim.start()
////                            } else if (it.translationX.absoluteValue > it.width * 0.75) {
////                                val anim = RecoverAnimation(it, it.translationX, it.translationY, -it.width.toFloat(), 0f)
////                                anim.start()
////                            } else {
////                                val anim = RecoverAnimation(it, it.translationX, it.translationY, 0f, 0f)
////                                anim.start()
////                            }
////                        }
////                    }
////                    return true
////                }
////                else ->
////                    return false
////            }
////        }
////        return false
////    }
//
//    fun attachToRecyclerView(recyclerView: RecyclerView) {
//        if (mRecyclerView === recyclerView) {
//            return  // nothing to do
//        }
//        if (mRecyclerView != null) {
//            destroyCallbacks()
//        }
//        mRecyclerView = recyclerView
//
//        val resources = recyclerView.resources
//        mSwipeEscapeVelocity = resources
//                .getDimension(R.dimen.item_touch_helper_swipe_escape_velocity)
//        mMaxSwipeVelocity = resources
//                .getDimension(R.dimen.item_touch_helper_swipe_escape_max_velocity)
//        setupCallbacks()
//    }
//
//    private fun setupCallbacks() {
////        val vc = ViewConfiguration.get(mRecyclerView.getContext())
////        mSlop = vc.scaledTouchSlop
////        mRecyclerView?.addItemDecoration(this)
//        mRecyclerView?.addOnItemTouchListener(mOnItemTouchListener)
//        mRecyclerView?.addOnChildAttachStateChangeListener(this)
//        startGestureDetection()
//    }
//
//    private fun destroyCallbacks() {
//        mRecyclerView?.removeItemDecoration(this)
//        mRecyclerView?.removeOnItemTouchListener(mOnItemTouchListener)
//        mRecyclerView?.removeOnChildAttachStateChangeListener(this)
//        // clean all attached
//        val recoverAnimSize = mRecoverAnimations.size
//        for (i in recoverAnimSize - 1 downTo 0) {
//            val recoverAnimation = mRecoverAnimations.get(0)
//            mCallback.clearView(mRecyclerView, recoverAnimation.mViewHolder)
//        }
//        mRecoverAnimations.clear()
//        mOverdrawChild = null
//        mOverdrawChildPosition = -1
//        releaseVelocityTracker()
//        stopGestureDetection()
//    }
//
//
//    private var mItemTouchHelperGestureListener: ItemTouchHelperGestureListener? = null
//
//    private fun startGestureDetection() {
//        mItemTouchHelperGestureListener = ItemTouchHelperGestureListener()
//        mGestureDetector = GestureDetectorCompat(mRecyclerView?.context,
//                mItemTouchHelperGestureListener)
//    }
//
//    private fun stopGestureDetection() {
//        if (mItemTouchHelperGestureListener != null) {
//            mItemTouchHelperGestureListener.doNotReactToLongPress()
//            mItemTouchHelperGestureListener = null
//        }
//        if (mGestureDetector != null) {
//            mGestureDetector = null
//        }
//    }
//
//
//    private fun diffX(x: Float): Float {
//        return x - mLastKnownPositionX
//
//    }
//
//    private fun diffY(y: Float): Float {
//        return y - mLastKnownPositionY
//    }
//
//    private class RecoverAnimation internal constructor(internal val mView: View/*, internal val mAnimationType: Int,
//                                                        internal val mActionState: Int*/, internal val mStartDx: Float, internal val mStartDy: Float, internal val mTargetX: Float, internal val mTargetY: Float) : Animator.AnimatorListener {
//
//        private val mValueAnimator: ValueAnimator
//
//        var mIsPendingCleanup: Boolean = false
//
//        internal var mX: Float = 0.toFloat()
//
//        internal var mY: Float = 0.toFloat()
//
//        // if user starts touching a recovering view, we put it into interaction mode again,
//        // instantly.
//        internal var mOverridden = false
//
//        internal var mEnded = false
//
//        private var mFraction: Float = 0.toFloat()
//
//        init {
//            mValueAnimator = ValueAnimator.ofFloat(0f, 1f)
//            mValueAnimator.addUpdateListener { animation ->
//                setFraction(animation.animatedFraction)
//                update()
//            }
////            mValueAnimator.setTarget(mViewHolder.itemView)
//            mValueAnimator.setTarget(mView)
//            mValueAnimator.addListener(this)
//            setFraction(0f)
//            mValueAnimator.duration = 250
//        }
//
//        fun setDuration(duration: Long) {
//            mValueAnimator.duration = duration
//        }
//
//        fun start() {
////            mViewHolder.setIsRecyclable(false)
//            mValueAnimator.start()
//        }
//
//        fun cancel() {
//            mValueAnimator.cancel()
//        }
//
//        fun setFraction(fraction: Float) {
//            mFraction = fraction
//        }
//
//        fun draw(cv: Canvas) {
//            mView.draw(cv)
//            update()
//        }
//
//        /**
//         * We run updates on onDraw method but use the fraction from animator callback.
//         * This way, we can sync translate x/y values w/ the animators to avoid one-off frames.
//         */
//        fun update() {
//            if (mStartDx == mTargetX) {
////                mX = mViewHolder.itemView.translationX
//                mX = mView.translationX
//            } else {
//                mX = mStartDx + mFraction * (mTargetX - mStartDx)
//            }
//            if (mStartDy == mTargetY) {
////                mY = mViewHolder.itemView.translationY
//                mY = mView.translationY
//            } else {
//                mY = mStartDy + mFraction * (mTargetY - mStartDy)
//            }
//            mView.translationX = mX
//            mView.translationY = mY
//        }
//
//        override fun onAnimationStart(animation: Animator) {
//            mEnded = false
//        }
//
//        override fun onAnimationEnd(animation: Animator) {
//            if (!mEnded) {
////                mViewHolder.setIsRecyclable(true)
//            }
//            mEnded = true
//        }
//
//        override fun onAnimationCancel(animation: Animator) {
//            setFraction(1f) //make sure we recover the view's state.
//        }
//
//        override fun onAnimationRepeat(animation: Animator) {
//
//        }
//    }
}