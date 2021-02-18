package me.jeybi.uztaxi.ui.history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_order_history.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.ui.adapters.OrderHistoryAdapter
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.NaiveHmacSigner

class OrderHistoryActivity : BaseActivity() {
    override fun setLayoutId(): Int {
        return R.layout.activity_order_history
    }

    var PAGE = 0

    val disposables = CompositeDisposable()

    var  HIVE_TOKEN =  ""
    var HIVE_USER_ID = 0L

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        HIVE_TOKEN = sharedPreferences.getString(Constants.HIVE_USER_TOKEN, "") ?: ""
        HIVE_USER_ID = sharedPreferences.getLong(Constants.HIVE_USER_ID, 0)

        rvBack.setOnClickListener {
            onBackPressed()
        }
        loadOrderHistory()

        swipeRefreshLayout.setOnRefreshListener {
            loadOrderHistory()
        }

        recyclerViewOrderHistory.layoutManager = LinearLayoutManager(this)

    }


    private fun loadOrderHistory(){


        disposables.add(
            RetrofitHelper.apiService(Constants.BASE_URL)
                .getOrderHistory(
                    getCurrentLanguage().toLanguageTag(),
                    Constants.HIVE_PROFILE,
                    NaiveHmacSigner.DateSignature(),
                    NaiveHmacSigner.AuthSignature(
                        HIVE_USER_ID,
                         HIVE_TOKEN,
                        "GET",
                        "/api/client/mobile/2.0/history"),
                    PAGE,
                    15
                    )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                         if (it.isSuccessful&&it.body()!=null){
                             swipeRefreshLayout.isRefreshing = false
                             recyclerViewOrderHistory.adapter = OrderHistoryAdapter(this,it.body()!!)
                         }
                },{

                })
        )

    }

}