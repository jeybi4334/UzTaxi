package me.jeybi.uztaxi.ui.auth.pages

import android.app.Activity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import kotlinx.android.synthetic.main.fragment_phone_number.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.auth.AuthenticationActivity
import me.jeybi.uztaxi.ui.auth.AuthenticationController
import me.jeybi.uztaxi.utils.NaiveHmacSigner


class PhoneNumberFragment : BaseFragment(), AuthenticationController.OnErrorListener {
    override fun setLayoutId(): Int {
        return R.layout.fragment_phone_number
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        text2.movementMethod = LinkMovementMethod.getInstance()

        phone_input.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
            }
        }

        phone_input.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(v)
                true
            } else false
        })


        rvLogin.setOnClickListener {
            if (progressBarPhone.visibility == View.GONE){
                (activity as AuthenticationActivity).onPhoneNumberEntered("+998${phone_input.rawText}",this)
                progressBarPhone.visibility = View.VISIBLE
            }
        }




    }


    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onErrorOccured(message: String) {
        textViewError.text = message
        progressBarPhone.visibility = View.GONE
    }

}