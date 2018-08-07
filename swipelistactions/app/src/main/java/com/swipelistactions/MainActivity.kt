package com.swipelistactions

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.swipelistactions.listitems.CategoryListItem
import com.swipelistactions.listitems.ChildListItem
import com.swipelistactions.listitems.ListItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), IListCallback {

    val mDataSet = prepareDataSet()
    var mAdapter: ListViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAdapter = ListViewAdapter(this)

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = mAdapter

        mAdapter?.setData(mDataSet)
    }

    override fun onItemClicked(item: ListItem) {
        if (item.isCategory()) {
            val collapsed = item.isStateCollapsed()

            mDataSet.forEach { if (it.getParentId() == item.getId()) it.setStateCollapsed(!collapsed) }
            val filtered = mDataSet.filter {  it.isStateCollapsed() && it.isCategory() || !it.isStateCollapsed() }
            mAdapter?.setData(filtered)
        }
    }

    override fun removeItem(item: ListItem) {
        if (item.isCategory()) {
            val newItems = mDataSet.filter { it.getParentId() != item.getId() }
            mDataSet.clear()
            mDataSet.addAll(newItems)
        } else {
            mDataSet.remove(item)
        }
        val filtered = mDataSet.filter { it.isStateCollapsed() && it.isCategory() || !it.isStateCollapsed() }
        mAdapter?.setData(filtered)
    }

    private fun prepareDataSet(): ArrayList<ListItem> {

        val list = ArrayList<ListItem>()

        for (i in 0..20) {
            list.add(CategoryListItem(i))
            for (j in 0..20) {
                list.add(ChildListItem(j, i))
            }
        }
        return list
    }

}
