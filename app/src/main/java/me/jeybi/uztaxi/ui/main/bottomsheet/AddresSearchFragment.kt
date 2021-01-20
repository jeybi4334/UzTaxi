package me.jeybi.uztaxi.ui.main.bottomsheet

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.bottomsheet_address.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.adapters.SearchAdapter
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.utils.Constants


class AddresSearchFragment : BottomSheetDialogFragment(), SearchAdapter.SearchItemClickListener{


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_address, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        recyclerViewSearch.layoutManager = LinearLayoutManager(activity)


//        fillRecyclerViewWithDemoData()

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
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchGeocode(editTextSearch.text.toString())
                    recyclerViewSearch.adapter = null
                    return true
                }
                return false
            }

        })


        imageMap.setOnClickListener {
            (activity as MainActivity).onDestinationPickClicked()
//            imageMap.setImageResource(R.drawable.ic_done)


        }
    }

    var searchDisposables = CompositeDisposable()


    private fun searchGeocode(keyWord: String) {
        progressBarSearch.visibility = View.VISIBLE
        textViewNoAddress.visibility = View.GONE
        recyclerViewSearch.adapter = null
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
                            Log.d("ADASD", "SUCCESS ")
                            if (it.body() != null && it.body()!!.size > 0) {
                                progressBarSearch.visibility = View.GONE
                                recyclerViewSearch.adapter = SearchAdapter(it.body()!!, this)
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



    override fun onSearchClicked(latitude: Double, longitude: Double, title: String) {
        (activity as MainActivity).onBottomSheetSearchItemClicked(latitude, longitude, title)
    }

}