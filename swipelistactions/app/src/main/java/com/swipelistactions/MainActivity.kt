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
        if (item.isCategory()){
            val collapsed = item.isStateCollapsed()
            item.setStateCollapsed(!collapsed)
        }
    }

    override fun removeItem(item: ListItem) {
        if (item.isCategory()) {
            val newItems = mDataSet.filter { it.getParentId() != item.getId() }
            mAdapter?.setData(newItems)
        }else{
            mDataSet.remove(item)
            mAdapter?.setData(mDataSet)
        }
    }

    private fun prepareDataSet(): ArrayList<ListItem> {

        val list = ArrayList<ListItem>()

        for (i in 0..100) {
            list.add(CategoryListItem(i))
            for (j in 0..100) {
                list.add(ChildListItem(j, i))
            }
        }
        return list
    }

}
