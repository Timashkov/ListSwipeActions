package com.swipelistactions.animations

/**
 * Created by Aleksei Timashkov on 07.08.18.
 */
interface IOnMove {
    fun updateSwipedState(translateX: Float)
    fun getSwipeTarget(dx: Float, selectedFlags: Int): Float
}