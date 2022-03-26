package com.kimhh.bssmbob

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val lunchList: ArrayList<LunchData> = arrayListOf()
    private val lunchList2: ArrayList<LunchData> = arrayListOf()
    private val lunchList3: ArrayList<LunchData> = arrayListOf()
                                                    //월 노랑   //화 핑  //수 초   //목 주  //금 하늘
    private val colorList = arrayListOf<String>("#FFB300","#9575CD","#009688","#FF7043","#78909C")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferences = applicationContext.getSharedPreferences("last_meal", Context.MODE_PRIVATE)
        editor = preferences.edit()
        mealParse()
        mealParse2()
        mealParse3()

        pager.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }
        pager2.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }
        pager3.apply {
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
        pager2.setPageTransformer(compositePageTransformer)
        pager3.setPageTransformer(compositePageTransformer)
    }

    fun mealParse() {
        val parsing = GlobalScope.launch(Dispatchers.IO) {
            try {
                val request = Jsoup.connect(
                    "https://open.neis.go.kr/hub/mealServiceDietInfo?Type=json&pIndex=1&pSize=1" +
                            "&ATPT_OFCDC_SC_CODE=C10&SD_SCHUL_CODE=7150658&MMEAL_SC_CODE=1&MLSV_FROM_YMD=${getMonday()}&MLSV_TO_YMD=${getFriday()}"
                ).ignoreContentType(true).ignoreHttpErrors(true).get()

                val jObject = JSONObject(request.body().text()).apply {
                    val meal_list = (this.getJSONArray("mealServiceDietInfo")[1] as JSONObject).getJSONArray("row")
                    for (meal in 0 until meal_list.length()) {
                        lunchList.add(LunchData("조식 ${(meal_list[meal] as JSONObject).getString("CAL_INFO")}",
                            (meal_list[meal] as JSONObject).getString("DDISH_NM"),
                            background = colorList[meal]))
                    }
                }

                runOnUiThread {
                    pager.adapter = MainAdapter(lunchList)
                    val cal = Calendar.getInstance();
                    val num = cal.get(Calendar.DAY_OF_WEEK);
                    pager.setCurrentItem(num, true)
                }
            } catch (e: Exception) {
//                runOnUiThread {
//                    kcal.text = preferences.getString("kcal", "급식")
//                }
            }
        }
    }

    fun mealParse2() {
        val parsing2 = GlobalScope.launch(Dispatchers.IO) {
            try {
                val request2 = Jsoup.connect(
                    "https://open.neis.go.kr/hub/mealServiceDietInfo?Type=json&pIndex=1&pSize=1" +
                            "&ATPT_OFCDC_SC_CODE=C10&SD_SCHUL_CODE=7150658&MMEAL_SC_CODE=2&MLSV_FROM_YMD=${getMonday()}&MLSV_TO_YMD=${getFriday()}"
                ).ignoreContentType(true).ignoreHttpErrors(true).get()

                val jObject2 = JSONObject(request2.body().text()).apply {
                    val meal_list2 = (this.getJSONArray("mealServiceDietInfo")[1] as JSONObject).getJSONArray("row")
                    for (meal in 0 until meal_list2.length()) {
                        lunchList2.add(LunchData("중식 ${(meal_list2[meal] as JSONObject).getString("CAL_INFO")}",
                            (meal_list2[meal] as JSONObject).getString("DDISH_NM"),
                            background = colorList[meal]))
                    }
                }

                runOnUiThread {
                    pager2.adapter = MainAdapter(lunchList2)
                    val cal = Calendar.getInstance();
                    val num = cal.get(Calendar.DAY_OF_WEEK);
                    pager2.setCurrentItem(num, true)
                }
            } catch (e: Exception) {
//                runOnUiThread {
//                    kcal.text = preferences.getString("kcal", "급식")
//                }
            }
        }
    }
    fun mealParse3() {
        val parsing3 = GlobalScope.launch(Dispatchers.IO) {
            try {
                val request3 = Jsoup.connect(
                    "https://open.neis.go.kr/hub/mealServiceDietInfo?Type=json&pIndex=1&pSize=1" +
                            "&ATPT_OFCDC_SC_CODE=C10&SD_SCHUL_CODE=7150658&MMEAL_SC_CODE=3&MLSV_FROM_YMD=${getMonday()}&MLSV_TO_YMD=${getFriday()}"
                ).ignoreContentType(true).ignoreHttpErrors(true).get()

                val jObject3 = JSONObject(request3.body().text()).apply {
                    val meal_list3 = (this.getJSONArray("mealServiceDietInfo")[1] as JSONObject).getJSONArray("row")
                    for (meal in 0 until meal_list3.length()) {
                        lunchList3.add(LunchData("석식 ${(meal_list3[meal] as JSONObject).getString("CAL_INFO")}",
                            (meal_list3[meal] as JSONObject).getString("DDISH_NM"),
                            background = colorList[meal]))
                    }
                }

                runOnUiThread {
                    pager3.adapter = MainAdapter(lunchList3)
                    val cal = Calendar.getInstance();
                    val num = cal.get(Calendar.DAY_OF_WEEK);
                    pager3.setCurrentItem(num, true)
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

        c.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY)

        return formatter.format(c.time)

    }

    fun getFriday(): String{

        val formatter = java.text.SimpleDateFormat("yyyyMMdd")

        val c = Calendar.getInstance()

        c.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY)

        return formatter.format(c.time)

    }
}