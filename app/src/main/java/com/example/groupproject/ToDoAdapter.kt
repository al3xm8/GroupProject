package com.example.groupproject

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ToDoAdapter(
    private val toDoItems: ArrayList<ToDoItem>,
    private val context: Context,
    private val saveTasks: (ArrayList<ToDoItem>) -> Unit,
    private val updateProgress: () -> Unit
) : RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder>() {

    companion object {
        var checkedItems: Int = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        return ToDoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToDoViewHolder, pos: Int) {
        val toDoItem = toDoItems[pos]
        holder.itemNameTV.text = toDoItem.title
        holder.descriptionTV.text = toDoItem.description
        holder.checkBox.isChecked = toDoItem.completed

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            playSound(holder.itemView.context)
            if (isChecked) {
                checkedItems++
            } else {
                checkedItems--
            }
            toDoItem.completed = isChecked
            saveTasks(toDoItems)  // Save tasks whenever checkbox state changes
            updateProgress()  // Update progress whenever checkbox state changes
        }

        holder.checkBox.setOnClickListener {
            checkedItems++
        }
    }

    override fun getItemCount(): Int {
        return toDoItems.size
    }

    fun addItem(item: ToDoItem) {
        toDoItems.add(item)
        notifyItemInserted(toDoItems.size - 1)
    }

    fun removeItem(idx: Int) {
        toDoItems.removeAt(idx)
        notifyItemRemoved(idx)
    }

    private fun playSound(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.checkbox_sound)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    }

    inner class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTV: TextView = itemView.findViewById(R.id.itemNameTV)
        val descriptionTV: TextView = itemView.findViewById(R.id.descriptionTV)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }
}
