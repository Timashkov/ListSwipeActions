package com.swipelistactions

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.view.*
import com.swipelistactions.ItemTouchHelper.*
import com.swipelistactions.listitems.ListItem
import java.util.ArrayList

/**
 * Created by Aleksei Timashkov on 06.08.18.
 */
class ViewHolderSwipeHelper(private val mItem: ListItem, private val mViewHolder: RecyclerView.ViewHolder, private val mMovementView: View) {

    val DEFAULT_SWIPE_ANIMATION_DURATION = 250L
    /**
     * Left direction, used for swipe & drag control.
     */
    val LEFT = 1 shl 2

    /**
     * Right direction, used for swipe & drag control.
     */
    val RIGHT = 1 shl 3

    // If you change these relative direction values, update Callback#convertToAbsoluteDirection,
    // Callback#convertToRelativeDirection.
    /**
     * Horizontal start direction. Resolved to LEFT or RIGHT depending on RecyclerView's layout
     * direction. Used for swipe & drag control.
     */
    val START = LEFT shl 2

    /**
     * Horizontal end direction. Resolved to LEFT or RIGHT depending on RecyclerView's layout
     * direction. Used for swipe & drag control.
     */
    val END = RIGHT shl 2

    /**
     * ItemTouchHelper is in idle state. At this state, either there is no related motion event by
     * the user or latest motion events have not yet triggered a swipe or drag.
     */
    val ACTION_STATE_IDLE = 0

    /**
     * A View is currently being swiped.
     */
    val ACTION_STATE_SWIPE = 1

    /**
     * Animation type for views which are swiped successfully.
     */
    val ANIMATION_TYPE_SWIPE_SUCCESS = 1 shl 1

    /**
     * Animation type for views which are not completely swiped thus will animate back to their
     * original position.
     */
    val ANIMATION_TYPE_SWIPE_CANCEL = 1 shl 2


    /**
     * The reference coordinates for the action start. For drag & drop, this is the time long
     * press is completed vs for swipe, this is the initial touch point.
     */
    internal var mInitialTouchX: Float = 0.toFloat()

    internal var mInitialTouchY: Float = 0.toFloat()

    /**
     * Set when ItemTouchHelper is assigned to a RecyclerView.
     */
    internal var mSwipeEscapeVelocity: Float = 0.toFloat()

    /**
     * Set when ItemTouchHelper is assigned to a RecyclerView.
     */
    internal var mMaxSwipeVelocity: Float = 0.toFloat()

    /**
     * The diff between the last event and initial touch.
     */
    internal var mDx: Float = 0.toFloat()

    internal var mDy: Float = 0.toFloat()

    internal val mPendingCleanup: MutableList<View> = ArrayList()

    internal var mActionState = ACTION_STATE_IDLE
    /**
     * The coordinates of the selected view at the time it is selected. We record these values
     * when action starts so that we can consistently position it even if LayoutManager moves the
     * View.
     */
    internal var mSelectedStartX: Float = 0.toFloat()

    internal var mSelectedStartY: Float = 0.toFloat()

    /**
     * The direction flags obtained from unmasking
     * [Callback.getAbsoluteMovementFlags] for the current
     * action state.
     */
    internal var mSelectedFlags: Int = LEFT

    private val PIXELS_PER_SECOND = 1000

    private var mVelocityTracker: VelocityTracker? = null
    private var mActivePointerId = ACTIVE_POINTER_ID_NONE

    private var mRecoverAnimations: MutableList<RecoverAnimation> = ArrayList()
    private var mSelected = false

