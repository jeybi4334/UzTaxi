package me.jeybi.uztaxi.ui.main.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_credit_cards.*
import kotlinx.android.synthetic.main.bottomsheet_payment.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.UzTaxiApplication
import me.jeybi.uztaxi.database.CreditCardEntity
import me.jeybi.uztaxi.model.PaymentMethod
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.CustomPagerAdapter

class PaymentMethodsSheet(val paymentMethods : ArrayList<PaymentMethod>) : BottomSheetDialogFragment() {

    val cardsDisposable = CompositeDisposable()

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
                itemView.findViewById<TextView>(R.id.textViewPaymentType).text = getString(R.string.cash)
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

        loadCardsFromDatabase()
    }

    private fun loadCardsFromDatabase() {

        cardsDisposable.add(
            (activity!!.application as UzTaxiApplication).uzTaxiDatabase.getCardDAO()
                .getCreditCards().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                           for (card in it){
                               val itemView = LayoutInflater.from(activity).inflate(R.layout.item_payment_method,linearParent,false)
                               itemView.findViewById<TextView>(R.id.textViewPaymentType).text = Constants.maskCardNumber(card.cardNumber,"#### •••• •••• ####")
                               itemView.findViewById<ImageView>(R.id.imageViewDone).visibility = View.GONE
                               itemView.findViewById<ImageView>(R.id.imageViewPaymentMethod).visibility = View.GONE
                               itemView.findViewById<MaterialCardView>(R.id.cardPaymentMethod).visibility = View.VISIBLE




                               when (card.cardDesign) {
                                   0 -> itemView.findViewById<View>(R.id.rvCardPayment)
                                       ?.setBackgroundResource(R.drawable.ic_card_type_1)
                                   1 -> itemView.findViewById<View>(R.id.rvCardPayment)
                                       ?.setBackgroundResource(R.drawable.ic_card_type_2)
                                   2 -> itemView.findViewById<View>(R.id.rvCardPayment)
                                       ?.setBackgroundResource(R.drawable.ic_card_type_3)
                                   3 -> itemView.findViewById<View>(R.id.rvCardPayment)
                                       ?.setBackgroundResource(R.drawable.ic_card_type_4)
                               }
                               linearParent.addView(itemView)
                           }
                },{

                })
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cardsDisposable.dispose()
    }

}