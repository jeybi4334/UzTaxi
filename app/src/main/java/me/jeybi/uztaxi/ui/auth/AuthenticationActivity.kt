package me.jeybi.uztaxi.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.ui.auth.pages.PhoneNumberFragment
import me.jeybi.uztaxi.ui.auth.pages.VerificationFragment
import me.jeybi.uztaxi.ui.main.MainActivity


class AuthenticationActivity : BaseActivity(), AuthenticationController.view {

    lateinit var presenter: AuthenticationPresenter

    val authDisposables = CompositeDisposable()

    override fun setLayoutId(): Int {
        return R.layout.activity_authentication
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {
        presenter = AuthenticationPresenter(this)
        changeFragment(PhoneNumberFragment(), false)
    }


    private fun changeFragment(newFragment: Fragment, backStack: Boolean) {

        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.contentAuthActivity, newFragment)
        if (backStack)
            transaction.addToBackStack(null)

        transaction.commit()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onPhoneNumberEntered(
        phoneNumber: String,
        phoneNumberListener: AuthenticationController.OnErrorListener
    ) {

        authDisposables.add(presenter.sendSmsToPhoneNumber(phoneNumber, phoneNumberListener))
    }

    override fun onSmsSent(id: Int) {
        changeFragment(VerificationFragment(), true)

    }

    override fun onResendClicked(onErrorListener: AuthenticationController.OnErrorListener) {

        authDisposables.add(presenter.resendSmsCode(onErrorListener))
    }

    override fun onVerifyClicked(
        code: String,
        onErrorListener: AuthenticationController.OnErrorListener
    ) {

        authDisposables.add(presenter.confirmSmsCode(code, onErrorListener))
    }

    override fun onUserVerified() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        authDisposables.dispose()
    }

}