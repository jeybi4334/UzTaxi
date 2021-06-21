package me.jeybi.uztaxi.ui.main.bottomsheet

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_search.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.adapters.SearchAdapter
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.utils.Constants

class SearchDialog(val title: String?, val delivery: Boolean) : BottomSheetDialogFragment(),
    SearchAdapter.SearchItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_search, container, false)
    }


    override fun onDismiss(dialog: DialogInterface) {
        (activity as MainActivity).hideKeyboard()
        super.onDismiss(dialog)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (title != null) {
            editTextSearch.hint = title
        }
        recyclerViewSearch.layoutManager = LinearLayoutManager(activity)


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


        textMap.setOnClickListener {
            if (!delivery)
                (activity as MainActivity).onDestinationPickClicked(Constants.DESTINATION_PICK_START,delivery)
            else
                (activity as MainActivity).onDestinationPickClicked(Constants.DESTINATION_PICK_ORDEDR,delivery)

            dismiss()
        }

    }


    var searchDisposables = CompositeDisposable()


    private fun searchGeocode(keyWord: String) {
        progressBarSearch.visibility = View.VISIBLE
        textViewNoAddress.visibility = View.GONE
        recyclerViewSearch.adapter = null
        searchDisposables.add(

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
                    (activity as MainActivity).sharedPreferences.getString(
                        Constants.NOMINATIM_LANGUAGE,
                        "uz"
                    ) ?: "uz",
                    Constants.GEOCODE_SOURCE
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    when (it.code()) {
                        Constants.STATUS_SUCCESSFUL -> {
                            if (it.body() != null) {
                                progressBarSearch.visibility = View.GONE
                                recyclerViewSearch.adapter = SearchAdapter(
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
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
        (activity as MainActivity).hideKeyboard()
        dismiss()
        (activity as MainActivity).onBottomSheetSearchItemClicked(latitude, longitude, title,delivery)
    }


}
