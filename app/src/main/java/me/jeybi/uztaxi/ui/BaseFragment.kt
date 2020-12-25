package me.jeybi.uztaxi.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import me.jeybi.uztaxi.UzTaxiApplication

abstract class BaseFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(setLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = (activity?.application as UzTaxiApplication).sharedPreferences
        onViewDidCreate(savedInstanceState)
    }

    @LayoutRes
    abstract fun setLayoutId() : Int

    abstract fun onViewDidCreate(savedInstanceState: Bundle?)


}