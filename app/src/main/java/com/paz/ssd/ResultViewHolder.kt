package com.paz.ssd

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val name: TextView = itemView.findViewById(R.id.res_LBL_name)
    private val per: TextView = itemView.findViewById(R.id.res_LBL_per)

    open fun bind(data: MyData ) {
        name.text = data.name
        per.text = String.format("%.5f%s",data.per,"%")

    }
}