    private val mOnItemTouchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            event?.let {
                val action = event.actionMasked

                when (action) {

                    MotionEvent.ACTION_DOWN -> {
                        mActivePointerId = event.getPointerId(0)
                        mInitialTouchX = event.x + mMovementView.translationX
                        mInitialTouchY = event.y
                        obtainVelocityTracker()
                        if (!mSelected) {
                            val animation = findAnimation(event)
                            if (animation != null) {
                                mInitialTouchX -= animation.mX
                                mInitialTouchY -= animation.mY
                                endRecoverAnimation(true)
                                if (mPendingCleanup.remove(animation.mViewHolder.itemView)) {
//                                    mCallback.clearView(mRecyclerView, animation.mViewHolder)
                                }
                                select(animation.mActionState)
                                updateDxDy(event, mSelectedFlags, 0)
                            }
                        }
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {

                        mVelocityTracker?.addMovement(event)
                        if (mActivePointerId == ItemTouchHelper.ACTIVE_POINTER_ID_NONE) {
                            return false
                        }

                        val activePointerIndex = event.findPointerIndex(mActivePointerId)
                        if (activePointerIndex >= 0) {
                            checkSelectForSwipe(action, event, activePointerIndex)
                        }
                        if (!mSelected) return false
                        // Find the index of the active pointer and fetch its position
                        if (activePointerIndex >= 0) {
                            updateDxDy(event, mSelectedFlags, activePointerIndex)
                            return true
                        }
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        mVelocityTracker?.clear()
                        select(ACTION_STATE_IDLE)
                        mActivePointerId = ItemTouchHelper.ACTIVE_POINTER_ID_NONE
                        return true
                    }
                    // fall through
                    MotionEvent.ACTION_UP -> {
                        select(ACTION_STATE_IDLE)
                        mActivePointerId = ItemTouchHelper.ACTIVE_POINTER_ID_NONE
                        return true
                    }
                    MotionEvent.ACTION_POINTER_UP -> {
                        val pointerIndex = event.actionIndex
                        val pointerId = event.getPointerId(pointerIndex)
                        if (pointerId == mActivePointerId) {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            mActivePointerId = event.getPointerId(newPointerIndex)
                            updateDxDy(event, mSelectedFlags, pointerIndex)
                        }
                        return true
                    }
                    else -> return false
                }
            }
            return false
        }


    }

    private var mSlop: Int = 0

    init {
        mViewHolder.itemView.setOnTouchListener(mOnItemTouchListener)
        val vc = ViewConfiguration.get(mViewHolder.itemView.context)
        mSlop = vc.scaledTouchSlop
    }

    internal fun obtainVelocityTracker() {
        mVelocityTracker?.recycle()
        mVelocityTracker = VelocityTracker.obtain()
    }

    private fun releaseVelocityTracker() {
        mVelocityTracker?.recycle()
        mVelocityTracker = null

    }

    private fun findAnimation(event: MotionEvent): RecoverAnimation? {
        if (mRecoverAnimations.isEmpty()) {
            return null
        }
        for (i in mRecoverAnimations.indices.reversed()) {
            val anim = mRecoverAnimations.get(i)
            if (anim.mViewHolder.itemView === mViewHolder.itemView) {
                return anim
            }
        }
        return null
    }


    /**
     * Checks whether we should select a View for swiping.
     */
    internal fun checkSelectForSwipe(action: Int, motionEvent: MotionEvent, pointerIndex: Int): Boolean {
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

    internal fun select(actionState: Int) {
        if (actionState == mActionState) {
            return
        }

        val prevActionState = mActionState
        // prevent duplicate animations
        endRecoverAnimation(true)
        mActionState = actionState

        val actionStateMask = (1 shl DIRECTION_FLAG_COUNT + DIRECTION_FLAG_COUNT * actionState) - 1
        var preventLayout = false

        if (mSelected) {

            if (mViewHolder.itemView.parent != null) {
                val swipeDir = checkHorizontalSwipe(LEFT)
                releaseVelocityTracker()
                // find where we should animate to
                val targetTranslateX: Float
                val targetTranslateY: Float
                val animationType: Int
                when (swipeDir) {
                    LEFT, RIGHT, START, END -> {
                        targetTranslateY = 0f
                        targetTranslateX = Math.signum(mDx) * mViewHolder.itemView.width
                    }
                    else -> {
                        targetTranslateX = 0f
                        targetTranslateY = 0f
                    }
                }
                if (swipeDir > 0) {
                    animationType = ANIMATION_TYPE_SWIPE_SUCCESS
                } else {
                    animationType = ANIMATION_TYPE_SWIPE_CANCEL
                }
                val tmpPosition = FloatArray(2)
                getSelectedDxDy(tmpPosition)
                val currentTranslateX = tmpPosition[0]
                val currentTranslateY = tmpPosition[1]


                val rv = object : RecoverAnimation(mViewHolder, mMovementView, animationType,
                        prevActionState, currentTranslateX, currentTranslateY,
                        targetTranslateX, targetTranslateY) {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        if (this.mOverridden) {
                            return
                        }
                        if (swipeDir <= 0) {
                            // this is a drag or failed swipe. recover immediately
//                            mCallback.clearView(mRecyclerView, prevSelected)
                            mMovementView.translationX = 0f
                            mMovementView.translationY = 0f
                            // full cleanup will happen on onDrawOver
                        } else {
                            // wait until remove animation is complete.
                            mPendingCleanup.add(mViewHolder.itemView)
                            mIsPendingCleanup = true
                            if (swipeDir > 0) {
                                // Animation might be ended by other animators during a layout.
                                // We defer callback to avoid editing adapter during a layout.
//                                postDispatchSwipe(this, swipeDir)
                            }
                        }
                        // removed from the list after it is drawn for the last time
//                        if (mOverdrawChild === prevSelected.itemView) {
//                            removeChildDrawingOrderCallbackIfNecessary(prevSelected.itemView)
//                        }
                    }
                }
                rv.setDuration(DEFAULT_SWIPE_ANIMATION_DURATION)
                mRecoverAnimations.add(rv)
                rv.start()
                preventLayout = true
            } else {
//                removeChildDrawingOrderCallbackIfNecessary(prevSelected.itemView)
//                mCallback.clearView(mRecyclerView, prevSelected)
            }
            mSelected = false
        }
        if (actionState == ACTION_STATE_SWIPE) {
            mSelectedFlags = LEFT
            mSelectedStartX = mViewHolder.itemView.left.toFloat()
            mSelectedStartY = mViewHolder.itemView.top.toFloat()
            mSelected = true

        }

//        if (!preventLayout) {
//            mRecyclerView.getLayoutManager().requestSimpleAnimationsInNextLayout()
//        }
//        mCallback.onSelectedChanged(mSelected, mActionState)
//        mRecyclerView.invalidate()
    }

    //    internal fun postDispatchSwipe(anim: RecoverAnimation, swipeDir: Int) {
