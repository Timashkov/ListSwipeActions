package com.swipelistactions

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.swipelistactions.listitems.CategoryListItem
import com.swipelistactions.listitems.ChildListItem
import com.swipelistactions.listitems.ListItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = ListViewAdapter()

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter

        ItemTouchHelper(RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)).attachToRecyclerView(listView)

        adapter.setData(prepareDataSet())
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {

    }

    private fun prepareDataSet(): List<ListItem> {

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
