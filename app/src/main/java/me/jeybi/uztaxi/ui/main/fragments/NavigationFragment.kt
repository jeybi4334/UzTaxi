package me.jeybi.uztaxi.ui.main.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.navigation_menu.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.address.AddressesActivity
import me.jeybi.uztaxi.ui.auth.AuthenticationActivity
import me.jeybi.uztaxi.ui.history.OrderHistoryActivity
import me.jeybi.uztaxi.ui.info.InfoActivity
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.ui.payment.AddCardActivity
import me.jeybi.uztaxi.ui.payment.CreditCardsActivity
import me.jeybi.uztaxi.ui.payment.PaymentTypeActivity
import me.jeybi.uztaxi.ui.settings.SettingsActivity
import me.jeybi.uztaxi.utils.Constants

class NavigationFragment : BaseFragment() {

    override fun setLayoutId(): Int {
        return R.layout.navigation_menu
    }
    var BONUS = 0.0

    override fun onResume() {
        super.onResume()
        checkPaymentType()
    }

    fun checkPaymentType(){
        val payment_type = sharedPreferences.getString(Constants.PAYMENT_TYPE,Constants.PAYMENT_TYPE_CASH)
        val payment_type_name = sharedPreferences.getString(Constants.PAYMENT_TYPE_NAME,"")

        when (payment_type) {
            "" -> {
                textNavigationPayment.text = getString(R.string.cash)
            }
            Constants.PAYMENT_TYPE_CASH -> {
                textNavigationPayment.text = getString(R.string.cash)
            }
            Constants.PAYMENT_TYPE_CONTRACTOR -> {
                try {
                    val jsonObject: JsonObject = JsonParser.parseString(payment_type_name).asJsonObject

                    if (jsonObject.isJsonObject){
                        textNavigationPayment.text = jsonObject.get("name").asString
//                        val imageLogo = itemView.findViewById<ImageView>(R.id.imageViewPaymentMethod)
//                        imageLogo.visibility = View.VISIBLE
//                        Glide.with(this).load(jsonObject.get("logo").asString)
//                            .priority(Priority.IMMEDIATE)
//                            .placeholder(R.drawable.ic_suitcase)
//                            .into(imageLogo)

                    }
                }catch (exception : IllegalStateException){
                    textNavigationPayment.text = payment_type_name
                }


            }
        }
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        lottieProfile.setOnClickListener{
            val intent = Intent(activity,CreditCardsActivity::class.java)
            intent.putExtra("lat",(activity as MainActivity).START_POINT_LAT)
            intent.putExtra("lon",(activity as MainActivity).START_POINT_LON)
            startActivity(intent)
        }

        linearPayment.setOnClickListener {
            val intent = Intent(activity, PaymentTypeActivity::class.java)
            intent.putExtra("lat",(activity as MainActivity).START_POINT_LAT)
            intent.putExtra("lon",(activity as MainActivity).START_POINT_LON)
            activity?.startActivity(intent)
        }

        textNavigation0.setOnClickListener{
            activity?.startActivity(Intent(activity, OrderHistoryActivity::class.java))
        }
        textNavigation1.setOnClickListener{
            activity?.startActivity(Intent(activity, AddressesActivity::class.java))
        }

        textNavigation2.setOnClickListener {
            activity?.startActivity(Intent(activity, SettingsActivity::class.java))
        }

        textNavigation3.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:" + Constants.CALL_CENTER_NUMBER)
            activity?.startActivity(dialIntent)
        }

        textNavigation4.setOnClickListener {
            activity?.startActivity(Intent(activity, InfoActivity::class.java))
        }



        textNavigation5.setOnClickListener {
            if (context!=null){

                val dialog = AlertDialog.Builder(context!!).create()
                val view = LayoutInflater.from(context).inflate(R.layout.dialog_log_out, null)

                val textYes = view.findViewById<TextView>(R.id.textYes)
                val textNo = view.findViewById<TextView>(R.id.textNo)

                textYes.setOnClickListener {
                    dialog.dismiss()
                    (activity as MainActivity).sharedPreferences.edit().putLong(
                        Constants.HIVE_USER_ID,
                        0
                    ).apply()
                    (activity as MainActivity).sharedPreferences.edit().putString(
                        Constants.HIVE_USER_TOKEN,
                        ""
                    ).apply()
                    (activity as MainActivity).sharedPreferences.edit().putBoolean(
                        Constants.PREF_FCM_REGISTERED,
                        false
                    ).apply()
                    context?.startActivity(Intent(context, AuthenticationActivity::class.java))
                    activity?.finish()
                }

                textNo.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.setView(view)
                dialog.show()





            }
        }

    }

    public fun onOpen(){
        lottieCoin.playAnimation()

    }

    public fun onBonusReady(bonus: Double){
        BONUS = bonus
        textViewBonus.text = "$bonus ${getString(R.string.currency)}"
    }

}