//        // wait until animations are complete.
//        mRecyclerView.post(object : Runnable {
//            override fun run() {
//                if (mRecyclerView != null && mRecyclerView.isAttachedToWindow()
//                        && !anim.mOverridden
//                        && anim.mViewHolder.adapterPosition != RecyclerView.NO_POSITION) {
//                    val animator = mRecyclerView.getItemAnimator()
//                    // if animator is running or we have other active recover animations, we try
//                    // not to call onSwiped because DefaultItemAnimator is not good at merging
//                    // animations. Instead, we wait and batch.
//                    if ((animator == null || !animator!!.isRunning(null)) && !hasRunningRecoverAnim()) {
//                        mCallback.onSwiped(anim.mViewHolder, swipeDir)
//                    } else {
//                        mRecyclerView.post(this)
//                    }
//                }
//            }
//        })
//    }
//
    private fun getSelectedDxDy(outPosition: FloatArray) {
        if (mSelectedFlags and (LEFT or RIGHT) != 0) {
            outPosition[0] = mSelectedStartX + mDx - mViewHolder.itemView.left
        } else {
            outPosition[0] = mViewHolder.itemView.translationX
        }
        if (mSelectedFlags and (UP or DOWN) != 0) {
            outPosition[1] = mSelectedStartY + mDy - mViewHolder.itemView.top
        } else {
            outPosition[1] = mViewHolder.itemView.translationY
        }
    }

    private fun checkHorizontalSwipe(flags: Int): Int {
        if (flags and (LEFT or RIGHT) != 0) {
            val dirFlag = if (mDx > 0) RIGHT else LEFT
            mVelocityTracker?.let { tracker ->
                if (mActivePointerId > -1) {
                    tracker.computeCurrentVelocity(PIXELS_PER_SECOND, mMaxSwipeVelocity)
                    val xVelocity = tracker.getXVelocity(mActivePointerId)
                    val yVelocity = tracker.getYVelocity(mActivePointerId)
                    val velDirFlag = if (xVelocity > 0f) RIGHT else LEFT
                    val absXVelocity = Math.abs(xVelocity)
                    if (velDirFlag and flags != 0 && dirFlag == velDirFlag
                            && absXVelocity >= mSwipeEscapeVelocity
                            && absXVelocity > Math.abs(yVelocity)) {
                        return velDirFlag
                    }
                }
            }

            val threshold = mViewHolder.itemView.width * 0.5

            if (flags and dirFlag != 0 && Math.abs(mDx) > threshold) {
                return dirFlag
            }
        }
        return 0
    }


    internal fun updateDxDy(ev: MotionEvent, directionFlags: Int, pointerIndex: Int) {
        val x = ev.getX(pointerIndex)
        val y = ev.getY(pointerIndex)

        // Calculate the distance moved
        mDx = x - mInitialTouchX
        mDy = 0f
        if (directionFlags and LEFT == 0) {
            mDx = Math.max(0f, mDx)
        }
        if (directionFlags and RIGHT == 0) {
            mDx = Math.min(0f, mDx)
        }

        mMovementView.translationX = mDx
        mMovementView.translationY = mDy
    }

