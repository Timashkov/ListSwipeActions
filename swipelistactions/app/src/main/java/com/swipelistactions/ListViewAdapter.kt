package com.swipelistactions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.swipelistactions.listitems.CategoryListItem
import com.swipelistactions.listitems.ListItem
import kotlinx.android.synthetic.main.v_category_item.view.*
import kotlinx.android.synthetic.main.v_child_item.view.*

class ListViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val mItems = ArrayList<ListItem>()

    fun setData(items: List<ListItem>) {
        mItems.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ViewTypes.CATEGORY.ordinal) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.v_category_item, parent, false)
            CategoryItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.v_child_item, parent, false)
            ChildItemViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mItems[position] is CategoryListItem) {
            ViewTypes.CATEGORY.ordinal
        } else {
            ViewTypes.CHILD.ordinal
        }
    }

    override fun getItemCount(): Int = mItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? CategoryItemViewHolder)?.setupItem(mItems[position])
                ?: (holder as? ChildItemViewHolder)?.setupItem(mItems[position])
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        (holder as? CategoryItemViewHolder)?.stopCallbacks()
                ?: (holder as? ChildItemViewHolder)?.stopCallbacks()
    }

    internal class CategoryItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        var viewHolderSwipeHelper: ViewHolderSwipeHelper? = null
        val viewForeground = view.viewForegroundCategory
        fun setupItem(listItem: ListItem) {
            view.category_text.text = listItem.getText()
            view.category_text_background.text = listItem.getBackText()

            if (listItem.getStateHalfSwiped()) {
                viewForeground.translationX = -viewForeground.width * 0.5f
            } else {
                viewForeground.translationX = 0f
            }

            viewHolderSwipeHelper = ViewHolderSwipeHelper(listItem, this, viewForeground)

        }

        fun stopCallbacks() {}
    }

    internal class ChildItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var viewHolderSwipeHelper: ViewHolderSwipeHelper? = null
        val viewForeground = view.viewForeground
        fun setupItem(listItem: ListItem) {
            view.child_text.text = listItem.getText()
            view.child_text_background.text = listItem.getBackText()

            viewHolderSwipeHelper = ViewHolderSwipeHelper(listItem, this, viewForeground)

            if (listItem.getStateHalfSwiped()) {
                viewForeground.translationX = -viewForeground.width * 0.5f
            } else {
                viewForeground.translationX = 0f
            }
        }

        fun stopCallbacks() {}
    }

    enum class ViewTypes {
        CATEGORY, CHILD
    }
}