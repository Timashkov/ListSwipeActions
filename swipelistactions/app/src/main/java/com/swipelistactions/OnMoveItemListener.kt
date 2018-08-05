package com.swipelistactions

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import kotlin.math.absoluteValue

class OnMoveItemListener : View.OnTouchListener {

    var mLastKnownPositionX = -1.0f
    var mLastKnownPositionY = -1.0f
    var isUserActionTracking = false

    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        motionEvent?.let {

            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    isUserActionTracking = true
                    mLastKnownPositionX = motionEvent.x
                    mLastKnownPositionY = motionEvent.y
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val diffX = diffX(motionEvent.x)
                    val diffY = diffY(motionEvent.y)

                    view?.let {
                        if (isUserActionTracking && diffX.absoluteValue > 0 && diffY.absoluteValue < it.height) {
                            it.translationX = diffX

                        }
                    }

                    return true
                }
                MotionEvent.ACTION_UP -> {
                    isUserActionTracking = false
                    val diffX = diffX(motionEvent.x)
                    val diffY = diffY(motionEvent.y)
                    if (diffX.absoluteValue < 10f && diffY.absoluteValue < 10f) {
                        view?.callOnClick()
                    } else {
                        view?.let {
                            if (it.translationX.absoluteValue > it.width * 0.4 && it.translationX.absoluteValue <= it.width * 0.75) {
                                val anim = RecoverAnimation(it, it.translationX, it.translationY, -it.width * 0.5f, 0f)
                                anim.start()
                            } else if (it.translationX.absoluteValue > it.width * 0.75) {
                                val anim = RecoverAnimation(it, it.translationX, it.translationY, -it.width.toFloat(), 0f)
                                anim.start()
                            } else {
                                val anim = RecoverAnimation(it, it.translationX, it.translationY, 0f, 0f)
                                anim.start()
                            }
                        }
                    }
                    return true
                }
                else ->
                    return false
            }
        }
        return false
    }

    private fun diffX(x: Float): Float {
        return x - mLastKnownPositionX

    }

    private fun diffY(y: Float): Float {
        return y - mLastKnownPositionY
    }

    private class RecoverAnimation internal constructor(internal val mView: View/*, internal val mAnimationType: Int,
                                                        internal val mActionState: Int*/, internal val mStartDx: Float, internal val mStartDy: Float, internal val mTargetX: Float, internal val mTargetY: Float) : Animator.AnimatorListener {

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
            mValueAnimator.addUpdateListener { animation ->
                setFraction(animation.animatedFraction)
                update()
            }
//            mValueAnimator.setTarget(mViewHolder.itemView)
            mValueAnimator.setTarget(mView)
            mValueAnimator.addListener(this)
            setFraction(0f)
            mValueAnimator.duration = 250
        }

        fun setDuration(duration: Long) {
            mValueAnimator.duration = duration
        }

        fun start() {
//            mViewHolder.setIsRecyclable(false)
            mValueAnimator.start()
        }

        fun cancel() {
            mValueAnimator.cancel()
        }

        fun setFraction(fraction: Float) {
            mFraction = fraction
        }

        fun draw(cv: Canvas) {
            mView.draw(cv)
            update()
        }

        /**
         * We run updates on onDraw method but use the fraction from animator callback.
         * This way, we can sync translate x/y values w/ the animators to avoid one-off frames.
         */
        fun update() {
            if (mStartDx == mTargetX) {
//                mX = mViewHolder.itemView.translationX
                mX = mView.translationX
            } else {
                mX = mStartDx + mFraction * (mTargetX - mStartDx)
            }
            if (mStartDy == mTargetY) {
//                mY = mViewHolder.itemView.translationY
                mY = mView.translationY
            } else {
                mY = mStartDy + mFraction * (mTargetY - mStartDy)
            }
            mView.translationX = mX
            mView.translationY = mY
        }

        override fun onAnimationStart(animation: Animator) {
            mEnded = false
        }

        override fun onAnimationEnd(animation: Animator) {
            if (!mEnded) {
//                mViewHolder.setIsRecyclable(true)
            }
            mEnded = true
        }

        override fun onAnimationCancel(animation: Animator) {
            setFraction(1f) //make sure we recover the view's state.
        }

        override fun onAnimationRepeat(animation: Animator) {

        }
    }
}