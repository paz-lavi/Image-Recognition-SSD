package com.paz.ssd

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class ResultsAdapter(private  val results: ArrayList<MyData>) : RecyclerView.Adapter<ResultViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_res, parent, false)
        return ResultViewHolder(view)    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        results[position].let { holder.bind(it ) }
    }

    override fun getItemCount(): Int {
        return results.size
    }


}
