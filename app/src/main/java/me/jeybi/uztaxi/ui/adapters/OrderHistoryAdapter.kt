package me.jeybi.uztaxi.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.ShortOrderInfo

class OrderHistoryAdapter(val context : Context,val data: ArrayList<ShortOrderInfo>) : RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_history, parent, false)
        return OrderHistoryHolder(view)
    }

    override fun onBindViewHolder(holder: OrderHistoryHolder, position: Int) {
        val shortOrderInfo = data[position]

        if (shortOrderInfo.time!=null) {
            holder.textViewDate.text = shortOrderInfo.time.split("T")[0]
            val time = shortOrderInfo.time.split("T")[1]
            holder.textViewOrderTime.text =  "${time.split(":")[0]} : ${time.split(":")[1]}"
        }
        if ( shortOrderInfo.route[0].name!="")
        holder.textViewStartAddress.text = shortOrderInfo.route[0].name
        if (shortOrderInfo.route.size>1){
            if ( shortOrderInfo.route[1].name!="")
            holder.textViewEndAddress.text = shortOrderInfo.route[1].name
        }
        if (shortOrderInfo.state==5){
            holder.imageViewOrderStatus.setImageResource(R.drawable.ic_checked)
            holder.textViewOderCost.text = context.getString(R.string.finished)
            holder.textViewOderCost.setTextColor(Color.parseColor("#3bb54a"))
        }else{
            holder.imageViewOrderStatus.setImageResource(R.drawable.ic_x_button)
            holder.textViewOderCost.text = context.getString(R.string.cancelled)
            holder.textViewOderCost.setTextColor(Color.parseColor("#e21b1b"))
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }


    class OrderHistoryHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textViewDate = view.findViewById<TextView>(R.id.textViewDate)
            val textViewOrderTime = view.findViewById<TextView>(R.id.textViewOrderTime)
            val textViewOderCost = view.findViewById<TextView>(R.id.textViewOderCost)
            val imageViewOrderStatus = view.findViewById<ImageView>(R.id.imageViewOrderStatus)
            val textViewStartAddress = view.findViewById<TextView>(R.id.textViewStartAddress)
            val textViewEndAddress = view.findViewById<TextView>(R.id.textViewEndAddress)

    }
}