package me.jeybi.uztaxi.ui.main.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_payment.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.PaymentMethod

class PaymentMethodsSheet(val paymentMethods : ArrayList<PaymentMethod>) : BottomSheetDialogFragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_payment,container,false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for (paymentMethod in paymentMethods){
            val itemView = LayoutInflater.from(view.context).inflate(R.layout.item_payment_method,linearParent,false)
            if (paymentMethod.kind=="cash") {
                itemView.findViewById<TextView>(R.id.textViewPaymentType).text = "Наличные"
                itemView.findViewById<ImageView>(R.id.imageViewPaymentMethod).setImageResource(R.drawable.ic_cash)
            }else{
                itemView.findViewById<TextView>(R.id.textViewPaymentType).text = paymentMethod.kind
                itemView.findViewById<ImageView>(R.id.imageViewPaymentMethod).setImageResource(R.drawable.uzcard)
            }

            itemView.findViewById<LinearLayout>(R.id.parentPaymentMethod).setOnClickListener {
                dismiss()
            }
            linearParent.addView(itemView)
        }


    }

}