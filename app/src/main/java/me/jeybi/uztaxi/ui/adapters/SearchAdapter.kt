package me.jeybi.uztaxi.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.SearchItemModel
import me.jeybi.uztaxi.model.SearchedAddress
import me.jeybi.uztaxi.utils.Constants

class SearchAdapter(val items: ArrayList<SearchedAddress>, val listener: SearchItemClickListener) :
    RecyclerView.Adapter<SearchAdapter.SearchHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return SearchHolder(view)
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
        val item = items[position]


        if (item.types?.aliasType != null) {
            when (item.types.aliasType) {
                Constants.ALIES_TYPE_AIRPORT -> {
                    holder.iconImage.setImageResource(R.drawable.s_airport)
                }Constants.ALIES_TYPE_BANK -> {
                    holder.iconImage.setImageResource(R.drawable.s_bank)
                }Constants.ALIES_TYPE_METRO -> {
                    holder.iconImage.setImageResource(R.drawable.s_metro)
                }Constants.ALIES_TYPE_KAFE_RESTAURANT -> {
                    holder.iconImage.setImageResource(R.drawable.s_eat)
                }Constants.ALIES_TYPE_STADIUM -> {
                    holder.iconImage.setImageResource(R.drawable.s_stadium)
                }Constants.ALIES_TYPE_PARK -> {
                    holder.iconImage.setImageResource(R.drawable.s_tree)
                }Constants.ALIES_TYPE_OTHER -> {
//                    holder.iconImage.setImageResource(R.drawable.ic_airport)
                }Constants.ALIES_TYPE_SCHOOL -> {
                    holder.iconImage.setImageResource(R.drawable.s_school)
                }Constants.ALIES_TYPE_HOTEL -> {
                    holder.iconImage.setImageResource(R.drawable.s_hotel)
                }Constants.ALIES_TYPE_TRAIN -> {
                    holder.iconImage.setImageResource(R.drawable.s_train)
                }Constants.ALIES_TYPE_AVTO -> {
                    holder.iconImage.setImageResource(R.drawable.s_auto)
                }Constants.ALIES_TYPE_BANYA -> {
//                    holder.iconImage.setImageResource(R.drawable.ic_airport)
                }Constants.ALIES_TYPE_GOVERMENT -> {
//                    holder.iconImage.setImageResource(R.drawable.ic_airport)
                }Constants.ALIES_TYPE_ZOOPARK -> {
                    holder.iconImage.setImageResource(R.drawable.s_zoo)
                }Constants.ALIES_TYPE_CEMETERY -> {
                    holder.iconImage.setImageResource(R.drawable.s_gravestone)
                }Constants.ALIES_TYPE_ART -> {
//                    holder.iconImage.setImageResource(R.drawable.ic_airport)
                }Constants.ALIES_TYPE_HOSPITAL -> {
                    holder.iconImage.setImageResource(R.drawable.s_hospital)
                }Constants.ALIES_TYPE_BEAUTY -> {
//                    holder.iconImage.setImageResource(R.drawable.ic_airport)
                }Constants.ALIES_TYPE_SHOP -> {
                    holder.iconImage.setImageResource(R.drawable.s_shopping)
                }Constants.ALIES_TYPE_BUSSTOP -> {
                    holder.iconImage.setImageResource(R.drawable.s_station)
                }Constants.ALIES_TYPE_TOURIST -> {
//                    holder.iconImage.setImageResource(R.drawable.ic_airport)
                }Constants.ALIES_TYPE_GARDEN -> {
//                    holder.iconImage.setImageResource(R.drawable.ic_airport)
                }Constants.ALIES_TYPE_CROSSROAD -> {
                    holder.iconImage.setImageResource(R.drawable.s_signpost)
                }
            }
        }



        if (item.components != null && item.components.size > 0) {
            var address = ""
            for (component in item.components) {
                when (component.level) {
                    in 4..8 -> {
                        address += "${component.name},"
                    }
                    9 -> {
                        holder.textViewTitle.text = component.name
                    }
                }
            }
            holder.textViewAddress.text = address
        }

        holder.itemView.setOnClickListener {
            if (item.position != null)
                listener.onSearchClicked(item.position.lat, item.position.lon,holder.textViewTitle.text.toString())
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class SearchHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconImage = view.findViewById<ImageView>(R.id.iconSearchItem)
        val textViewTitle = view.findViewById<TextView>(R.id.textViewSearchTitle)
        val textViewAddress = view.findViewById<TextView>(R.id.textViewSearchAddress)

    }

    interface SearchItemClickListener {
        fun onSearchClicked(latitude: Double, longitude: Double,title : String)
    }
}