package com.example.groupproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserCalendar : AppCompatActivity() {

    private lateinit var overallProgressBar: ProgressBar
    private lateinit var overallPercentageTV: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var calendarView: CalendarView

    private lateinit var adView : AdView

    var un = Username.username

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        overallProgressBar = findViewById(R.id.overallProgressBar)
        overallPercentageTV = findViewById(R.id.overallPercentageTV)
        sharedPreferences = getSharedPreferences("ToDoList", Context.MODE_PRIVATE)

        Log.i("AlexM", Username.username)

        calendarView = findViewById(R.id.calendarView)
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Create an intent to start the ToDoList activity
            val intent = Intent(this@UserCalendar, ToDoList::class.java)
            // Pass the selected date to the ToDoList activity
            intent.putExtra("year", year)
            intent.putExtra("month", month)
            intent.putExtra("day", dayOfMonth)
            startActivity(intent)
        }

        updateOverallProgress()

        adView = findViewById(R.id.adViewCalendar)
        var adBuilder = AdRequest.Builder()

        adBuilder.addKeyword("motivation").addKeyword("plan")
        var adRequest = adBuilder.build()

        adView.loadAd(adRequest)

    }

    public fun updateOverallProgress() {
        val gson = Gson()

        val allTasksJson = sharedPreferences.getString(un + "allTasks", null)
        val type = object : TypeToken<Map<String, ArrayList<ToDoItem>>>() {}.type

        if (allTasksJson != null) {
            val allTasks: Map<String, ArrayList<ToDoItem>> = gson.fromJson(allTasksJson, type)
            var totalTasks = 0
            var completedTasks = 0

            for (tasks in allTasks.values) {
                totalTasks += tasks.size
                completedTasks += tasks.count { it.completed }
            }

            val overallPercentage = if (totalTasks > 0) ((completedTasks.toFloat() / totalTasks) * 100).toInt() else 0
            overallPercentageTV.text = "$overallPercentage%"
            overallProgressBar.progress = overallPercentage
        }
    }

    override fun onResume() {
        super.onResume()
        updateOverallProgress()
    }
}
