package com.swipelistactions.listitems

interface ListItem {
    fun getText(): String
    fun getBackText(): String

    fun setStateCollapsed(isCollapsed: Boolean)
    fun getStateCollapsed(): Boolean

    fun setStateHalfSwiped(isSwiped: Boolean)
    fun getStateHalfSwiped(): Boolean
}