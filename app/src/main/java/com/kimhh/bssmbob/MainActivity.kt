package com.kimhh.bssmbob

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val lunchList: ArrayList<LunchData> = arrayListOf()
    private val yoil = arrayListOf("월","화","수","목","금")
    //svar mealList = arrayListOf("","","","","")
    val mealList : ArrayList<String> = arrayListOf("","","","","")
    val dateList: ArrayList<String> = ArrayList()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferences = applicationContext.getSharedPreferences("last_meal", Context.MODE_PRIVATE)
        editor = preferences.edit()
        GlobalScope.launch(Dispatchers.Main) {
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
        }

    val getMeal = CoroutineScope(Dispatchers.IO).async {
        Jsoup.connect(
            "https://open.neis.go.kr/hub/mealServiceDietInfo?KEY=ef5a0e25d9b24cce86082829ccb81947&Type=json&pIndex=1&pSize=100&ATPT_OFCDC_SC_CODE=C10&SD_SCHUL_CODE=7150658&MLSV_FROM_YMD=${getMonday()}&MLSV_TO_YMD=${getFriday()}"
        ).ignoreContentType(true).ignoreHttpErrors(true).get().body().text()

    }

    private suspend fun addMealList() {
        getMeal.await()?.let {
            JSONObject(it).apply {
                val meal_list =
                    (this.getJSONArray("mealServiceDietInfo")[1] as JSONObject).getJSONArray("row")
                for (meal in 0 until meal_list.length()) {
                    val bob = (meal_list[meal] as JSONObject).getString("DDISH_NM").trim()
                        .replace("\\([^)]*\\)".toRegex(), " ").replace("\\s+".toRegex(), "\n")
                        .trim()
                    val time =
                        (meal_list[meal] as JSONObject).getString("MMEAL_SC_NM")
                    val d = (meal_list[meal] as JSONObject).getString("MLSV_YMD")
                    val di = dateList.indexOf(d)
                    mealList.set(di, mealList.get(di) + "${time}\n\n${bob}\n\n")
                }
            }
        }
    }
    private fun timeParse(){
        for( i in 0 until dateList.size){
            var M = dateList.get(i).substring(4,6).toInt()
            var d = dateList.get(i).substring(6,8).toInt()
            dateList.set(i,"${M}월 ${d}일 (${yoil[i]})")
        }
    }
    private fun addLunchData(){
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
    }

    private suspend fun mealParse() {
        getWeek()
        addMealList()
        timeParse()
        addLunchData()
        val fom = DateTimeFormatter.ofPattern("yyyyMMdd")
        val ind = dateList.indexOf((LocalDate.now().format(fom)))
        try {
                runOnUiThread {
                    pager.adapter = MainAdapter(lunchList)
                    if (ind < 0) {
                        pager.setCurrentItem(0, true)
                    } else {
                        pager.setCurrentItem(ind, true)
                    }
                }
            } catch (e: Exception) {
                Log.e("error", e.toString())
                runOnUiThread {
                }
            }
    }
    private fun getFriday(): String {
        val formatter = java.text.SimpleDateFormat("yyyyMMdd")
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        return formatter.format(c.time)
    }

    private fun getMonday(): String {
        val formatter = java.text.SimpleDateFormat("yyyyMMdd")
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return formatter.format(c.time)
    }

    private fun getWeek() {
        val formatter = java.text.SimpleDateFormat("yyyyMMdd")
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        for (i in 0..4) {
            c.add(Calendar.DATE, if (i == 0) 0 else 1).toString()
            dateList.add(formatter.format(c.time))
        }
    }
}
