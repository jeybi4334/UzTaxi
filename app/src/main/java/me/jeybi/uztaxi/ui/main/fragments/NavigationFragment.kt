package me.jeybi.uztaxi.ui.main.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.navigation_menu.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.auth.AuthenticationActivity
import me.jeybi.uztaxi.ui.history.OrderHistoryActivity
import me.jeybi.uztaxi.ui.info.InfoActivity
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.ui.settings.SettingsActivity
import me.jeybi.uztaxi.utils.Constants

class NavigationFragment : BaseFragment() {

    override fun setLayoutId(): Int {
        return R.layout.navigation_menu
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        textNavigation0.setOnClickListener{
            activity?.startActivity(Intent(activity, OrderHistoryActivity::class.java))
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
        lottieProfile.playAnimation()

    }

    public fun onBonusReady(bonus: Double){
        textViewBonus.text = "$bonus сум"
    }

}