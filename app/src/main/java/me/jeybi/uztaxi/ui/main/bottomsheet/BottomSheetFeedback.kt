package me.jeybi.uztaxi.ui.main.bottomsheet

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.bottom_sheet_ride_finish.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.RateOrderBody
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.NaiveHmacSigner


class BottomSheetFeedback(val orderID: Long, val cost : Double, val usedBonus : Double) : BottomSheetDialogFragment(){

    var RATE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_ride_finish, container, false)

        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        startActivity(Intent(activity,MainActivity::class.java))
        activity?.finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textViewBillAmount.text = "${cost - usedBonus}"
        textViewCashBackAmount.text = "-$usedBonus сум"
        textViewOrderDate.text = NaiveHmacSigner.DateSignature()

        dialog!!.setOnShowListener { dialog -> // In a previous life I used this method to get handles to the positive and negative buttons
            // of a dialog in order to change their Typeface. Good ol' days.
            val d = dialog as BottomSheetDialog

            // This is gotten directly from the source of BottomSheetDialog
            // in the wrapInBottomSheet() method
            val bottomSheet =
                d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?

            // Right here!
            if (bottomSheet!=null)
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }

        rvConfirm.setOnClickListener {
//            if (RATE!=0){
//                progressFeedback.visibility = View.VISIBLE
//                textConfirm.visibility = View.GONE
//
//                val rateOrderBody = RateOrderBody(RATE,null,editTextFeedback.toString())
//
//                RetrofitHelper.apiService(Constants.BASE_URL)
//                    .rateOrder(
//                        Constants.HIVE_PROFILE,
//                        NaiveHmacSigner.DateSignature(),
//                        NaiveHmacSigner.AuthSignature((activity as MainActivity).HIVE_USER_ID,
//                            (activity as MainActivity).HIVE_TOKEN,
//                                "POST",
//                                "/api/client/mobile/1.1/orders/$orderID/feedback"
//                                ),orderID,rateOrderBody)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeOn(Schedulers.io())
//                    .subscribe({
//                               if (it.isSuccessful){
//                                   dismiss()
//                               }
//                        progressFeedback.visibility = View.VISIBLE
//                        textConfirm.visibility = View.GONE
//                    },{
//                        progressFeedback.visibility = View.VISIBLE
//                        textConfirm.visibility = View.GONE
//                    })
//
//
//
//            }
//
         dismiss()
        }


        imageStar1.setOnClickListener {
            rvConfirm.setBackgroundResource(R.drawable.bc_button_purple)
            imageStar1.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.star_pop))
            imageStar1.setImageResource(R.drawable.ic_star_selected)
            imageStar2.setImageResource(R.drawable.ic_star_unselected)
            imageStar3.setImageResource(R.drawable.ic_star_unselected)
            imageStar4.setImageResource(R.drawable.ic_star_unselected)
            imageStar5.setImageResource(R.drawable.ic_star_unselected)
            RATE = 1
        }
        imageStar2.setOnClickListener {
            rvConfirm.setBackgroundResource(R.drawable.bc_button_purple)
            imageStar2.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.star_pop))
            imageStar2.setImageResource(R.drawable.ic_star_selected)
            imageStar1.setImageResource(R.drawable.ic_star_selected)
            imageStar3.setImageResource(R.drawable.ic_star_unselected)
            imageStar4.setImageResource(R.drawable.ic_star_unselected)
            imageStar5.setImageResource(R.drawable.ic_star_unselected)
            RATE = 2
        }
        imageStar3.setOnClickListener {
            rvConfirm.setBackgroundResource(R.drawable.bc_button_purple)
            imageStar3.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.star_pop))
            imageStar3.setImageResource(R.drawable.ic_star_selected)
            imageStar1.setImageResource(R.drawable.ic_star_selected)
            imageStar2.setImageResource(R.drawable.ic_star_selected)
            imageStar4.setImageResource(R.drawable.ic_star_unselected)
            imageStar5.setImageResource(R.drawable.ic_star_unselected)
            RATE = 3
        }
        imageStar4.setOnClickListener {
            rvConfirm.setBackgroundResource(R.drawable.bc_button_purple)
            imageStar4.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.star_pop))
            imageStar4.setImageResource(R.drawable.ic_star_selected)
            imageStar1.setImageResource(R.drawable.ic_star_selected)
            imageStar2.setImageResource(R.drawable.ic_star_selected)
            imageStar3.setImageResource(R.drawable.ic_star_selected)
            imageStar5.setImageResource(R.drawable.ic_star_unselected)
            RATE = 4
        }
        imageStar5.setOnClickListener {
            rvConfirm.setBackgroundResource(R.drawable.bc_button_purple)
            imageStar5.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.star_pop))
            imageStar5.setImageResource(R.drawable.ic_star_selected)
            imageStar1.setImageResource(R.drawable.ic_star_selected)
            imageStar2.setImageResource(R.drawable.ic_star_selected)
            imageStar3.setImageResource(R.drawable.ic_star_selected)
            imageStar4.setImageResource(R.drawable.ic_star_selected)

            RATE = 5
        }

    }

}