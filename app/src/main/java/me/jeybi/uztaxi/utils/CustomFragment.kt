package me.jeybi.uztaxi.utils

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.database.CreditCardEntity
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.payment.AddCardActivity

class CustomFragment(
    val position: Int,
    val scale: Float,
    val cardType: Int,
    val designType: Int,
    val creditCardEntity: CreditCardEntity?
) : Fragment(),
    AddCardActivity.CardDetailsChangeListener {

    var imageBankLogo: ImageView? = null
    var imageUzcard: ImageView? = null
    var textViewCardOwner: TextView? = null
    var textViewCardExpiryDate: TextView? = null
    var textViewCardNumber: TextView? = null
    var textViewCardBonus: TextView? = null

    lateinit var cardViewParent: CustomConstraintLayout
    lateinit var creditCardView: MaterialCardView

    var rvDeleteCard: RelativeLayout? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (container == null) {
            return null
        }

        var linearLayout: LinearLayout? = null

        when (cardType) {
            Constants.CARD_TYPE_CREATE -> {

                linearLayout =
                    inflater.inflate(R.layout.item_add_card, container, false) as LinearLayout

                creditCardView = linearLayout.findViewById(R.id.item_content)
                creditCardView.setOnClickListener {
                    startActivity(Intent(activity, AddCardActivity::class.java))
                }
            }
            Constants.CARD_TYPE_PLASTIC -> {
                linearLayout =
                    inflater.inflate(R.layout.item_card, container, false) as LinearLayout

                when (designType) {
                    0 -> linearLayout.findViewById<View>(R.id.layoutCard)
                        ?.setBackgroundResource(R.drawable.ic_card_type_1)
                    1 -> linearLayout.findViewById<View>(R.id.layoutCard)
                        ?.setBackgroundResource(R.drawable.ic_card_type_2)
                    2 -> linearLayout.findViewById<View>(R.id.layoutCard)
                        ?.setBackgroundResource(R.drawable.ic_card_type_3)
                    3 -> linearLayout.findViewById<View>(R.id.layoutCard)
                        ?.setBackgroundResource(R.drawable.ic_card_type_4)
                }

                imageBankLogo = linearLayout.findViewById(R.id.imageBankLogo)
                imageUzcard = linearLayout.findViewById(R.id.imageUzcard)
                textViewCardOwner = linearLayout.findViewById<TextView>(R.id.textViewCardOwner)
                textViewCardExpiryDate =
                    linearLayout.findViewById<TextView>(R.id.textViewCardExpiryDate)
                textViewCardNumber = linearLayout.findViewById<TextView>(R.id.textViewCardNumber)

                if (creditCardEntity != null) {
                    textViewCardOwner?.text = creditCardEntity.cardName
                    textViewCardExpiryDate?.text = creditCardEntity.cardExpiry
                    textViewCardNumber?.text = creditCardEntity.cardNumber

                    imageUzcard?.setImageResource(Constants.getBankProviderIcon(creditCardEntity.cardNumber))
                    imageBankLogo?.setImageResource(
                        if (creditCardEntity.cardNumber.startsWith("8600")) Constants.getCardBankIcon(
                            creditCardEntity.cardNumber.toString()
                        ) else Constants.getHumoBankIcon(creditCardEntity.cardNumber.toString())
                    )
                }

            }
            Constants.CARD_TYPE_WALLET -> {
                linearLayout =
                    inflater.inflate(R.layout.item_bonus_card, container, false) as LinearLayout

                val textViewWalletNumber = linearLayout.findViewById<TextView>(R.id.textViewWalletNumber)
                textViewCardBonus = linearLayout.findViewById<TextView>(R.id.textViewCardBonus)
            }

            Constants.CARD_TYPE_STYLE -> {
                linearLayout =
                    inflater.inflate(R.layout.item_card, container, false) as LinearLayout
                imageBankLogo = linearLayout.findViewById(R.id.imageBankLogo)
                imageUzcard = linearLayout.findViewById(R.id.imageUzcard)
                textViewCardOwner = linearLayout.findViewById<TextView>(R.id.textViewCardOwner)
                textViewCardExpiryDate =
                    linearLayout.findViewById<TextView>(R.id.textViewCardExpiryDate)
                textViewCardNumber = linearLayout.findViewById<TextView>(R.id.textViewCardNumber)

                when (position) {
                    0 -> linearLayout.findViewById<View>(R.id.layoutCard)
                        ?.setBackgroundResource(R.drawable.ic_card_type_1)
                    1 -> linearLayout.findViewById<View>(R.id.layoutCard)
                        ?.setBackgroundResource(R.drawable.ic_card_type_2)
                    2 -> linearLayout.findViewById<View>(R.id.layoutCard)
                        ?.setBackgroundResource(R.drawable.ic_card_type_3)
                    3 -> linearLayout.findViewById<View>(R.id.layoutCard)
                        ?.setBackgroundResource(R.drawable.ic_card_type_4)
                }

            }
            else -> linearLayout =
                inflater.inflate(R.layout.item_card, container, false) as LinearLayout
        }



        cardViewParent = linearLayout.findViewById(R.id.item_root)
        creditCardView = linearLayout.findViewById(R.id.item_content)
        rvDeleteCard = linearLayout.findViewById(R.id.rvDeleteCard)





        cardViewParent.setScaleBoth(scale)
        return linearLayout
    }

   override fun onBonusReady(bonus : Double){

       textViewCardBonus?.text = "${Constants.getFormattedPrice(bonus)} UZS"
            Log.d("DSDASDSAD","BONUS ${bonus}")


    }

    override fun editCardClicked(onDeleteClickListener : OnDeleteClick) {
        if (rvDeleteCard != null) {
            if (rvDeleteCard!!.scaleX != 1f){
                cardViewParent.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.wobble))
                rvDeleteCard!!.animate().scaleX(1f).scaleY(1f).setDuration(400).setInterpolator(OvershootInterpolator()).start()
                creditCardView.cardElevation = 0f
                rvDeleteCard!!.setOnClickListener {
                    onDeleteClickListener.onDeletedClicked()
                }
            }else{
                cardViewParent.clearAnimation()
                rvDeleteCard!!.animate().scaleX(0f).scaleY(0f).setDuration(300).setInterpolator(AnticipateInterpolator()).start()
                creditCardView.cardElevation = 6f
            }

        }
    }

    interface OnDeleteClick{
        fun onDeletedClicked()
    }

    override fun onCardNumberChanged(cardNumber: String) {

        val stringBuilder = StringBuilder()
        stringBuilder.append(cardNumber)
        for (i in 0..18) {
            if (i >= cardNumber.length && i != 4 && i != 9 && i != 14) {
                stringBuilder.append("0")
            } else if (i == 4 || i == 9 || i == 14) {
                stringBuilder.append(" ")
            }
        }
        imageUzcard?.setImageResource(Constants.getBankProviderIcon(stringBuilder.toString()))
        imageBankLogo?.setImageResource(
            if (stringBuilder.startsWith("8600")) Constants.getCardBankIcon(
                stringBuilder.toString()
            ) else Constants.getHumoBankIcon(stringBuilder.toString())
        )
        textViewCardNumber?.text = stringBuilder
    }

    override fun onCardExpiryChanged(expiryDate: String) {
        textViewCardExpiryDate?.text = expiryDate
    }

    override fun onCardNameChanged(cardName: String) {
        textViewCardOwner?.text = cardName
    }


}