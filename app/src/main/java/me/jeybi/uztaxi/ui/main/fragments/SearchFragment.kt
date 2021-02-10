package me.jeybi.uztaxi.ui.main.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.bottom_sheet_search.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.*
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.adapters.SearchAdapter
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.ui.main.MainController
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.NaiveHmacSigner

class SearchFragment : BaseFragment(), SearchAdapter.SearchItemClickListener,
    MainController.SearchCancelListener {

    override fun setLayoutId(): Int {
        return R.layout.bottom_sheet_search
    }

    var searchDisposables = CompositeDisposable()


    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        recyclerViewSearchHistory.layoutManager = LinearLayoutManager(activity)

        loadOrdersHistory()

        rvWhereTo.setOnClickListener {
            editTextSearch.isFocusable = true
            editTextSearch.isFocusableInTouchMode = true
            editTextSearch.requestFocus()
            editTextSearch.setHintTextColor(Color.parseColor("#D6CAD9"))
            (activity as MainActivity).onSearchClicked(this)
            rvFromWhere.visibility = View.VISIBLE
            editTextFromWhere.setText((activity as MainActivity).CURRENT_ADDRESS)
            editTextFromWhere.isFocusable = true
            editTextFromWhere.isFocusableInTouchMode = true

        }


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
            (activity as MainActivity).onDestinationPickClicked()
//            imageMap.setImageResource(R.drawable.ic_done)


        }

    }

    private fun loadOrdersHistory() {

        progressBarSearch.visibility = View.VISIBLE
        searchDisposables.add(
            RetrofitHelper.apiService(Constants.BASE_URL)
                .getAddressHistory(
                    Constants.HIVE_PROFILE,
                    NaiveHmacSigner.DateSignature(),
                    NaiveHmacSigner.AuthSignature(
                        (activity as MainActivity).HIVE_USER_ID,
                        (activity as MainActivity).HIVE_TOKEN,
                        "GET",
                        "/api/client/mobile/2.0/history"
                    )
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if (it.isSuccessful&&it.body()!=null) {
                        textViewNoAddress.visibility = View.GONE

                        val geocodes = ArrayList<GeocodeFeature>()
                        for (item in it.body()!!){
                            if (item.route.size>1){
                                val geocodeFeature = GeocodeFeature("", Geometry("", arrayListOf(item.route[1].position!!.lon,item.route[1].position!!.lat)),
                                    GeocodeProperty("","",item.route[1].name,null,null,null,null,null)
                                )
                                geocodes.add(geocodeFeature)
                            }
                        }

                        recyclerViewSearchHistory.adapter = SearchAdapter(activity!!,geocodes,this)
                    }
                    progressBarSearch.visibility = View.GONE
//                    textViewNoAddress.visibility = View.GONE
                }, {
                    progressBarSearch.visibility = View.GONE
//                    textViewNoAddress.visibility = View.GONE
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
                    (activity as MainActivity).sharedPreferences.getString(Constants.NOMINATIM_LANGUAGE,"ru") ?: "ru",
                    Constants.GEOCODE_SOURCE
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    when (it.code()) {
                        Constants.STATUS_SUCCESSFUL -> {
                            if (it.body() != null ) {
                                progressBarSearch.visibility = View.GONE
                                recyclerViewSearchHistory.adapter = SearchAdapter(activity!!,it.body()!!.features, this)
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
        (activity as MainActivity).onBottomSheetSearchItemClicked(latitude, longitude, title)
    }

    override fun onSearchCancel() {
        rvFromWhere.visibility = View.GONE
        editTextFromWhere.setText("")
        (activity as MainActivity).hideKeyboard()
        editTextFromWhere.isFocusable = false
        editTextFromWhere.isFocusableInTouchMode = false
    }

    override fun onSearchStart() {
        rvFromWhere.visibility = View.VISIBLE
        editTextFromWhere.setText((activity as MainActivity).CURRENT_ADDRESS)
        editTextFromWhere.isFocusable = true
        editTextFromWhere.isFocusableInTouchMode = true
    }


}