//    private fun swipeIfNecessary(): Int {
//
//        val originalMovementFlags = LEFT
//        val absoluteMovementFlags =LEFT
//        val flags = LEFT
//        if (flags == 0) {
//            return 0
//        }
//        val originalFlags = originalMovementFlags and ACTION_MODE_SWIPE_MASK shr ACTION_STATE_SWIPE * DIRECTION_FLAG_COUNT
//        var swipeDir: Int
//        if (Math.abs(mDx) > Math.abs(mDy)) {
//            if ((swipeDir = checkHorizontalSwipe(flags)) > 0) {
//                // if swipe dir is not in original flags, it should be the relative direction
//                return if (originalFlags and swipeDir == 0) {
//                    // convert to relative
//                    Callback.convertToRelativeDirection(swipeDir,
//                            ViewCompat.getLayoutDirection(mRecyclerView))
//                } else swipeDir
//            }
//
//        }
//        return 0
//    }

    private open class RecoverAnimation internal constructor(internal val mViewHolder: RecyclerView.ViewHolder,
                                                             internal val mView: View,
                                                             internal val mAnimationType: Int,
                                                             internal val mActionState: Int,
                                                             internal val mStartDx: Float,
                                                             internal val mStartDy: Float,
                                                             internal val mTargetX: Float,
                                                             internal val mTargetY: Float) : Animator.AnimatorListener {

        private val mValueAnimator: ValueAnimator

        var mIsPendingCleanup: Boolean = false

        internal var mX: Float = 0.toFloat()

        internal var mY: Float = 0.toFloat()

        // if user starts touching a recovering view, we put it into interaction mode again,
        // instantly.
        internal var mOverridden = false

        internal var mEnded = false

        private var mFraction: Float = 0.toFloat()

        init {
            mValueAnimator = ValueAnimator.ofFloat(0f, 1f)
            mValueAnimator.addUpdateListener { animation -> setFraction(animation.animatedFraction)
            update()}
            mValueAnimator.setTarget(mView)
            mValueAnimator.addListener(this)
            setFraction(0f)
        }

        fun setDuration(duration: Long) {
            mValueAnimator.duration = duration
        }

        fun start() {
            mViewHolder.setIsRecyclable(false)
            mValueAnimator.start()
        }

        fun cancel() {
            mValueAnimator.cancel()
        }

        fun setFraction(fraction: Float) {
            mFraction = fraction
        }

        /**
         * We run updates on onDraw method but use the fraction from animator callback.
         * This way, we can sync translate x/y values w/ the animators to avoid one-off frames.
         */
        fun update() {
            if (mStartDx == mTargetX) {
                mX = mViewHolder.itemView.translationX
            } else {
                mX = mStartDx + mFraction * (mTargetX - mStartDx)
            }
            if (mStartDy == mTargetY) {
                mY = mViewHolder.itemView.translationY
            } else {
                mY = mStartDy + mFraction * (mTargetY - mStartDy)
            }
            mView.translationX = mX
            mView.translationY = mY
        }

        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationEnd(animation: Animator) {
            if (!mEnded) {
                mViewHolder.setIsRecyclable(true)
            }
            mEnded = true
        }

        override fun onAnimationCancel(animation: Animator) {
            setFraction(1f) //make sure we recover the view's state.
        }

        override fun onAnimationRepeat(animation: Animator) {

        }
    }

    /**
     * Returns the animation type or 0 if cannot be found.
     */
    internal fun endRecoverAnimation(override: Boolean): Int {
        val recoverAnimSize = mRecoverAnimations.size
        for (i in recoverAnimSize - 1 downTo 0) {
            val anim = mRecoverAnimations.get(i)
            if (anim.mViewHolder === mViewHolder) {
                anim.mOverridden = anim.mOverridden or override
                if (!anim.mEnded) {
                    anim.cancel()
                }
                mRecoverAnimations.removeAt(i)
                return anim.mAnimationType
            }
        }
        return 0
    }
}
