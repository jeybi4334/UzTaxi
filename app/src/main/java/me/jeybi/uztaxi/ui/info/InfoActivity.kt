package me.jeybi.uztaxi.ui.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_info.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseActivity


class InfoActivity : BaseActivity() {
    override fun setLayoutId(): Int {
        return R.layout.activity_info
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        rvBack.setOnClickListener{
            onBackPressed()
        }

        rvTerms.setOnClickListener {

            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse( "https://uz.taxi/user-agreement-for-client/"))
            startActivity(browserIntent)
        }

        rvPolicy.setOnClickListener{
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(   "https://uz.taxi/app-client-privacy-policy/"))
            startActivity(browserIntent)
        }

    }

}