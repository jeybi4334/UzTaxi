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
import me.jeybi.uztaxi.model.SearchItemModel
import me.jeybi.uztaxi.model.SearchedAddress
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.adapters.SearchAdapter
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.ui.main.MainController
import me.jeybi.uztaxi.utils.Constants

class SearchFragment : BaseFragment(), SearchAdapter.SearchItemClickListener,MainController.SearchCancelListener{

    override fun setLayoutId(): Int {
        return R.layout.bottom_sheet_search
    }

    var searchDisposables = CompositeDisposable()


    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        recyclerViewSearchHistory.layoutManager = LinearLayoutManager(activity)


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

//        fillRecyclerViewWithDemoData()

        editTextSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count>1&&s!=null){
                    searchGeocode(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        editTextSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchGeocode(editTextSearch.text.toString())
                    recyclerViewSearchHistory.adapter = null
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

    private fun searchGeocode(keyWord: String) {
        progressBarSearch.visibility = View.VISIBLE
        textViewNoAddress.visibility = View.GONE
        recyclerViewSearchHistory.adapter = null
        searchDisposables.add(
            RetrofitHelper.apiService(Constants.BASE_URL_MILLENIUM)
                .geocodePoint(
                    Constants.HIVE_MILLENIUM,
                    "${(activity as MainActivity).CURRENT_LATITUDE} ${(activity as MainActivity).CURRENT_LONGITUDE}",
                    keyWord
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    when (it.code()) {
                        Constants.STATUS_SUCCESSFUL -> {
                            Log.d("ADASD","SUCCESS ")
                            if (it.body()!=null&&it.body()!!.size>0){
                                progressBarSearch.visibility = View.GONE
                                recyclerViewSearchHistory.adapter = SearchAdapter(it.body()!!,this)
                            }else{
                                onCouldntFindAnything()
                            }
                        }
                        Constants.STATUS_BAD_REQUEST->{
                            onCouldntFindAnything()
                        }
                    }
                }, {
                    onCouldntFindAnything()
                })
        )


    }

    private fun onCouldntFindAnything(){
        progressBarSearch.visibility = View.GONE
        textViewNoAddress.visibility = View.VISIBLE
        textViewNoAddress.text = "Мы не смогли найти ничего по вашему запросу :("
    }

//


    override fun onDestroy() {
        super.onDestroy()
        searchDisposables.dispose()
    }



    override fun onSearchClicked(latitude: Double, longitude: Double,title : String) {
        (activity as MainActivity).onBottomSheetSearchItemClicked(latitude,longitude,title)
    }

    override fun onSearchCancel() {
        rvFromWhere.visibility = View.GONE
        editTextFromWhere.setText("")
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