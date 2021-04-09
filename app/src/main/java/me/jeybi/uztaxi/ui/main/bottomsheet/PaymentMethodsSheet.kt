package me.jeybi.uztaxi.ui.main.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_credit_cards.*
import kotlinx.android.synthetic.main.bottomsheet_payment.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.UzTaxiApplication
import me.jeybi.uztaxi.database.CreditCardEntity
import me.jeybi.uztaxi.model.PaymentMethod
import me.jeybi.uztaxi.ui.main.MainActivity
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

            when (paymentMethod.kind) {
                "cash" -> {
                    itemView.findViewById<TextView>(R.id.textViewPaymentType).text = getString(R.string.pay_cash)
                }
                "contractor" -> {
                    try {
                        val jsonObject: JsonObject = JsonParser.parseString(paymentMethod.name).asJsonObject

                        if (jsonObject.isJsonObject){
                            itemView.findViewById<TextView>(R.id.textViewPaymentType).text = jsonObject.get("name").asString
                            val imageLogo = itemView.findViewById<ImageView>(R.id.imageViewPaymentMethod)
                                imageLogo.visibility = View.VISIBLE
                            Glide.with(context!!).load(jsonObject.get("logo").asString)
                                .priority(Priority.IMMEDIATE)
                                .placeholder(R.drawable.ic_suitcase)
                                .into(imageLogo)

                        }
                    }catch (exception : IllegalStateException){
                        itemView.findViewById<ImageView>(R.id.imageViewPaymentMethod).setImageResource(R.drawable.ic_suitcase)
                        itemView.findViewById<TextView>(R.id.textViewPaymentType).text = paymentMethod.name
                    }


                }
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
                               val itemView = LayoutInflater.from(activity).inflate(R.layout.item_payment_card,linearParent,false)

                               itemView.findViewById<TextView>(R.id.textViewCardName).text = card.cardName
                               itemView.findViewById<TextView>(R.id.textViewCarNum).text = Constants.maskCardNumber(card.cardNumber,"#### •••• •••• ####")
                               itemView.findViewById<ImageView>(R.id.imageViewDone).visibility = View.GONE




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