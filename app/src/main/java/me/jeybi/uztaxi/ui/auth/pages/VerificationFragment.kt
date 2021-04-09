package me.jeybi.uztaxi.ui.auth.pages

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import kotlinx.android.synthetic.main.fragment_verify_code.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.auth.AuthenticationActivity
import me.jeybi.uztaxi.ui.auth.AuthenticationController


class VerificationFragment() : BaseFragment(), AuthenticationController.OnErrorListener {

    override fun setLayoutId(): Int {
        return R.layout.fragment_verify_code
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {
        setUpEditTexts()

        textViewResend.setOnClickListener {
            progressBarCode.visibility = View.VISIBLE
            textViewError.text = ""
            (activity as AuthenticationActivity).onResendClicked(this)
        }

        rvConfirm.setOnClickListener {
            if (!smsCode1.text.isNullOrEmpty() && !smsCode2.text.isNullOrEmpty() && !smsCode3.text.isNullOrEmpty() && !smsCode4.text.isNullOrEmpty()) {
                if (progressBarCode.visibility == View.GONE) {
                    progressBarCode.visibility = View.VISIBLE
                    (activity as AuthenticationActivity).onVerifyClicked(
                        "${smsCode1.text}${smsCode2.text}${smsCode3.text}${smsCode4.text}",
                        this
                    )
                }
            } else {
                textViewError.text = getString(R.string.verification_code_wrong)
            }
        }

    }

    private fun setUpEditTexts() {
        smsCode1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0) {
                    smsCode2.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        smsCode2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0) {
                    smsCode3.requestFocus()
                } else {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        smsCode2.setOnKeyListener { v, keyCode, _ -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (smsCode2.text.isEmpty()) {
                    smsCode1.requestFocus()
                }
            }
            false
        }
        smsCode3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0) {
                    smsCode4.requestFocus()
                } else {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        smsCode3.setOnKeyListener { v, keyCode, _ -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (smsCode3.text.isEmpty()) {
                    smsCode2.requestFocus()
                }
            }
            false
        }
        smsCode4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0) {
//                    smsCode1.requestFocus()
                    hideKeyboard(activity as AuthenticationActivity)

                } else {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        smsCode4.setOnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (smsCode4.text.isEmpty()) {
                    smsCode3.requestFocus()
                }
            }
            false
        }

        smsCode4.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(activity as AuthenticationActivity)
                return@OnEditorActionListener true
            }
            false
        })
    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onErrorOccured(message: String) {
        progressBarCode.visibility = View.GONE
        textViewError.text = message
        textViewError.setTextColor(Color.RED)
        if (message == "no") {
            textViewError.text = getString(R.string.sms_sent_successfully)
            textViewError.setTextColor(Color.GREEN)
        }
    }

}