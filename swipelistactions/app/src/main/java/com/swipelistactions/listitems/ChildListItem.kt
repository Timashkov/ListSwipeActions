package com.swipelistactions.listitems

class ChildListItem(private val mId: Int, private val mParentId: Int) : ListItem {
    val mText = "Child #$mId of parent#$mParentId"

    override fun getText(): String = mText
    override fun getBackText(): String = "$mText background"

    private var mIsCollapsed = false
    private var mHalfSwiped = false

    override fun setStateCollapsed(isCollapsed: Boolean) {
        mIsCollapsed = isCollapsed
    }

    override fun isStateCollapsed(): Boolean = mIsCollapsed

    override fun setStateHalfSwiped(isSwiped: Boolean) {
        mHalfSwiped = isSwiped
    }

    override fun getStateHalfSwiped(): Boolean = mHalfSwiped

    override fun getId(): Int = mId

    override fun getParentId(): Int = mParentId

    override fun isCategory(): Boolean = false
}