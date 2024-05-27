package com.learning.arcgiscodezero.test.MainActivity24

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arcgismaps.data.Feature
import com.arcgismaps.data.ServiceFeatureTable
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.learning.arcgiscodezero.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.util.Xml
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

class EditFeatureActivity : AppCompatActivity() {
    private lateinit var customTextInputEditText: CustomTextInputEditText
    private lateinit var customDatePickerEditText: CustomDatePickerEditText

    private lateinit var customInputTypeObject: CustomTextInputEditText
    private lateinit var customInputSpecies: CustomTextInputEditText

    private lateinit var saveButton: Button

    private lateinit var customMap: MutableMap<String, CustomTextInputEditText>

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

        customMap = mutableMapOf()

        customInputTypeObject = findViewById(R.id.editText_tip)
        customInputTypeObject.setTag("tip")
        var options = ArrayList((resources.getStringArray(R.array.domain_type)).asList())
        customInputTypeObject.setOptions(options)
        customInputTypeObject.setActivityContext(this)

        customMap.put(customInputTypeObject.getTag()!!, customInputTypeObject)

        customInputSpecies = findViewById(R.id.editText_vrsta)
        customInputSpecies.setTag("vrsta")
        options = ArrayList((resources.getStringArray(R.array.domain_species)).asList())
        customInputSpecies.setOptions(options)
        customInputSpecies.setActivityContext(this)

        customMap.put(customInputSpecies.getTag()!!, customInputSpecies)

        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)

        // Example data
        val fields = listOf(
            CustomField("Tip", "text"),
            CustomField("Number", "number"),
            CustomField("Custom Tip", "customText"),
            CustomField("Pick a date", "datePicker")
        )

        // Dynamically add fields
        for (field in fields) {
            val view = createView(field)
            linearLayout.addView(view)
        }


//        for (field in Repository.fields) {
//            val editText = EditText(this)
//            editText.hint = field?.name
//            linearLayout.addView(editText)
//        }

        saveButton = findViewById(R.id.button_save)
    }

    private fun createViewFromXml(field: CustomField): View {
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)

        val xmlLayout = """
    <com.google.android.material.textfield.TextInputLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        app:boxStrokeColor="#CCCCCC"
        app:boxBackgroundColor="#FFFFFF"
        app:boxBackgroundMode="outline"
        app:boxCornerRadiusTopStart="8dp"
        app:boxCornerRadiusTopEnd="8dp"
        app:boxCornerRadiusBottomStart="8dp"
        app:boxCornerRadiusBottomEnd="8dp">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/editText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Hint"
            android:padding="16dp"
            android:textSize="16sp"
            android:drawablePadding="12dp"
            android:inputType="text" />
    </com.google.android.material.textfield.TextInputLayout>
""".trimIndent()

        val xml = xmlLayout.replace("android:hint=\"Hint\"", "android:hint=\"${field.name}\"")
            .replace("android:inputType=\"text\"", "android:inputType=\"${getInputType(field.type)}\"")

        val parser: XmlPullParser = Xml.newPullParser()
        parser.setInput(StringReader(xml))

        return LayoutInflater.from(this).inflate(parser, linearLayout, false)
    }

    private fun getInputType(type: String): String {
        return when (type) {
            "text" -> "text"
            "number" -> "number"
            "customText" -> "text"  // Custom handling may be required
            "datePicker" -> "none"
            else -> throw IllegalArgumentException("Unsupported field type")
        }
    }

    private fun createView(field: CustomField): TextInputLayout {
        val context = this
        val textInputLayout = TextInputLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
                boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
                boxStrokeColor = Color.argb(255, 204, 204, 204)
                boxBackgroundColor = Color.WHITE
                setBoxCornerRadii(8f, 8f, 8f, 8f)
            }
        }

        val testOptions = arrayListOf("Option 1", "Option 2", "Option 3")

        val editText = when (field.type) {
            "text" -> TextInputEditText(context).apply {
                hint = field.name
                setPadding(16, 16, 16, 16)
                textSize = 16f
            }
            "number" -> TextInputEditText(context).apply {
                hint = field.name
                setPadding(16, 16, 16, 16)
                textSize = 16f
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
            }
            "customText" -> CustomTextInputEditText(context).apply {
                hint = field.name
                setPadding(16, 16, 16, 16)
                textSize = 16f
                setOptions(testOptions)
                setActivityContext(context)
                setTag(field.name)
                customMap[field.name] = this
            }
            "datePicker" -> CustomDatePickerEditText(context).apply {
                hint = field.name
                inputType = android.text.InputType.TYPE_NULL
            }
            else -> throw IllegalArgumentException("Unsupported field type")
        }

        textInputLayout.addView(editText)
        return textInputLayout
    }


    data class CustomField(val name: String, val type: String)

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
            if (customMap.containsKey(tag))  {
                val editText = customMap[tag]
//            val editText = when (tag) {
//                "tippp" -> customTextInputEditText
//                "tip" -> customInputTypeObject
//                "vrsta" -> customInputSpecies
//                else -> null
//            }
            editText?.setText(selectedOption)
            }
        }
        clearFocusFromAllFields()
    }

    private fun clearFocusFromAllFields() {
        val rootView = findViewById<View>(android.R.id.content)
        rootView.clearFocus()
    }
}
