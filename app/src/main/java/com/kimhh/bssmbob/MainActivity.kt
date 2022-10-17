package com.kimhh.bssmbob

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_pager.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.*
import kotlin.collections.ArrayList
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val lunchList: ArrayList<LunchData> = arrayListOf()

    //월 노랑   //화 핑    //수 초   //목 주   //금 하늘
    private val colorList =
        arrayListOf("#6E85B7", "#B2C8DF", "#C4D7E0", "#F8F9D7", "#FFFFFF")
    private val yoil = arrayListOf("월","화","수","목","금")
    var mealList: ArrayList<String> = ArrayList()
    val kcalList: ArrayList<Float> = ArrayList()
    val dateList: ArrayList<String> = ArrayList()
    //private val colorList = arrayListOf<String>("#FFB300","#9575CD","#009688","#FF7043","#78909C")

    override fun onCreate(savedInstanceState: Bundle?) {




        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferences = applicationContext.getSharedPreferences("last_meal", Context.MODE_PRIVATE)
        editor = preferences.edit()

        mealParse()

        pager.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }


        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer(ViewPager2.PageTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
        })

        pager.setPageTransformer(compositePageTransformer)


    }


    fun mealParse() {
        getWeek()
        for(i in 0..5){
            mealList.add("")
        }
        val parsing = GlobalScope.launch(Dispatchers.IO) {
            try {
                val req = Jsoup.connect(
                    "https://open.neis.go.kr/hub/mealServiceDietInfo?KEY=ef5a0e25d9b24cce86082829ccb81947&Type=json&pIndex=1&pSize=100" +
                            "&ATPT_OFCDC_SC_CODE=C10&SD_SCHUL_CODE=7150658&MLSV_FROM_YMD=${getMonday()}&MLSV_TO_YMD=${getFriday()}"
                ).ignoreContentType(true).ignoreHttpErrors(true).get()
                val jsonobj = JSONObject(req.body().text()).apply {
                    val meal_list =
                        (this.getJSONArray("mealServiceDietInfo")[1] as JSONObject).getJSONArray("row")

                    for (meal in 0 until meal_list.length()) {
                        val test = (meal_list[meal] as JSONObject).getString("DDISH_NM").trim()
                            .replace("\\([^)]*\\)".toRegex(), " ").replace("\\s+".toRegex(), "\n").trim()
                        val test2 = (meal_list[meal] as JSONObject).getString("CAL_INFO")
                            .split(" ")[0].toFloat()
                        val test3 =
                            (meal_list[meal] as JSONObject).getString("MMEAL_SC_NM")
                        val d = (meal_list[meal] as JSONObject).getString("MLSV_YMD")
//                        lunchList.add(LunchData("조식 ${(meal_list[meal] as JSONObject).getString("CAL_INFO")}",
//                            test, background = colorList[meal]))

                        val di = dateList.indexOf(d)
                       when (test3){
                          "조식" -> mealList.set(di, mealList.get(di) + "${test3}\n\n${test}\n\n")
                           else -> mealList.set(di, mealList.get(di) + "${test3}\n\n${test}\n\n")
                       }
                        var a = 1

                        //lunchList.add(LunchData((kcalList.get(meal).toString()),mealList.get(meal),background = colorList[meal]))
                    }

                }
                val cal: LocalDate = LocalDate.now()
                val fom = DateTimeFormatter.ofPattern("yyyyMMdd")
                val fmd = cal.format(fom)
                val ind = dateList.indexOf((fmd))
                for( i in 0 until dateList.size){
                    var M = dateList.get(i).substring(4,6).toInt()
                    var d = dateList.get(i).substring(6,8).toInt()
                    dateList.set(i,"${M.toString()}월 ${d.toString()}일 (${yoil[i]})")
                }
                val TAG = "asdf"
                for (i in 0 until dateList.size) {
                    lunchList.add(
                        i,
                        LunchData(
                            dateList.get(i),
                            if (mealList.get(i) == "") "급식이 없습니다" else mealList.get(i),
                            background = "#3676e8"
                        )
                    )
                }

                runOnUiThread {
                    pager.adapter = MainAdapter(lunchList)

                    if (ind < 0) {
                        pager.setCurrentItem(0, true)
                    } else {
                        pager.setCurrentItem(ind, true)
                    }
                }
            } catch (e: Exception) {
//                runOnUiThread {
//                    kcal.text = preferences.getString("kcal", "급식")
//                }
                Log.e("error", e.toString())
            }
        }
    }


    fun getFriday(): String {

        val formatter = java.text.SimpleDateFormat("yyyyMMdd")

        val c = Calendar.getInstance()

        c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)

        return formatter.format(c.time)


    }

    fun getMonday(): String {

        val formatter = java.text.SimpleDateFormat("yyyyMMdd")

        val c = Calendar.getInstance()

        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        return formatter.format(c.time)

    }

    fun getWeek(): Unit {

        val formatter = java.text.SimpleDateFormat("yyyyMMdd")
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        for (i in 0..4) {
            c.add(Calendar.DATE, if (i == 0) 0 else 1).toString()
            dateList.add(formatter.format(c.time))
        }

    }

}