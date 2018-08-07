package com.swipelistactions

import com.swipelistactions.listitems.ListItem


/**
 * Created by Aleksei Timashkov on 07.08.18.
 */
interface IListCallback {
    fun removeItem(item: ListItem)
    fun onItemClicked(item: ListItem)
}