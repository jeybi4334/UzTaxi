package me.jeybi.uztaxi.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_car.view.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.SearchItemModel

class CarsAdapter(val items : ArrayList<SearchItemModel>, val listener : SearchItemClickListener) : RecyclerView.Adapter<CarsAdapter.CarsHolder>() {

    var previousItem : View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car,parent,false)
        return CarsHolder(view)
    }

    override fun onBindViewHolder(holder: CarsHolder, position: Int) {
        val item = items[position]
        holder.iconImage.setImageResource(item.image)
        holder.textViewTarrifName.text = item.title
        holder.textViewPrice.text = item.address
        holder.itemView.setOnClickListener {
            listener.onSearchClicked()

            holder.itemView.setBackgroundResource(R.drawable.bc_item_car_selected)
            holder.itemView.textViewTarrifName.setTextColor(Color.WHITE)
            holder.itemView.textViewPrice.setTextColor(Color.WHITE)

            if (previousItem!=null){
                previousItem!!.setBackgroundResource(R.drawable.bc_item_car)
                previousItem!!.findViewById<TextView>(R.id.textViewTarrifName).setTextColor(Color.parseColor("#B1B1B1"))
                previousItem!!.findViewById<TextView>(R.id.textViewPrice).setTextColor(Color.parseColor("#B1B1B1"))
            }

            previousItem = holder.itemView
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class CarsHolder(view : View) : RecyclerView.ViewHolder(view){
        val iconImage = view.findViewById<ImageView>(R.id.imageViewCar)
        val textViewTarrifName = view.findViewById<TextView>(R.id.textViewTarrifName)
        val textViewPrice = view.findViewById<TextView>(R.id.textViewPrice)

    }

    interface SearchItemClickListener{
        fun onSearchClicked()
    }
}