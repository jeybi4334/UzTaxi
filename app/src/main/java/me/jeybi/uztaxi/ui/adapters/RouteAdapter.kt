package me.jeybi.uztaxi.ui.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.okhttp.Route
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.RouteItem
import me.jeybi.uztaxi.ui.main.MainActivity

class RouteAdapter(val data : ArrayList<RouteItem>,val listener : onRouteAddClickListener,val activity : MainActivity,val delivery : Boolean) : RecyclerView.Adapter<RouteAdapter.RouteHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_route,parent,false)


        val viewHolder = RouteHolder(view)

//        viewHolder.itemView.findViewById<ImageView>(R.id.iconEnd).setOnTouchListener { view, event ->
//            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
//                activity.startDragging(viewHolder)
//            }
//            return@setOnTouchListener true
//        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: RouteHolder, position: Int) {

        if (delivery) {
            holder.imageViewAdd.visibility = View.GONE
        }else{
            holder.imageViewAdd.visibility = View.VISIBLE
        }


        if (position==0){
            holder.imageViewAdd.setImageResource(R.drawable.ic_add_point)
            if (data.size==1)
                holder.iconEnd.setImageResource(R.drawable.ic_circle_end)
            else
                holder.iconEnd.setImageResource(R.drawable.ic_draghandler)
        }else{
            holder.imageViewAdd.setImageResource(R.drawable.ic_cancel_route)

            holder.iconEnd.setImageResource(R.drawable.ic_draghandler)
        }

        val item = data[position]
        holder.textViewDestination.text = item.address
        holder.imageViewAdd.setOnClickListener{
            if (position==0){
                listener.onRouteAddClicked()
            }else{
               data.remove(item)

                listener.onRemoveRouteClicked(item.lat,item.lon,position,item)
            }

        }

        holder.textMap.setOnClickListener{
            listener.onChangeRouteLocationClicked(item.lat,item.lon)
        }

        holder.iconEnd.setOnTouchListener { view, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                activity.startDragging(holder)
            }
            return@setOnTouchListener true
        }
    }

    fun moveItem(fromView : RecyclerView.ViewHolder,toView : RecyclerView.ViewHolder ,from: Int, to: Int) {

        if (to==0){
            (toView as RouteHolder).iconEnd.setImageResource(R.drawable.ic_draghandler)
            (fromView as RouteHolder).iconEnd.setImageResource(R.drawable.ic_draghandler)
            (toView as RouteHolder).imageViewAdd.setImageResource(R.drawable.ic_cancel_route)
            (fromView as RouteHolder).imageViewAdd.setImageResource(R.drawable.ic_add_point)

        }
        if (from==0){
            (toView as RouteHolder).iconEnd.setImageResource(R.drawable.ic_draghandler)
            (fromView as RouteHolder).iconEnd.setImageResource(R.drawable.ic_draghandler)
            (toView as RouteHolder).imageViewAdd.setImageResource(R.drawable.ic_add_point)
            (fromView as RouteHolder).imageViewAdd.setImageResource(R.drawable.ic_cancel_route)
        }

        val fromEmoji = data[from]
        data.removeAt(from)
        if (to < from) {
            data.add(to, fromEmoji)
        } else {
            data.add(to - 1, fromEmoji)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }


    class RouteHolder(view : View) : RecyclerView.ViewHolder(view){
        val textViewDestination = view.findViewById<TextView>(R.id.textViewDestination)
        val textMap = view.findViewById<TextView>(R.id.textMap)
        val imageViewAdd = view.findViewById<ImageView>(R.id.imageViewAdd)
//        val imageViewSelectFromMap = view.findViewById<ImageView>(R.id.imageViewSelectFromMap)
        val iconEnd = view.findViewById<ImageView>(R.id.iconEnd)
    }

    interface onRouteAddClickListener{
        fun onRouteAddClicked()
        fun onChangeRouteLocationClicked(lat : Double,lon : Double)
        fun onRemoveRouteClicked(lat:Double,lon :Double,position: Int,removedItem : RouteItem)

    }

}