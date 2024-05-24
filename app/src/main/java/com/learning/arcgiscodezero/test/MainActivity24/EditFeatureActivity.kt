package com.learning.arcgiscodezero.test.MainActivity24

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arcgismaps.data.Feature
import com.arcgismaps.data.ServiceFeatureTable
import com.google.android.material.snackbar.Snackbar
import com.learning.arcgiscodezero.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditFeatureActivity : AppCompatActivity() {
    private lateinit var customTextInputEditText: CustomTextInputEditText
    private lateinit var customDatePickerEditText: CustomDatePickerEditText

    private lateinit var customInputTypeObject: CustomTextInputEditText
    private lateinit var customInputSpecies: CustomTextInputEditText

    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_feature)

        initView()
        initListeners()

    }

    private fun initView() {
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

        saveButton = findViewById(R.id.button_save)
    }

    private fun initListeners() {

        saveButton.setOnClickListener {
            if (customInputSpecies.text.toString() != "") {
                Repository.feature?.attributes?.set("vrsta", customInputSpecies.text.toString())
                lifecycleScope.launch {
                    Repository.feature?.let { feature -> updateFeature(feature) }
                }
            }
        }

    }

    private suspend fun updateFeature(feature: Feature) {
        val serviceFeatureTable = feature.featureTable as? ServiceFeatureTable
        if (serviceFeatureTable != null) {
            try {
                serviceFeatureTable.updateFeature(feature).apply {
                    onSuccess {
                        serviceFeatureTable.applyEdits()
                        finish()
                    }
                    onFailure { error ->
                        val rootView = findViewById<View>(android.R.id.content)
                        Snackbar.make(rootView, "Failed to update feature", Snackbar.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                val rootView = findViewById<View>(android.R.id.content)
                Snackbar.make(rootView, "An error occurred", Snackbar.LENGTH_SHORT).show()
            }
        } else {
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView, "Invalid feature table", Snackbar.LENGTH_SHORT).show()
        }
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
