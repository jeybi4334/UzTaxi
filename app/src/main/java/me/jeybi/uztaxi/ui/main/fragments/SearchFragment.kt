package me.jeybi.uztaxi.ui.main.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.bottom_sheet_search.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.UzTaxiApplication
import me.jeybi.uztaxi.model.*
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.adapters.AddressAdapter
import me.jeybi.uztaxi.ui.adapters.SearchAdapter
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.ui.main.MainController
import me.jeybi.uztaxi.utils.Constants


class SearchFragment : BaseFragment(), SearchAdapter.SearchItemClickListener,
    MainController.SearchCancelListener {

    override fun setLayoutId(): Int {
        return R.layout.bottom_sheet_search
    }

    var searchDisposables = CompositeDisposable()


    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        rvWhereTo.setOnClickListener {
            editTextSearch.isFocusable = true
            editTextSearch.isFocusableInTouchMode = true
            editTextSearch.requestFocus()
            editTextSearch.setHintTextColor(Color.parseColor("#D6CAD9"))
            (activity as MainActivity).onSearchClicked(this)
//            rvFromWhere.visibility = View.VISIBLE
//            editTextFromWhere.setText((activity as MainActivity).CURRENT_ADDRESS)
//            editTextFromWhere.isFocusable = true
//            editTextFromWhere.isFocusableInTouchMode = true

        }

        loadSavedAdresses()

        rvDelivery.setOnClickListener {

        }

        rvAddAddress.setOnClickListener {
            (activity as MainActivity).onAddAddressClicked()

        }

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 1 && s != null) {
                    searchGeocode(s.toString())
                } else {
                    loadSavedAdresses()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        editTextSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    (activity as MainActivity).hideKeyboard()
                    return true
                }
                return false
            }

        })

        editTextSearch.setOnClickListener {
            rvWhereTo.performClick()
        }

        imageMap.setOnClickListener {
            (activity as MainActivity).onDestinationPickClicked(Constants.DESTINATION_PICK_ORDEDR)
//            imageMap.setImageResource(R.drawable.ic_done)


        }

    }

    fun loadSavedAdresses() {
        textViewNoAddress.visibility = View.GONE
        progressBarSearch.visibility = View.GONE
        (activity as MainActivity).mainDisposables
            .add(
                (activity!!.application as UzTaxiApplication).uzTaxiDatabase
                    .getAddressDAO().getAddresses()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({


                        val flexboxLayoutManager = FlexboxLayoutManager(activity).apply {
                            flexWrap = FlexWrap.WRAP
                            flexDirection = FlexDirection.ROW
                            alignItems = AlignItems.STRETCH
                        }


                        recyclerViewSearchHistory.layoutManager = flexboxLayoutManager


//                        recyclerViewSearchHistory.layoutManager = StaggeredGridLayoutManager(
//                            2,
//                            StaggeredGridLayoutManager.VERTICAL
//                        )

                        recyclerViewSearchHistory.adapter = AddressAdapter(it)
                    }, {

                    })
            )
    }

    private fun searchGeocode(keyWord: String) {
        progressBarSearch.visibility = View.VISIBLE
        textViewNoAddress.visibility = View.GONE
        recyclerViewSearchHistory.adapter = null
        searchDisposables.add(
//            RetrofitHelper.apiService(Constants.BASE_URL_MILLENIUM)
            RetrofitHelper.apiService(Constants.BASE_URL_GEOCODE)
                .geocode(
                    keyWord,
                    (activity as MainActivity).CURRENT_LATITUDE,
                    (activity as MainActivity).CURRENT_LONGITUDE,
                    Constants.GEOCODE_RADIUS,
                    (activity as MainActivity).CURRENT_LATITUDE,
                    (activity as MainActivity).CURRENT_LONGITUDE,
                    Constants.GEOCODE_COUNTRY,
                    Constants.GEOCODE_TOKEN,
                    Constants.GEOCODE_LIMIT,
                    (activity as MainActivity).getCurrentLanguage().toLanguageTag(),
                    Constants.GEOCODE_SOURCE
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    when (it.code()) {
                        Constants.STATUS_SUCCESSFUL -> {
                            if (it.body() != null) {
                                progressBarSearch.visibility = View.GONE
                                recyclerViewSearchHistory.layoutManager = LinearLayoutManager(
                                    activity,
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )
                                recyclerViewSearchHistory.adapter = SearchAdapter(
                                    activity!!,
                                    it.body()!!.features,
                                    this
                                )
                            } else {
                                onCouldntFindAnything()
                            }
                        }
                        Constants.STATUS_BAD_REQUEST -> {
                            onCouldntFindAnything()
                        }
                    }
                }, {
                    onCouldntFindAnything()
                })
        )


    }

    private fun onCouldntFindAnything() {
        progressBarSearch.visibility = View.GONE
        textViewNoAddress.visibility = View.VISIBLE
        textViewNoAddress.text = getString(R.string.couldnot_find_anything)
    }

//


    override fun onDestroy() {
        super.onDestroy()
        searchDisposables.dispose()
    }


    override fun onSearchClicked(latitude: Double, longitude: Double, title: String) {
        (activity as MainActivity).hideKeyboard()
        (activity as MainActivity).onBottomSheetSearchItemClicked(latitude, longitude, title)
    }

    override fun onSearchCancel() {
//        rvFromWhere.visibility = View.GONE
//        editTextFromWhere.setText("")
//        (activity as MainActivity).hideKeyboard()
//        editTextFromWhere.isFocusable = false
//        editTextFromWhere.isFocusableInTouchMode = false
    }

    override fun onSearchStart() {
//        rvFromWhere.visibility = View.VISIBLE
//        editTextFromWhere.setText((activity as MainActivity).CURRENT_ADDRESS)
//        editTextFromWhere.isFocusable = true
//        editTextFromWhere.isFocusableInTouchMode = true
    }


}