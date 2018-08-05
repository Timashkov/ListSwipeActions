package com.swipelistactions.listitems

class ChildListItem(index: Int, val mParent: Int) : ListItem {
    val mText = "Child #$index of parent#$mParent"

    override fun getText(): String = mText
    override fun getBackText(): String = "$mText background"

    private var mIsCollapsed = false
    private var mHalfSwiped = false

    override fun setStateCollapsed(isCollapsed: Boolean) {
        mIsCollapsed = isCollapsed
    }

    override fun getStateCollapsed(): Boolean = mIsCollapsed

    override fun setStateHalfSwiped(isSwiped: Boolean) {
        mHalfSwiped = isSwiped
    }
}