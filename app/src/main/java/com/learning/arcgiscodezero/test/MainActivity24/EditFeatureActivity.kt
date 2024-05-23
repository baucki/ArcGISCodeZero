package com.learning.arcgiscodezero.test.MainActivity24

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.learning.arcgiscodezero.R

class EditFeatureActivity : AppCompatActivity() {
    private lateinit var customTextInputEditText: CustomTextInputEditText
    private lateinit var customDatePickerEditText: CustomDatePickerEditText

    private lateinit var customInputTypeObject: CustomTextInputEditText
    private lateinit var customInputSpecies: CustomTextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_feature)

        customTextInputEditText = findViewById(R.id.editText_tippp)
        customTextInputEditText.setTag("tippp")
        val testOptions = arrayListOf("Option 1", "Option 2", "Option 3")
        customTextInputEditText.setOptions(testOptions)
        customTextInputEditText.setActivityContext(this)
        customDatePickerEditText = findViewById(R.id.datePickerEditText)

        customInputTypeObject = findViewById(R.id.editText_tip)
        customInputTypeObject.setTag("tip")
        var options = ArrayList((resources.getStringArray(R.array.domain_type)).asList())
        customInputTypeObject.setOptions(options)
        customInputTypeObject.setActivityContext(this)

        customInputSpecies = findViewById(R.id.editText_vrsta)
        customInputSpecies.setTag("vrsta")
        options = ArrayList((resources.getStringArray(R.array.domain_species)).asList())
        customInputSpecies.setOptions(options)
        customInputSpecies.setActivityContext(this)

        // Set click listeners for the CustomTextInputEditText fields
        customInputTypeObject.setOnClickListener { startOptionsListActivity("tippp") }
        customInputTypeObject.setOnClickListener { startOptionsListActivity("tip") }
        customInputSpecies.setOnClickListener { startOptionsListActivity("vrsta") }
    }

    private fun startOptionsListActivity(tag: String) {
        val intent = Intent(this, OptionsListActivity::class.java)
        // Pass the tag to OptionsListActivity
        intent.putExtra("tag", tag)
        startActivityForResult(intent, CustomTextInputEditText.OPTIONS_LIST_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CustomTextInputEditText.OPTIONS_LIST_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedOption = data?.getStringExtra("selectedOption")
            val tag = data?.getStringExtra("tag")
            val editText = when (tag) {
                "tippp" -> customTextInputEditText
                "tip" -> customInputTypeObject
                "vrsta" -> customInputSpecies
                else -> null
            }
            editText?.setText(selectedOption)
        }
        clearFocusFromAllFields()
    }

    private fun clearFocusFromAllFields() {
        val rootView = findViewById<View>(android.R.id.content)
        rootView.clearFocus()
    }
}
