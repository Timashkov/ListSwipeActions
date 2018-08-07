package com.swipelistactions.animations

import android.animation.Animator
import android.animation.ValueAnimator
import android.support.v7.widget.RecyclerView
import android.view.View

open class RecoverAnimation internal constructor(internal val mViewHolder: RecyclerView.ViewHolder,
                                                         internal val mView: View,
                                                         val mActionState: Int,
                                                         internal val mStartDx: Float,
                                                         internal val mTargetX: Float) : Animator.AnimatorListener {

        private val mValueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
        private var mX: Float = 0.toFloat()

        // if user starts touching a recovering view, we put it into interaction mode again,
        // instantly.
        internal var mOverridden = false

        internal var mEnded = false

        private var mFraction: Float = 0.toFloat()

        init {
            mValueAnimator.addUpdateListener { animation ->
                setFraction(animation.animatedFraction)
                update()
            }
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
            mX = if (mStartDx == mTargetX) {
                mView.translationX
            } else {
                mStartDx + mFraction * (mTargetX - mStartDx)
            }
            mView.translationX = mX
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
