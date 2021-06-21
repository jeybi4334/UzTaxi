package me.jeybi.uztaxi.ui.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.database.AddressEntity
import me.jeybi.uztaxi.ui.address.AddressesActivity
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.setSafeOnClickListener

class AddressAdapter(var data : ArrayList<AddressEntity>, val activity : Activity?,val onItemMoveListener: OnItemMoveListener?) : RecyclerView.Adapter<AddressAdapter.AddressHolder>() {



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

        holder.rvDeleteAddress.setOnClickListener {
            onItemMoveListener?.onItemRemoved(address.id,position)
            data.removeAt(position)
        }
        if (activity!=null&&activity is MainActivity){
            val distance = Constants.distanceTo((activity as MainActivity).START_POINT_LAT,(activity as MainActivity).START_POINT_LON,address.latitude,address.longitude)


            val time = ((distance.toInt() * 3600) / 10000) / 70

            holder.textViewAddressTime.text = "$time ${activity.getString(R.string.minute)}"
        }

        if (activity!=null&&activity is MainActivity)
        holder.constraintAddress.setSafeOnClickListener {
            activity.onBottomSheetSearchItemClicked(address.latitude,address.longitude,address.title,false)
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
            Constants.ALIES_TYPE_MOSQUE->{
                holder.imageAddressAlias.setImageResource(R.drawable.ic_mosque)
            }
            else->{
                holder.imageAddressAlias.setImageResource(R.drawable.ic_address_point)
            }
        }
        if (activity!=null&&activity is AddressesActivity) {
            holder.itemView.findViewById<ConstraintLayout>(R.id.constraintAddress).setOnTouchListener { view, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    activity.startDragging(holder)
                }
                return@setOnTouchListener true
            }

        }
    }


    fun moveItem(fromView : RecyclerView.ViewHolder,toView : RecyclerView.ViewHolder ,from: Int, to: Int) {
        onItemMoveListener?.onItemMoved(data[from],data[to])
        val fromEmoji = data[from]
        data.removeAt(from)
        if (to < from) {
            data.add(to, fromEmoji)
        } else {
            data.add(to - 1, fromEmoji)
        }

    }

    interface OnItemMoveListener{
        fun onItemMoved(movedItem : AddressEntity , newItem : AddressEntity)
        fun onItemRemoved(id : Int,adapterPosition : Int)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class AddressHolder(view : View) : RecyclerView.ViewHolder(view){
        val textViewAddressTitle = view.findViewById<TextView>(R.id.textViewAddressTitle)
        val textViewAddressTime = view.findViewById<TextView>(R.id.textViewAddressTime)
        val imageAddressAlias = view.findViewById<ImageView>(R.id.imageAddressAlias)
        val parentAddress = view.findViewById<FrameLayout>(R.id.parentAddress)
        val rvDeleteAddress= view.findViewById<RelativeLayout>(R.id.rvDeleteAddress)
        val constraintAddress= view.findViewById<ConstraintLayout>(R.id.constraintAddress)
    }
}
