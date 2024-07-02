package com.example.groupproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class MainActivity : AppCompatActivity() {

    private lateinit var overallProgressBar: ProgressBar
    lateinit var overallPercentageTV: TextView
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var loginBT: Button
    private lateinit var usernameET: EditText
    private lateinit var pwdET: EditText

    private lateinit var adView : AdView

    private var username1 : String = "user1"
    private var username2 : String = "user2"
    private var username3 : String = "user3"
    private var username4 : String = "user4"
    private var username5 : String = "user5"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        loginBT = findViewById(R.id.loginBT)
        usernameET = findViewById(R.id.usernameET)

        loginBT.setOnClickListener() {
            var u = usernameET.text.toString()
            if (u == username1 || u == username2 || u == username3 || u == username4 || u == username5) {
                val intent = Intent(
                    this@MainActivity,
                    UserCalendar::class.java
                )
                Username.username = usernameET.text.toString()
                startActivity(intent)
            }else {
                Toast.makeText(this, "Incorrect username", Toast.LENGTH_SHORT).show()
            }
        }

        sharedPreferences = getSharedPreferences("ToDoList", Context.MODE_PRIVATE)

        // Display Ad
        adView = findViewById(R.id.adViewLogin)
        var adBuilder = AdRequest.Builder()
        adBuilder.addKeyword("motivation").addKeyword("plan")
        var adRequest = adBuilder.build()
        adView.loadAd(adRequest)
    }
}