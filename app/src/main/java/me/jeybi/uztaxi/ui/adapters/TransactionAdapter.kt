package me.jeybi.uztaxi.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.Transaction
import me.jeybi.uztaxi.utils.Constants

class TransactionAdapter(val transactions : ArrayList<Transaction>) : RecyclerView.Adapter<TransactionAdapter.TransactionHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction,parent,false)
        return TransactionHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val transaction = transactions[position]
        if (transaction.type==0){
            holder.rvTransactionType.setBackgroundResource(R.drawable.bc_transaction_min)
            holder.imageTransactionUp.setImageResource(R.drawable.ic_tr_up)
            holder.textTransactionAmount.text = "-${Constants.getFormattedPrice(transaction.amount)} сум"
            holder.textTransactionAmount.setTextColor(Color.parseColor("#FF3A79"))
        }else{
            holder.rvTransactionType.setBackgroundResource(R.drawable.bc_transaction_plus)
            holder.imageTransactionUp.setImageResource(R.drawable.ic_tr_down)
            holder.textTransactionAmount.text = "+${Constants.getFormattedPrice(transaction.amount)} сум"
            holder.textTransactionAmount.setTextColor(Color.parseColor("#1AD5AD"))
        }
        holder.textTransactionName.text = transaction.name

        holder.textTransactionSource.text = transaction.source
        holder.textTransactionDate.text = transaction.date
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class TransactionHolder(view : View) : RecyclerView.ViewHolder(view) {
        val rvTransactionType = view.findViewById<RelativeLayout>(R.id.rvTransactionType)
        val imageTransactionUp = view.findViewById<ImageView>(R.id.imageTransactionUp)
        val textTransactionName = view.findViewById<TextView>(R.id.textTransactionName)
        val textTransactionAmount = view.findViewById<TextView>(R.id.textTransactionAmount)
        val textTransactionSource = view.findViewById<TextView>(R.id.textTransactionSource)
        val textTransactionDate = view.findViewById<TextView>(R.id.textTransactionDate)
    }
}