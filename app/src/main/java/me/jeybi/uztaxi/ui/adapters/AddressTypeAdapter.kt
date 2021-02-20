package me.jeybi.uztaxi.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.AddressType

class AddressTypeAdapter(val data : ArrayList<AddressType>,val onAddressTypeChosen: OnAddressTypeChosenListener) : RecyclerView.Adapter<AddressTypeAdapter.AddressTypeHolder>() {

    var oldViewHolder : AddressTypeHolder? = null
    var ALIES_CHOSEN = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressTypeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address_type,parent,false)

        return AddressTypeHolder(view)
    }

    override fun onBindViewHolder(holder: AddressTypeHolder, position: Int) {
        val address = data[position]

        holder.imageViewAddressType.setImageResource(address.image)
        holder.textViewAddressType.text = address.name

        holder.linearAddressType.setOnClickListener {
            if (oldViewHolder!=null){
                oldViewHolder?.linearAddressType?.setBackgroundResource(R.drawable.bc_address_white)
                oldViewHolder?.textViewAddressType?.setTextColor(Color.parseColor("#C0C2C9"))
                oldViewHolder?.imageViewAddressType?.setColorFilter(Color.parseColor("#C0C2C9"), android.graphics.PorterDuff.Mode.SRC_IN)
                oldViewHolder?.cardAddressType?.cardElevation = 0.0f
            }
            if (ALIES_CHOSEN==address.alies){
                holder.linearAddressType?.setBackgroundResource(R.drawable.bc_address_white)
                holder.textViewAddressType?.setTextColor(Color.parseColor("#C0C2C9"))
                holder.imageViewAddressType?.setColorFilter(Color.parseColor("#C0C2C9"), android.graphics.PorterDuff.Mode.SRC_IN)
                ALIES_CHOSEN = 0
                onAddressTypeChosen.onAliesChosen(0)
            }else{
                holder.linearAddressType.setBackgroundResource(R.drawable.bc_address_type)
                holder.textViewAddressType.setTextColor(Color.WHITE)
                holder.imageViewAddressType?.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN)
                oldViewHolder?.cardAddressType?.cardElevation = 6f
                onAddressTypeChosen.onAliesChosen(address.alies)
                ALIES_CHOSEN = address.alies
            }

            oldViewHolder = holder
        }


    }

    override fun getItemCount(): Int {
        return data.size
    }



    class AddressTypeHolder(view : View) : RecyclerView.ViewHolder(view){
            val linearAddressType = view.findViewById<LinearLayout>(R.id.linearAddressType)
            val imageViewAddressType = view.findViewById<ImageView>(R.id.imageViewAddressType)
            val textViewAddressType = view.findViewById<TextView>(R.id.textViewAddressType)
            val cardAddressType = view.findViewById<MaterialCardView>(R.id.cardAddressType)

    }

    interface OnAddressTypeChosenListener{
        fun onAliesChosen(alies : Int)
    }
}