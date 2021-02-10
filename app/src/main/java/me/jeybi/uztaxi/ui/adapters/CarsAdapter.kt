package me.jeybi.uztaxi.ui.adapters

import android.content.Context
import android.graphics.Color
import android.media.Image
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.romainpiel.shimmer.Shimmer
import com.romainpiel.shimmer.ShimmerTextView
import kotlinx.android.synthetic.main.item_car.view.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.SearchItemModel
import me.jeybi.uztaxi.model.ServiceTariff
import me.jeybi.uztaxi.model.TariffOption
import me.jeybi.uztaxi.utils.Constants
import java.text.DecimalFormat

class CarsAdapter(val context : Context,val items : ArrayList<ServiceTariff>, val listener : TariffClickListener) : RecyclerView.Adapter<CarsAdapter.CarsHolder>() {

    var previousItem : View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car,parent,false)
        return CarsHolder(view)
    }

    override fun onBindViewHolder(holder: CarsHolder, position: Int) {
        holder.rvCar.tag = "${position}"
        val item = items[position]

        holder.textViewTarrifName.text = item.name
        val decimalFormat = DecimalFormat("###,###")

        val shimmer = Shimmer()

        when(item.icon){
            Constants.CAR_TYPE_SPARK->{
                holder.imageViewCar.setImageResource(R.drawable.spark)
            }
            Constants.CAR_TYPE_4->{
                holder.imageViewCar.setImageResource(R.drawable.spark)
            }
            Constants.CAR_TYPE_DELIVERY->{
                holder.imageViewCar.setImageResource(R.drawable.scooter)
            }
            Constants.CAR_TYPE_PEREGON->{
                holder.imageViewCar.setImageResource(R.drawable.malibu)
            }
            else->{
                holder.imageViewCar.setImageResource(R.drawable.scooter)
            }
        }

        holder.textViewPrice.text = "${context.getString(R.string.from)} ${decimalFormat.format(item.minCost)} ${context.getString(R.string.currency)}"

        holder.rvCar.setOnClickListener {

            listener.onTariffChosen(item.id,shimmer,holder.textViewPrice,item.options)
            shimmer.start(holder.textViewPrice)


            holder.rvCar.setBackgroundResource(R.drawable.bc_item_car_selected)
            holder.textViewTarrifName.setTextColor(Color.BLACK)
            holder.textViewPrice.setTextColor(Color.BLACK)

            holder.textViewPrice.text = context.getString(R.string.estimating)

            if (previousItem!=null&& previousItem!!.tag != holder.itemView.tag){
                previousItem!!.setBackgroundResource(R.drawable.bc_item_car)
                previousItem!!.findViewById<TextView>(R.id.textViewTarrifName).setTextColor(Color.parseColor("#B1B1B1"))
                previousItem!!.findViewById<TextView>(R.id.textViewPrice).setTextColor(Color.parseColor("#B1B1B1"))
            }

            previousItem = holder.rvCar

        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class CarsHolder(view : View) : RecyclerView.ViewHolder(view){
        val imageViewCar = view.findViewById<ImageView>(R.id.imageViewCar)
        val textViewTarrifName = view.findViewById<TextView>(R.id.textViewTarrifName)
        val textViewPrice = view.findViewById<ShimmerTextView>(R.id.textViewPrice)
        val rvCar = view.findViewById<RelativeLayout>(R.id.rvCar)
    }

    interface TariffClickListener{
        fun onTariffChosen(tariffID : Long ,shimmer: Shimmer,textViewPrice : ShimmerTextView, options : ArrayList<TariffOption>)
    }
}