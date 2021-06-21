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
            Constants.CAR_TYPE_1 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_white_nexia3)
            }
            Constants.CAR_TYPE_2 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_white_spark)
            }
            Constants.CAR_TYPE_3 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_white_gentra)
            }
            Constants.CAR_TYPE_4 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_white_epica)
            }
            Constants.CAR_TYPE_5 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_white_isuzu)
            }
            Constants.CAR_TYPE_6 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_black_vito)
            }
            Constants.CAR_TYPE_7 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_white_captiva)
            }
            Constants.CAR_TYPE_8 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_white_malibu1)
            }
            Constants.CAR_TYPE_9 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_white_malibu2)
            }
            Constants.CAR_TYPE_10 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_scooter)
            }
            Constants.CAR_TYPE_11 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_white_cruiser)
            }
            Constants.CAR_TYPE_12 -> {
                holder.imageViewCar.setImageResource(R.drawable.car_keys)
            }
            else->{
                holder.imageViewCar.setImageResource(R.drawable.car_default)
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

        holder.rvCar.setOnLongClickListener {
            listener.onTarifReclicked(item)
            true
        }

        holder.rvCar.setOnClickListener {

            listener.onTariffChosen(item.id, shimmer, holder.textViewPrice, item.options,item.costChangeStep)
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
            options: ArrayList<TariffOption>,
            costChangeStep : Double
        )
        fun onTarifReclicked(tariff: ServiceTariff)
    }
}