package me.jeybi.uztaxi.ui.address

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.RelativeLayout
import androidx.core.view.children
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_addresses.*

import kotlinx.android.synthetic.main.bottom_sheet_where.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.UzTaxiApplication
import me.jeybi.uztaxi.database.AddressEntity
import me.jeybi.uztaxi.model.RouteCoordinates
import me.jeybi.uztaxi.model.RouteItem
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.ui.adapters.AddressAdapter
import me.jeybi.uztaxi.ui.adapters.RouteAdapter
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.ui.main.fragments.SearchFragment

class AddressesActivity : BaseActivity(), AddressAdapter.OnItemMoveListener {

    override fun setLayoutId(): Int {
        return R.layout.activity_addresses
    }

    val addressDisposable = CompositeDisposable()

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        rvBack.setOnClickListener {
            onBackPressed()
        }

        loadSavedAdresses()

        rvEdit.setOnClickListener {
            for(item in recyclerViewAddresses.children){
               val rvDelete =  item.findViewById<RelativeLayout>(R.id.rvDeleteAddress)
                if (rvDelete.scaleX==1f){
                    item.clearAnimation()
                    rvDelete.animate().scaleX(0f).scaleY(0f).setDuration(200).setInterpolator(
                        AnticipateInterpolator()
                    ).start()
                }else{
                    item.startAnimation(AnimationUtils.loadAnimation(this,R.anim.wobble))
                    rvDelete.animate().scaleX(1f).scaleY(1f).setDuration(400).setInterpolator(
                        OvershootInterpolator()
                    ).start()
                }
            }
        }

    }

    fun loadSavedAdresses() {

        addressDisposable
            .add(
                (application as UzTaxiApplication).uzTaxiDatabase
                    .getAddressDAO().getAddresses()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({


                        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
                            flexWrap = FlexWrap.WRAP
                            flexDirection = FlexDirection.ROW
                            alignItems = AlignItems.STRETCH
                        }


                        recyclerViewAddresses.layoutManager = flexboxLayoutManager

                        recyclerViewAddresses.adapter = AddressAdapter( ArrayList(it),this,this)
                        itemTouchHelper.attachToRecyclerView(recyclerViewAddresses)
                        recyclerViewAddresses.scheduleLayoutAnimation()

                    }, {

                    })
            )
    }

    fun startDragging(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onDestroy() {
        super.onDestroy()
        addressDisposable.dispose()
    }

    override fun onItemMoved(movedItem: AddressEntity, newItem: AddressEntity) {

        addressDisposable.add(
            (application as UzTaxiApplication).uzTaxiDatabase
                .getAddressDAO().updateAddress(movedItem.id,newItem.id.toLong())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    addressDisposable.add(
                        (application as UzTaxiApplication).uzTaxiDatabase
                            .getAddressDAO().updateAddress(newItem.id,movedItem.id.toLong())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({
                            },{}))

                },{}))

    }

    override fun onItemRemoved(id: Int,adapterPosition : Int) {
        addressDisposable.add(
            (application as UzTaxiApplication).uzTaxiDatabase
                .getAddressDAO().deleteAddress(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    val adapter = recyclerViewAddresses.adapter as AddressAdapter
                    adapter.notifyItemRemoved(adapterPosition)
                },{}))
    }


    private val itemTouchHelper by lazy {
        // 1. Note that I am specifying all 4 directions.
        //    Specifying START and END also allows
        //    more organic dragging than just specifying UP and DOWN.
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or
                        ItemTouchHelper.DOWN
                    or
                        ItemTouchHelper.START or
                        ItemTouchHelper.END,
                0
            ) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {

                    val adapter = recyclerView.adapter as AddressAdapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    // 2. Update the backing model. Custom implementation in
                    //    MainRecyclerViewAdapter. You need to implement
                    //    reordering of the backing model inside the method.

                    adapter.moveItem(viewHolder, target, from, to)
                    // 3. Tell adapter to render the model update.
                    adapter.notifyItemMoved(from, to)

//                    val fromEmoji = ROUTE_DATA[from]
//                    ROUTE_DATA.removeAt(from)
//                    if (to < from) {
//                        ROUTE_DATA.add(to, fromEmoji)
//                    } else {
//                        ROUTE_DATA.add(to - 1, fromEmoji)
//                    }


                    return true
                }
                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                    // 4. Code block for horizontal swipe.
                    //    ItemTouchHelper handles horizontal swipe as well, but
                    //    it is not relevant with reordering. Ignoring here.
                }
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }

}