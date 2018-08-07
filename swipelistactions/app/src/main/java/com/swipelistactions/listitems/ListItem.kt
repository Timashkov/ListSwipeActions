package com.swipelistactions.listitems

import com.swipelistactions.animations.IItemState

interface ListItem : IItemState{
    fun getText(): String
    fun getBackText(): String
    fun isCategory(): Boolean
    fun getId(): Int
    fun getParentId(): Int

    fun setStateCollapsed(isCollapsed: Boolean)
    fun isStateCollapsed(): Boolean

    fun setStateHalfSwiped(isSwiped: Boolean)
}