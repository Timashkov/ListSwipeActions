package com.swipelistactions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.swipelistactions.listitems.ListItem
import com.swipelistactions.viewholders.CategoryItemViewHolder
import com.swipelistactions.viewholders.ChildItemViewHolder

class ListViewAdapter(private val listCallback: IListCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }
    private val mItems = ArrayList<ListItem>()

    fun setData(items: List<ListItem>) {
        mItems.clear()
        mItems.addAll(items)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ViewTypes.CATEGORY.ordinal) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.v_category_item, parent, false)
            CategoryItemViewHolder(view, listCallback)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.v_child_item, parent, false)
            ChildItemViewHolder(view, listCallback)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mItems[position].isCategory()) {
            ViewTypes.CATEGORY.ordinal
        } else {
            ViewTypes.CHILD.ordinal
        }
    }

    override fun getItemId(position: Int): Long {
        return mItems[position].getItemUniqId()
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

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        (holder as? CategoryItemViewHolder)?.refreshView()
                ?: (holder as? ChildItemViewHolder)?.refreshView()
    }


    enum class ViewTypes {
        CATEGORY, CHILD
    }
}