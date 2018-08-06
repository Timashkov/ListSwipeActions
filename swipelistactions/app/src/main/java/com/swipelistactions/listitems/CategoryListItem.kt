package com.swipelistactions.listitems

class CategoryListItem(private val mId: Int) : ListItem {

    private val mText = "Category #$mId"
    val mIndex = -1
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

    override fun getStateHalfSwiped(): Boolean = mHalfSwiped
}