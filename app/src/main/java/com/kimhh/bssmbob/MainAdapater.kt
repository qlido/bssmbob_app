package com.kimhh.bssmbob

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.kimhh.bssmbob.LunchData
import kotlinx.android.synthetic.main.item_pager.view.*

class MainAdapter(val items: ArrayList<LunchData>) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = MainViewHolder(parent)


    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holer: MainViewHolder, position: Int) {
        items[position].let { item ->
            with(holer) {
                kcal.text = item.kcal
                meal_list.text = item.meal
                background.setCardBackgroundColor(Color.parseColor(item.background))
            }
        }
    }

    inner class MainViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_pager, parent, false)) {
        var kcal = itemView.kcal
        var meal_list = itemView.meal_list
        var background = itemView.backgroundColor as CardView
    }
}