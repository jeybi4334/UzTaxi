package me.jeybi.uztaxi.ui.payment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_payment_type.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.PaymentMethod
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.NaiveHmacSigner

class PaymentTypeActivity : BaseActivity() {

    val paymentTypesDisposable = CompositeDisposable()
    var LAT = 0.0
    var LON = 0.0

    var PAYMENT_TYPE = ""

    override fun setLayoutId(): Int {
        return R.layout.activity_payment_type
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        if (intent.extras!=null){
            LAT = intent.extras!!.getDouble("lat")
            LON = intent.extras!!.getDouble("lon")

        }

        PAYMENT_TYPE = sharedPreferences.getString(Constants.PAYMENT_TYPE,Constants.PAYMENT_TYPE_CASH) ?: Constants.PAYMENT_TYPE_CASH

        rvBack.setOnClickListener {
            onBackPressed()
        }

        loadPaymentTypes(LAT,LON)



    }

    fun onPaymentTypesLoaded(paymentTypes : ArrayList<PaymentMethod>){
        linearPaymentTypes.removeAllViews()
        for (paymentMethod in paymentTypes){

            val itemView = LayoutInflater.from(this).inflate(R.layout.item_payment_method,activityPay,false)
            val imageViewDone = itemView.findViewById<ImageView>(R.id.imageViewDone)

            when (paymentMethod.kind) {
                Constants.PAYMENT_TYPE_CASH -> {
                    if (PAYMENT_TYPE==Constants.PAYMENT_TYPE_CASH){
                        imageViewDone.visibility = View.VISIBLE
                    }else{
                        imageViewDone.visibility = View.GONE
                    }
                    itemView.findViewById<TextView>(R.id.textViewPaymentType).text = getString(R.string.pay_cash)

                    itemView.findViewById<LinearLayout>(R.id.parentPaymentMethod).setOnClickListener {
                        PAYMENT_TYPE=Constants.PAYMENT_TYPE_CASH
                        imageViewDone.visibility = View.VISIBLE
                        sharedPreferences.edit().putString(Constants.PAYMENT_TYPE,Constants.PAYMENT_TYPE_CASH).apply()
                        sharedPreferences.edit().putString(Constants.PAYMENT_TYPE_NAME,"").apply()
                        sharedPreferences.edit().putLong(Constants.PAYMENT_TYPE_ID,0L).apply()
                        loadPaymentTypes(LAT,LON)
                    }
                }

                Constants.PAYMENT_TYPE_CONTRACTOR -> {
                    if (PAYMENT_TYPE==Constants.PAYMENT_TYPE_CONTRACTOR){
                        imageViewDone.visibility = View.VISIBLE
                    }else{
                        imageViewDone.visibility = View.GONE
                    }
                    if (PAYMENT_TYPE==Constants.PAYMENT_TYPE_CONTRACTOR){
                        imageViewDone.visibility = View.VISIBLE
                    }else{
                        imageViewDone.visibility = View.GONE
                    }

                    try {
                        val jsonObject: JsonObject = JsonParser.parseString(paymentMethod.name).asJsonObject

                        if (jsonObject.isJsonObject){
                            itemView.findViewById<TextView>(R.id.textViewPaymentType).text = jsonObject.get("name").asString
                            val imageLogo = itemView.findViewById<ImageView>(R.id.imageViewPaymentMethod)
                            imageLogo.visibility = View.VISIBLE
                            Glide.with(this).load(jsonObject.get("logo").asString)
                                .priority(Priority.IMMEDIATE)
                                .placeholder(R.drawable.ic_suitcase)
                                .into(imageLogo)

                        }
                    }catch (exception : IllegalStateException){
                        itemView.findViewById<ImageView>(R.id.imageViewPaymentMethod).setImageResource(R.drawable.ic_suitcase)
                        itemView.findViewById<TextView>(R.id.textViewPaymentType).text = paymentMethod.name
                    }

                    itemView.findViewById<LinearLayout>(R.id.parentPaymentMethod).setOnClickListener {
                        PAYMENT_TYPE=Constants.PAYMENT_TYPE_CONTRACTOR
                        imageViewDone.visibility = View.VISIBLE
                        sharedPreferences.edit().putString(Constants.PAYMENT_TYPE,Constants.PAYMENT_TYPE_CONTRACTOR).apply()
                        sharedPreferences.edit().putString(Constants.PAYMENT_TYPE_NAME,paymentMethod.name?:"").apply()
                        sharedPreferences.edit().putLong(Constants.PAYMENT_TYPE_ID,paymentMethod.id?:0L).apply()

                        loadPaymentTypes(LAT,LON)
                    }
                }
            }


            linearPaymentTypes.addView(itemView)


        }
    }


    fun loadPaymentTypes(latitude : Double,longitude : Double){
        paymentTypesDisposable.add(
            RetrofitHelper.apiService(Constants.BASE_URL)
                .getPaymentOptions(
                    getCurrentLanguage().toLanguageTag(),
                    Constants.HIVE_PROFILE,
                    "$latitude $longitude",
                    NaiveHmacSigner.DateSignature(),
                    NaiveHmacSigner.AuthSignature(
                        HIVE_USER_ID,
                        HIVE_TOKEN,
                        "GET",
                        "/api/client/mobile/1.0/payment-methods"
                    )
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.isSuccessful && it.body() != null) {
                        onPaymentTypesLoaded(it.body()!!)
                    }
                }, {

                })
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        paymentTypesDisposable.dispose()
    }

}