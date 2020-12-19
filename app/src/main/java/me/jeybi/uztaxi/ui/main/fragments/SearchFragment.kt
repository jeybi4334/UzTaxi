package me.jeybi.uztaxi.ui.main.fragments

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.bottom_sheet_search.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.SearchItemModel
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.adapters.SearchAdapter
import me.jeybi.uztaxi.ui.main.MainActivity

class SearchFragment : BaseFragment(), SearchAdapter.SearchItemClickListener {

    override fun setLayoutId(): Int {
        return R.layout.bottom_sheet_search
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        recyclerViewSearchHistory.layoutManager = LinearLayoutManager(activity)

        fillRecyclerViewWithDemoData()

    }

    private fun fillRecyclerViewWithDemoData() {
        val items = ArrayList<SearchItemModel>()
        items.add(SearchItemModel(R.drawable.ic_shopping,"Samarqand Darvoza","Uzbekistan, Tashkent, Khushnavo Street, 2"))
        items.add(SearchItemModel(R.drawable.ic_saerch_place,"M-mavze, 5A","Uzbekistan, Tashkent, Chilanzar dist, Chilonzor dahasi"))
        items.add(SearchItemModel(R.drawable.ic_tree_black_silhouette_shape,"Eco Park","Uzbekistan, Tashkent, Mirobod dist, 87"))
        items.add(SearchItemModel(R.drawable.ic_saerch_place,"M-mavze, 5A","Uzbekistan, Tashkent, Chilanzar dist, Chilonzor dahasi"))
        items.add(SearchItemModel(R.drawable.ic_home,"Home","Uzbekistan, Tashkent, Yunusabad dahasi, 16-mavze, 14 dom, entrance 3"))
        items.add(SearchItemModel(R.drawable.ic_cup_of_drink,"Uch Baqaloq","Uzbekistan, Ahmad Donish street, 26 A"))
        items.add(SearchItemModel(R.drawable.ic_saerch_place,"Nukus 32","Uzbekistan, Tashkent, Nukus street, 32"))
        items.add(SearchItemModel(R.drawable.ic_saerch_place,"M-mavze, 5A","Uzbekistan, Tashkent, Chilanzar dist, Chilonzor dahasi"))
        items.add(SearchItemModel(R.drawable.ic_metro,"Bodomzor","Uzbekistan, Tashkent, Bodomzor street, Bodomzor bekati"))
        items.add(SearchItemModel(R.drawable.ic_college_graduation,"INHA University","Uzbekistan, Tashkent,Ziyolilar street, 28"))
        items.add(SearchItemModel(R.drawable.ic_cheeseburger,"Black Star Burger","Uzbekistan, Tashkent, Yunusobod dist, S1"))
        items.add(SearchItemModel(R.drawable.ic_saerch_place,"M-mavze, 5A","Uzbekistan, Tashkent, Chilanzar dist, Chilonzor dahasi"))

        recyclerViewSearchHistory.adapter = SearchAdapter(items,this)

    }

    override fun onSearchClicked() {
        (activity as MainActivity).onBottomSheetSearchItemClicked()
    }


}