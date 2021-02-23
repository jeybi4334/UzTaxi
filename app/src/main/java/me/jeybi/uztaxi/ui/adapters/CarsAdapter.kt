package me.jeybi.uztaxi.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.util.Assert
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.romainpiel.shimmer.Shimmer
import com.romainpiel.shimmer.ShimmerTextView
import kotlinx.android.synthetic.main.item_car.view.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.ServiceTariff
import me.jeybi.uztaxi.model.TariffOption
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.utils.Constants
import java.text.DecimalFormat


class CarsAdapter(
    val context: Context,
    val items: ArrayList<ServiceTariff>,
    val listener: TariffClickListener
) : RecyclerView.Adapter<CarsAdapter.CarsHolder>() {

    var previousItem : View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarsHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        return CarsHolder(view)
    }

    override fun onBindViewHolder(holder: CarsHolder, position: Int) {
        holder.itemView.tag = "${position}"
        val item = items[position]

        try {
            val jsonObject: JsonObject = JsonParser.parseString(item.name).asJsonObject

            if (jsonObject.isJsonObject){
                holder.textViewTarrifName.text = jsonObject.get((context as MainActivity).getCurrentLanguage().toLanguageTag()).asString
            }
        }catch (exception : IllegalStateException){
            holder.textViewTarrifName.text = item.name
        }






        val decimalFormat = DecimalFormat("###,###")

        val shimmer = Shimmer()

        when(item.icon){
            Constants.CAR_TYPE_SPARK -> {
                holder.imageViewCar.setImageResource(R.drawable.spark)
            }
            Constants.CAR_TYPE_4 -> {
                holder.imageViewCar.setImageResource(R.drawable.spark)
            }
            Constants.CAR_TYPE_DELIVERY -> {
                holder.imageViewCar.setImageResource(R.drawable.scooter)
            }
            Constants.CAR_TYPE_PEREGON -> {
                holder.imageViewCar.setImageResource(R.drawable.spark)
            }
            else->{
                holder.imageViewCar.setImageResource(R.drawable.scooter)
            }
        }
        if ((context as MainActivity).getCurrentLanguage().toLanguageTag()!="uz"&&(context as MainActivity).getCurrentLanguage().toLanguageTag()!="kl")
        holder.textViewPrice.text = "${context.getString(R.string.from)} ${decimalFormat.format(item.minCost)} ${context.getString(
            R.string.currency
        )}"
        else
            holder.textViewPrice.text = "${decimalFormat.format(item.minCost)} ${context.getString(R.string.currency)}${context.getString(
                R.string.from
            )}"

        holder.rvCar.setOnClickListener {

            listener.onTariffChosen(item.id, shimmer, holder.textViewPrice, item.options)
            shimmer.start(holder.textViewPrice)


            holder.rvCar.setBackgroundResource(R.drawable.bc_item_car_selected)
            holder.textViewTarrifName.setTextColor(Color.BLACK)
            holder.textViewPrice.setTextColor(Color.BLACK)

            holder.textViewPrice.text = context.getString(R.string.estimating)

            if (previousItem!=null&& previousItem!!.tag != holder.itemView.tag){
                previousItem!!.setBackgroundResource(R.drawable.bc_item_car)
                previousItem!!.findViewById<TextView>(R.id.textViewTarrifName).setTextColor(
                    Color.parseColor(
                        "#B1B1B1"
                    )
                )
                previousItem!!.findViewById<TextView>(R.id.textViewPrice).setTextColor(
                    Color.parseColor(
                        "#B1B1B1"
                    )
                )
            }else if (previousItem!=null&&previousItem!!.tag == holder.itemView.tag){
                listener.onTarifReclicked(item)
            }

            previousItem = holder.rvCar

        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class CarsHolder(view: View) : RecyclerView.ViewHolder(view){
        val imageViewCar = view.findViewById<ImageView>(R.id.imageViewCar)
        val textViewTarrifName = view.findViewById<TextView>(R.id.textViewTarrifName)
        val textViewPrice = view.findViewById<ShimmerTextView>(R.id.textViewPrice)
        val rvCar = view.findViewById<RelativeLayout>(R.id.rvCar)
    }

    interface TariffClickListener{
        fun onTariffChosen(
            tariffID: Long,
            shimmer: Shimmer,
            textViewPrice: ShimmerTextView,
            options: ArrayList<TariffOption>
        )
        fun onTarifReclicked(tariff: ServiceTariff)
    }
}