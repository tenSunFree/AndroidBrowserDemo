package com.home.androidbrowserdemo.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.home.androidbrowserdemo.R
import com.home.androidbrowserdemo.controller.AlbumController
import com.home.androidbrowserdemo.view.activity.MainActivity
import com.home.androidbrowserdemo.view.layoutmanager.EchelonLayoutManager
import kotlinx.android.synthetic.main.fragment_main_all_pagination.*

class MainAllPaginationFragment(val activity: MainActivity, val list: List<AlbumController>) :
    Fragment() {

    private var mRecyclerView: RecyclerView? = null
    private var mLayoutManager: EchelonLayoutManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_all_pagination, container, false)
        mRecyclerView = rootView.findViewById(R.id.recycler_view)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
    }

    private fun initData() {
        mLayoutManager = EchelonLayoutManager()
        mRecyclerView!!.layoutManager = mLayoutManager
        val adapter = MyAdapter()
        mRecyclerView!!.adapter = adapter
        image_view_add.setOnClickListener {
            activity.showEchelonFragment(false)
            activity.addAlbum()
        }
        image_view_more.setOnClickListener {
            Toast.makeText(activity, "click image_view_more", Toast.LENGTH_SHORT).show()
        }
        text_view_quantity.text = list.size.toString()
        text_view_quantity.setOnClickListener {
            Toast.makeText(activity, "click text_view_quantity", Toast.LENGTH_SHORT).show()
        }
    }

    internal inner class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_echelon, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val view = list[position].albumView
            val vp = view.parent
            if (vp is ViewGroup) {
                vp.removeView(view)
            }
            holder.frameLayout.addView(view)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            internal var frameLayout: FrameLayout = itemView.findViewById(R.id.frame_layout)
        }
    }
}
