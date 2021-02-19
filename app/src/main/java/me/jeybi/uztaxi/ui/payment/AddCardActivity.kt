package me.jeybi.uztaxi.ui.payment

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_card.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.UzTaxiApplication
import me.jeybi.uztaxi.database.CreditCardEntity
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.CustomFragment
import me.jeybi.uztaxi.utils.CustomPagerAdapter
import java.util.*


class AddCardActivity : BaseActivity() {

    var mAdapter: CustomPagerAdapter? = null

    var CARD_NUMBER = ""
    var EXPIRY_DATE = "00/00"
    var CARD_NAME = ""


    override fun setLayoutId(): Int {
        return R.layout.activity_add_card
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        rvBack.setOnClickListener {
            onBackPressed()
        }

        mAdapter = CustomPagerAdapter(supportFragmentManager, arrayListOf())
        mViewPager!!.adapter = mAdapter!!
        mViewPager!!.setPageTransformer(false, mAdapter!!)
        mViewPager!!.currentItem = 0
        mViewPager!!.offscreenPageLimit = 3
        mViewPager!!.pageMargin = -200

        setUpEditTextAndViewPager()


    }

    private fun setUpEditTextAndViewPager() {

        editTextCardNumber.addTextChangedListener(object : TextWatcher {

            private val space = ' '

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                // Remove spacing char
                if (s.length > 0 && s.length % 5 == 0) {
                    val c = s[s.length - 1]
                    if (space == c) {
                        s.delete(s.length - 1, s.length)
                    }
                }
                // Insert char where needed.
                if (s.length > 0 && s.length % 5 == 0) {
                    val c = s[s.length - 1]
                    // Only if its a digit where there should be a space we insert a space
                    if (Character.isDigit(c) && TextUtils.split(
                            s.toString(),
                            space.toString()
                        ).size <= 3
                    ) {
                        s.insert(s.length - 1, space.toString())
                    }
                }


                mAdapter!!.getCurentFragment(mViewPager.currentItem)
                    .onCardNumberChanged(s.toString())
                checkIfCanAdd()
            }
        })

        editTextExpiryDate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, added: Int) {
                var current = s.toString()
                if (current.length == 2 && start == 1) {
                    editTextExpiryDate.setText("$current/")
                    editTextExpiryDate.setSelection(current.length + 1)
                } else if (current.length == 2 && before == 1) {
                    current = current.substring(0, 1)
                    editTextExpiryDate.setText(current)
                    editTextExpiryDate.setSelection(current.length)
                }
                mAdapter!!.getCurentFragment(mViewPager.currentItem)
                    .onCardExpiryChanged(current)
                checkIfCanAdd()
            }
        })

        editTextCardName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                mAdapter!!.getCurentFragment(mViewPager.currentItem)
                    .onCardNameChanged(s.toString())
                checkIfCanAdd()
            }

        })

        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                mAdapter!!.getCurentFragment(mViewPager.currentItem)
                    .onCardNumberChanged(CARD_NUMBER)
                mAdapter!!.getCurentFragment(mViewPager.currentItem)
                    .onCardExpiryChanged(EXPIRY_DATE)
                mAdapter!!.getCurentFragment(mViewPager.currentItem)
                    .onCardNameChanged(CARD_NAME)

                checkIfCanAdd()
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })

    }

    private fun checkIfCanAdd() {
        CARD_NUMBER = editTextCardNumber.text.toString().trim()
        CARD_NAME = editTextCardName.text.toString()
        EXPIRY_DATE = editTextExpiryDate.text.toString()

        if (CARD_NUMBER.length>=19&&CARD_NAME!=""&&EXPIRY_DATE.length>=4){
            rvReady.setBackgroundResource(R.drawable.bc_button_purple)
            rvReady.isClickable = true

            rvReady.setOnClickListener {
                (application as UzTaxiApplication).uzTaxiDatabase.getCardDAO()
                    .insertCard(
                        CreditCardEntity(
                            0,
                            CARD_NAME,
                            CARD_NUMBER,
                            EXPIRY_DATE,
                            mViewPager.currentItem
                        )
                    ).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        finish()
                    },{

                    })

                if (switchFilter.isChecked){
                    sharedPreferences.edit().putString(Constants.MAIN_CREDIT_CARD,CARD_NUMBER).apply()
                }

            }

        }else{
            rvReady.setBackgroundResource(R.drawable.bc_button_purple_disabled)
            rvReady.isClickable = false
        }
    }

    interface CardDetailsChangeListener{
        fun  onCardNumberChanged(cardNumber: String)
        fun onCardExpiryChanged(expiryDate : String)
        fun onCardNameChanged(cardName : String)
        fun editCardClicked(onDeleteClickListener : CustomFragment.OnDeleteClick)
        fun onBonusReady(bonus : Double)
    }

}