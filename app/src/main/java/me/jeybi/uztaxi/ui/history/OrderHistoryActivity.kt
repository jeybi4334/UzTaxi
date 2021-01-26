package me.jeybi.uztaxi.ui.history

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_order_history.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseActivity

class OrderHistoryActivity : BaseActivity() {
    override fun setLayoutId(): Int {
        return R.layout.activity_order_history
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        rvBack.setOnClickListener {
            onBackPressed()
        }
        loadOrderHistory()

        swipeRefreshLayout.setOnRefreshListener {
            loadOrderHistory()
        }

    }


    private fun loadOrderHistory(){


    }

}