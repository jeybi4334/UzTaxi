package me.jeybi.uztaxi.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.database.AddressEntity
import me.jeybi.uztaxi.utils.Constants

class AddressAdapter(val data : List<AddressEntity>) : RecyclerView.Adapter<AddressAdapter.AddressHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address,parent,false)

        return AddressHolder(view)
    }

    override fun onBindViewHolder(holder: AddressHolder, position: Int) {
            val address = data[position]
        holder.textViewAddressTitle.text = address.title
            if (holder.parentAddress.layoutParams is FlexboxLayoutManager.LayoutParams){
                (holder.parentAddress.layoutParams as FlexboxLayoutManager.LayoutParams).flexGrow = 1f
            }



        when(address.alies){
            Constants.ALIES_TYPE_HOME->{
                holder.imageAddressAlias.setImageResource(R.drawable.ic_address_home)
            }
            Constants.ALIES_TYPE_SCHOOL->{
                holder.imageAddressAlias.setImageResource(R.drawable.ic_address_school)
            }
            Constants.ALIES_TYPE_PARK->{
                holder.imageAddressAlias.setImageResource(R.drawable.ic_address_park)
            }
            Constants.ALIES_TYPE_WORK->{
                holder.imageAddressAlias.setImageResource(R.drawable.ic_address_work)
            }
            Constants.ALIES_TYPE_SHOP->{
                holder.imageAddressAlias.setImageResource(R.drawable.ic_address_shop)
            }
            Constants.ALIES_TYPE_GYM->{
                holder.imageAddressAlias.setImageResource(R.drawable.ic_address_gym)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class AddressHolder(view : View) : RecyclerView.ViewHolder(view){
        val textViewAddressTitle = view.findViewById<TextView>(R.id.textViewAddressTitle)
        val textViewAddressTime = view.findViewById<TextView>(R.id.textViewAddressTime)
        val imageAddressAlias = view.findViewById<ImageView>(R.id.imageAddressAlias)
        val parentAddress = view.findViewById<ConstraintLayout>(R.id.parentAddress)

    }
}
