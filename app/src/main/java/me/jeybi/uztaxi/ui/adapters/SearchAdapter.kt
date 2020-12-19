package me.jeybi.uztaxi.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.SearchItemModel

class SearchAdapter(val items : ArrayList<SearchItemModel>,val listener : SearchItemClickListener) : RecyclerView.Adapter<SearchAdapter.SearchHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search,parent,false)
        return SearchHolder(view)
    }

    override fun onBindViewHolder(holder: SearchHolder, position: Int) {
            val item = items[position]
        holder.iconImage.setImageResource(item.image)
        holder.textViewTitle.text = item.title
        holder.textViewAddress.text = item.address
        holder.itemView.setOnClickListener {
            listener.onSearchClicked()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class SearchHolder(view : View) : RecyclerView.ViewHolder(view){
        val iconImage = view.findViewById<ImageView>(R.id.iconSearchItem)
        val textViewTitle = view.findViewById<TextView>(R.id.textViewSearchTitle)
        val textViewAddress = view.findViewById<TextView>(R.id.textViewSearchAddress)

    }

    interface SearchItemClickListener{
        fun onSearchClicked()
    }
}