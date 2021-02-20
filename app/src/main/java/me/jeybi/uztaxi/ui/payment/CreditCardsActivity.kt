package me.jeybi.uztaxi.ui.payment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_add_card.*
import kotlinx.android.synthetic.main.activity_credit_cards.*
import kotlinx.android.synthetic.main.activity_credit_cards.mViewPager
import kotlinx.android.synthetic.main.activity_credit_cards.rvBack
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.UzTaxiApplication
import me.jeybi.uztaxi.database.CreditCardEntity
import me.jeybi.uztaxi.model.Transaction
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.ui.adapters.TransactionAdapter
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.CustomFragment
import me.jeybi.uztaxi.utils.CustomPagerAdapter
import me.jeybi.uztaxi.utils.NaiveHmacSigner

class CreditCardsActivity : BaseActivity() {

    val cardsDisposable = CompositeDisposable()
    var mAdapter: CustomPagerAdapter? = null

    var BONUS = 0.0
    val cardData = ArrayList<CreditCardEntity>()

    var LAT = 0.0
    var LON = 0.0

    override fun setLayoutId(): Int {
        return R.layout.activity_credit_cards
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        rvBack.setOnClickListener {
            onBackPressed()
        }

        if (intent.extras!=null){
            LAT = intent.extras!!.getDouble("lat")
            LON = intent.extras!!.getDouble("lon")

        }

        rvEdit.setOnClickListener {
            for(frag in mAdapter!!.fragmentsList){
                frag.editCardClicked(object :
                    CustomFragment.OnDeleteClick {
                    override fun onDeletedClicked() {
                        if (cardData.size>2){
                            cardsDisposable.add(
                                (application as UzTaxiApplication).uzTaxiDatabase.getCardDAO()
                                    .deleteCard(cardData[mViewPager.currentItem].id)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe({
                                        loadCardsFromDatabase()
                                    },{

                                    })
                            )
                        }
                    }
                })
            }

//            mAdapter?.getCurentFragment(mViewPager.currentItem)?.editCardClicked(object :
//                CustomFragment.OnDeleteClick {
//                override fun onDeletedClicked() {
//                    if (cardData.size>2){
//                       cardsDisposable.add(
//                           (application as UzTaxiApplication).uzTaxiDatabase.getCardDAO()
//                               .deleteCard(cardData[mViewPager.currentItem].id)
//                               .observeOn(AndroidSchedulers.mainThread())
//                               .subscribeOn(Schedulers.io())
//                               .subscribe({
//                                  loadCardsFromDatabase()
//                               },{
//
//                               })
//                       )
//                    }
//                }
//            })
        }

        loadTransactions()
    }

    private fun getBonus() {

        cardsDisposable.add(RetrofitHelper.apiService(Constants.BASE_URL)
            .getBonuses(
                getCurrentLanguage().toLanguageTag(),
                "${LAT} ${LON}",
                Constants.HIVE_PROFILE,
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    HIVE_USER_ID,
                    HIVE_TOKEN,
                    "GET",
                    "/api/client/mobile/2.1/bonuses"
                )
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful && it.body() != null) {
                    val balance = it.body()!!.balance
                    BONUS = balance
                    if (mAdapter!=null)
                    for(frag in mAdapter!!.fragmentsList){
                        frag.onBonusReady(BONUS)
                    }

                    mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
                        override fun onPageScrolled(
                            position: Int,
                            positionOffset: Float,
                            positionOffsetPixels: Int
                        ) {


                        }

                        override fun onPageSelected(position: Int) {
                            mAdapter?.getCurentFragment(position)?.onBonusReady(BONUS)
                        }

                        override fun onPageScrollStateChanged(state: Int) {

                        }
                    })
                }
            }, {

            })
        )
    }

    private fun loadTransactions(){
        recyclerViewTransactions.layoutManager = LinearLayoutManager(this)

        val transactions = ArrayList<Transaction>()

        transactions.add(Transaction(0,"Для оплаты проезда",0,12500.0,"Фев 21","С карты **** 6966"))
        transactions.add(Transaction(0,"Из поездки",1,1500.0,"Сен 03","В кошелек **** 4343"))
        transactions.add(Transaction(0,"Для оплаты проезда",0,17800.0,"Май 23","С карты **** 4343"))
        transactions.add(Transaction(0,"Для оплаты проезда",0,6500.0,"Авг 23","С карты **** 4343"))
        transactions.add(Transaction(0,"Из-за использования кредитной карты",1,500.0,"Янв 10","В кошелек **** 4343"))
        transactions.add(Transaction(0,"Для оплаты проезда",1,1600.0,"Окт 17","В кошелек **** 4343"))
        transactions.add(Transaction(0,"Для оплаты проезда",0,10200.0,"Июнь 30","С карты **** 4343"))
        transactions.add(Transaction(0,"Для оплаты проезда",1,4300.0,"Авг 02","В кошелек **** 4343"))
        transactions.add(Transaction(0,"Для оплаты проезда",0,25000.0,"Апр 09","С карты **** 4343"))

        recyclerViewTransactions.adapter = TransactionAdapter(transactions)
        recyclerViewTransactions.scheduleLayoutAnimation()
    }

    private fun loadCardsFromDatabase() {
        mViewPager.removeAllViews()
        mAdapter?.clearAll()
        mViewPager.adapter = null
        cardData.clear()

        cardsDisposable.add(
            (application as UzTaxiApplication).uzTaxiDatabase.getCardDAO()
                .getCreditCards().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({

                   for (item in it){
                       cardData.add(CreditCardEntity(item.id,item.cardName,item.cardNumber,item.cardExpiry,item.cardDesign))
                   }
                    cardData.add(CreditCardEntity(0,"","","",1003))
                    cardData.add(CreditCardEntity(0,"","","",1001))

                    mAdapter = CustomPagerAdapter(supportFragmentManager, cardData)
                    mViewPager.adapter = mAdapter!!
                    mViewPager.setPageTransformer(false, mAdapter!!)
                    mViewPager.currentItem = 0
                    mViewPager.offscreenPageLimit = 6
                    mViewPager.pageMargin = -200
                    getBonus()
                },{

                })
        )
    }

    override fun onResume() {
        super.onResume()
        loadCardsFromDatabase()
    }

    override fun onDestroy() {
        super.onDestroy()
        cardsDisposable.dispose()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
//        super.onSaveInstanceState(outState, outPersistentState)
    }

}