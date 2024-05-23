package com.learning.arcgiscodezero.test.MainActivity24

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.learning.arcgiscodezero.R

class OptionsListActivity : AppCompatActivity(), OptionsListAdapter.OnItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options_list)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val wordList = intent.getStringArrayListExtra("optionList") ?: ArrayList()

        val adapter = OptionsListAdapter(wordList, this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(option: String) {
        val tag = intent.getStringExtra("tag") // Retrieve the tag from the intent
        val resultsIntent = Intent().apply {
            putExtra("selectedOption", option)
            putExtra("tag", tag) // Pass the tag along with the selected option
        }
        setResult(Activity.RESULT_OK, resultsIntent)
        finish()
    }

}