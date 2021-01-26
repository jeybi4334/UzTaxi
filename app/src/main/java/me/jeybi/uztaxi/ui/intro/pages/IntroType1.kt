package me.jeybi.uztaxi.ui.intro.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.intro_1.*
import me.jeybi.uztaxi.R

class IntroType1(val position : Int) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.intro_1,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(position){
            1->{
                imageViewIntro.setImageResource(R.drawable.page_2)
                textViewIntroTitle.text = "Примите лучшие варианты"
                textViewIntroContent.text = "Широкий выбор тарифов и водителей позволит вам подобрать оптимальную поездку для вашего пункта назначения."
            }
            2->{
                imageViewIntro.setImageResource(R.drawable.page_3)
                textViewIntroTitle.text = "Лучшие водители в вашем регионе"
                textViewIntroContent.text = "Мы нанимаем каждого водителя, проходя специальное собеседование, и выбираем его для вашей поездки на основе их поведения."
            }
            3->{
                imageViewIntro.setImageResource(R.drawable.page_4)
                textViewIntroTitle.text = "Любой метод оплаты"
                textViewIntroContent.text = "Вы можете добавить свою карту UzCard или HumoCard, чтобы оплатить поездку и получать кэшбэк в реальном времени с каждой успешной поездки."
            }
        }
    }

}