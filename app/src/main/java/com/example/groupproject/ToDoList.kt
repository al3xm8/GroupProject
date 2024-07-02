package com.example.groupproject

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ToDoList : AppCompatActivity() {

    private lateinit var dateTV: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var addBT: FloatingActionButton
    private lateinit var backBT: Button

    private lateinit var progressBar: ProgressBar
    private lateinit var percentageTV: TextView

    private val toDoItems = ArrayList<ToDoItem>()
    private lateinit var adapter: ToDoAdapter

    private var totalItems: Int = 0
    private var checkedItems: Int = 0

    private lateinit var sharedPreferences: SharedPreferences

    private var un = Username.username

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todolist)

        dateTV = findViewById(R.id.dateTV)
        recyclerView = findViewById(R.id.recyclerView)
        addBT = findViewById(R.id.addBT)
        backBT = findViewById(R.id.backBT)

        progressBar = findViewById(R.id.progressBar)
        percentageTV = findViewById(R.id.percentageTV)

        sharedPreferences = getSharedPreferences("ToDoList", Context.MODE_PRIVATE)

        backBT.setOnClickListener {
            finish()
        }

        dateTV.text = getDate()

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ToDoAdapter(toDoItems, this, { saveTasks(getDate(), it) }, { updateProgress() }, { deleteFromFirebase(getDate(), it) })
        recyclerView.adapter = adapter

        loadTasks(getDate())

        addBT.setOnClickListener {
            showInputDialog(getDate())
        }

        updateProgress()
    }

    private fun getDate(): String {
        val year = intent.getIntExtra("year", 0)
        val month = intent.getIntExtra("month", 0)
        val day = intent.getIntExtra("day", 0)

        val cal = Calendar.getInstance()
        cal.set(year, month, day)

        val dayMonthYear = SimpleDateFormat("dd MMMM yyyy", Locale.US)
        val dateString = dayMonthYear.format(cal.time)
        return dateString
    }

    private fun saveTasks(date: String, tasks: ArrayList<ToDoItem>) {
        val editor = sharedPreferences.edit()
        val gson = Gson()

        // Save current date's tasks
        val json = gson.toJson(tasks)
        editor.putString(un + date, json)
        editor.apply()

        // Save all tasks across dates
        val allTasksJson = sharedPreferences.getString(un + "allTasks", null)
        val type = object : TypeToken<MutableMap<String, ArrayList<ToDoItem>>>() {}.type
        val allTasks: MutableMap<String, ArrayList<ToDoItem>> =
            if (allTasksJson != null) gson.fromJson(allTasksJson, type) else mutableMapOf()

        allTasks[date] = tasks
        editor.putString(un + "allTasks", gson.toJson(allTasks))
        editor.apply()

        // Update overall progress in SharedPreferences
        updateOverallProgressInSharedPreferences()
    }

    private fun loadTasks(date: String) {
        val gson = Gson()
        val json = sharedPreferences.getString(un + date, null)
        val type = object : TypeToken<ArrayList<ToDoItem>>() {}.type
        if (json != null) {
            val items: ArrayList<ToDoItem> = gson.fromJson(json, type)
            toDoItems.clear()
            toDoItems.addAll(items)
            adapter.notifyDataSetChanged()
            updateProgress()  // Ensure progress is updated after loading tasks
        }

        // Load special tasks from Firebase
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("specialEvents").child(date)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val item = snapshot.getValue(ToDoItem::class.java)
                    if (item != null) {
                        // Check if the task already exists in the toDoItems list
                        val taskExists = toDoItems.any { it.title == item.title && it.username == item.username }
                        if (!taskExists) {
                            toDoItems.add(item)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
                updateProgress()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ToDoList", "Error in loading special tasks from Firebase: " + databaseError)
            }
        })
    }

    private fun showInputDialog(date: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.todo_input, null)

        val nameInputET = dialogLayout.findViewById<EditText>(R.id.nameInputET)
        val descriptionET = dialogLayout.findViewById<EditText>(R.id.descriptionET)
        val specialCB = dialogLayout.findViewById<CheckBox>(R.id.specialCB)
        builder.setView(dialogLayout)
            .setPositiveButton("Add") { _, _ ->
                val itemName = nameInputET.text.toString()
                val description = descriptionET.text.toString()

                if (itemName.isNotEmpty()) {
                    if (specialCB.isChecked) {
                        val firebaseDatabase = FirebaseDatabase.getInstance()
                        val databaseReference = firebaseDatabase.getReference("specialEvents").child(date)
                        val key = databaseReference.push().key ?: ""
                        val specialEvent = ToDoItem(itemName, description, username = un, isSpecial = true, key = key)
                        databaseReference.child(key).setValue(specialEvent)
                    }
                    addItem(itemName, description, date)
                    totalItems++
                    updateProgress()
                }
            }
            .setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun addItem(itemName: String, description: String, date: String, key: String = "") {
        val item = ToDoItem(itemName, description, username = un, isSpecial = key.isNotEmpty(), key = key)
        toDoItems.add(item)
        recyclerView.adapter?.notifyDataSetChanged()
        saveTasks(date, toDoItems)
        updateProgress()  // Update progress after adding a new item
    }

    private fun updateProgress() {
        totalItems = toDoItems.size
        checkedItems = getCheckedItems()
        val percentage = if (totalItems > 0) ((checkedItems.toFloat() / totalItems) * 100).toInt() else 0
        percentageTV.text = "$percentage%"
        progressBar.progress = percentage
    }

    private fun getCheckedItems(): Int {
        return toDoItems.count { it.completed }
    }

    private fun updateOverallProgressInSharedPreferences() {
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

            val editor = sharedPreferences.edit()
            editor.putInt("overallProgress", overallPercentage)
            editor.apply()
        }
    }

    private fun deleteFromFirebase(date: String, item: ToDoItem) {
        Log.d("ToDoList", "Attempting to delete item from Firebase with key: ${item.key}")
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("specialEvents").child(date)
        reference.child(item.key).removeValue()
    }
}
