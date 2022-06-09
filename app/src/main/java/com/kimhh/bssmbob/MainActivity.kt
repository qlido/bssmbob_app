package com.kimhh.bssmbob

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
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
    private val colorList = arrayListOf<String>("#FFB300","#9575CD","#009688","#FF7043","#78909C")
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
        val parsing = GlobalScope.launch(Dispatchers.IO) {
            try {
                val request = Jsoup.connect(
                    "https://open.neis.go.kr/hub/mealServiceDietInfo?Type=json&pIndex=1&pSize=1" +
                            "&ATPT_OFCDC_SC_CODE=C10&SD_SCHUL_CODE=7150658&MMEAL_SC_CODE=1&MLSV_FROM_YMD=${getMonday()}&MLSV_TO_YMD=${getFriday()}"
                ).ignoreContentType(true).ignoreHttpErrors(true).get()


                val request2 = Jsoup.connect(
                    "https://open.neis.go.kr/hub/mealServiceDietInfo?Type=json&pIndex=1&pSize=1" +
                            "&ATPT_OFCDC_SC_CODE=C10&SD_SCHUL_CODE=7150658&MMEAL_SC_CODE=2&MLSV_FROM_YMD=${getMonday()}&MLSV_TO_YMD=${getFriday()}"
                ).ignoreContentType(true).ignoreHttpErrors(true).get()

                val request3 = Jsoup.connect(
                    "https://open.neis.go.kr/hub/mealServiceDietInfo?Type=json&pIndex=1&pSize=1" +
                            "&ATPT_OFCDC_SC_CODE=C10&SD_SCHUL_CODE=7150658&MMEAL_SC_CODE=3&MLSV_FROM_YMD=${getMonday()}&MLSV_TO_YMD=${getFriday()}"
                ).ignoreContentType(true).ignoreHttpErrors(true).get()

                val jObject = JSONObject(request.body().text()).apply {
                    val meal_list = (this.getJSONArray("mealServiceDietInfo")[1] as JSONObject).getJSONArray("row")

                    for (meal in 0 until meal_list.length()) {
                        val test =  (meal_list[meal] as JSONObject).getString("DDISH_NM").trim().replace("\\([^)]*\\)".toRegex()," ").replace("\\s+".toRegex(),"\n")
                        val test2 = (meal_list[meal] as JSONObject).getString("CAL_INFO").split(" ")[0].toFloat()
//                        lunchList.add(LunchData("조식 ${(meal_list[meal] as JSONObject).getString("CAL_INFO")}",
//                            test, background = colorList[meal]))
                        val d = (meal_list[meal] as JSONObject).getString("MLSV_YMD")
                        kcalList.add(meal,test2)
                        mealList.add(meal, "조식 \n\n ${test}")
                        dateList.add(d)

                        //lunchList.add(LunchData((kcalList.get(meal).toString()),mealList.get(meal),background = colorList[meal]))
                    }
                }
                val jObject2 = JSONObject(request2.body().text()).apply {
                    val meal_list2 = (this.getJSONArray("mealServiceDietInfo")[1] as JSONObject).getJSONArray("row")
                    for (meal in 0 until meal_list2.length()) {

                        val test =  (meal_list2[meal] as JSONObject).getString("DDISH_NM").trim().replace("\\([^)]*\\)".toRegex()," ").replace("\\s+".toRegex(),"\n")
                        val test2 = (meal_list2[meal] as JSONObject).getString("CAL_INFO").split(" ")[0].toFloat()
//                        lunchList.add(LunchData("조식 ${(meal_list[meal] as JSONObject).getString("CAL_INFO")}",
//                            test, background = colorList[meal]))

                        kcalList.add(meal,test2)
                        mealList.set(meal, mealList.get(meal)+"\n\n중식 \n\n${test}")

                        // lunchList.add(meal,LunchData((kcalList.get(meal).toString()),mealList.get(meal),background = colorList[meal]))
                    }
                }
                val jObject3 = JSONObject(request3.body().text()).apply {
                    val meal_list3 = (this.getJSONArray("mealServiceDietInfo")[1] as JSONObject).getJSONArray("row")
                    for (meal in 0 until meal_list3.length()) {
                        val test =  (meal_list3[meal] as JSONObject).getString("DDISH_NM").trim().replace("\\([^)]*\\)".toRegex()," ").replace("\\s+".toRegex(),"\n")
                        val test2 = (meal_list3[meal] as JSONObject).getString("CAL_INFO").split(" ")[0].toFloat()
                        val d = (meal_list3[meal] as JSONObject).getString("MLSV_YMD")

                        //lunchList.add(LunchData("조식 ${(meal_list[meal] as JSONObject).getString("CAL_INFO")}",
                        //  (meal_list[meal] as JSONObject).getString("DDISH_NM"), background = colorList[meal]))

                        val di = dateList.indexOf(d)
                        kcalList.add(di, test2)
                        mealList.set(di, mealList.get(di) + "\n\n석식 \n\n${test}")



    
                    }
                    lunchList3.add(LunchData("석식 50000kcal",
                        "행복한 집으로 가셨군요\n집에서 맛난 국밥 드세요",
                        background = "#ffc0cb"))
                }
                for(i in 0 until dateList.size) {
                    lunchList.add(
                        i,
                        LunchData(dateList.get(i), mealList.get(i), background = colorList[i])
                    )
                }
                runOnUiThread {
-
                    pager.adapter = MainAdapter(lunchList)
                    val cal: LocalDate = LocalDate.now()
                    val fom = DateTimeFormatter.ofPattern("yyyyMMdd")
                    val fmd = cal.format(fom)
                    val ind = dateList.indexOf((fmd))
                    if(ind < 0){
                        pager.setCurrentItem( 0, true)
                    }else{
            pager.setCurrentItem(ind,true)
                   }
                }
            } catch (e: Exception) {
//                runOnUiThread {
//                    kcal.text = preferences.getString("kcal", "급식")
//                }
            }
        }
    }


    fun getMonday(): String{

        val formatter = java.text.SimpleDateFormat("yyyyMMdd")

        val c = Calendar.getInstance()

        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        return formatter.format(c.time)

    }


    fun getFriday(): String {

        val formatter = java.text.SimpleDateFormat("yyyyMMdd")

        val c = Calendar.getInstance()

        c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)

        return formatter.format(c.time)


    }

}