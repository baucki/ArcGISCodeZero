package com.learning.arcgiscodezero.test.MainActivity24

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
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
import androidx.core.view.marginTop
import com.arcgismaps.data.CodedValueDomain
import com.arcgismaps.data.InheritedDomain
import com.arcgismaps.data.RangeDomain
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

class EditFeatureActivity : AppCompatActivity() {
    private lateinit var textInput: TextInputEditText
    private lateinit var numberInput: TextInputEditText
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
        textInput = findViewById(R.id.editText_tipp)
        numberInput = findViewById(R.id.editText_number)
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

        customMap[customInputTypeObject.tag!!] = customInputTypeObject

        customInputSpecies = findViewById(R.id.editText_vrsta)
        customInputSpecies.setTag("vrsta")
        options = ArrayList((resources.getStringArray(R.array.domain_species)).asList())
        customInputSpecies.setOptions(options)
        customInputSpecies.setActivityContext(this)

        customMap[customInputSpecies.tag!!] = customInputSpecies

        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
//
//        val fields = listOf(
//            CustomField("Tip", "text"),
//            CustomField("Number", "number"),
//            CustomField("Custom Tip", "customText"),
//            CustomField("Pick a date", "datePicker")
//        )
//
//        for (field in fields) {
//            val view = createView(this, field)
//            linearLayout.addView(view)
//        }


        for (field in Repository.fields) {
//            val editText = EditText(this)
//            editText.hint = field?.name
//            linearLayout.addView(editText)
            when (Repository.fieldTypeMap[field?.fieldType]) {
                "Text" -> {
                    var codedValues = ""
                    codedValues = getCodedValues(field!!.name)
                    if (codedValues != "") {
                        Repository.codedValuesList = ArrayList(codedValues.split(","))
                        val view = createView(this, CustomField(field.alias, "customText"))
                        linearLayout.addView(view)
                    } else {
                        val view = createView(this, CustomField(field.alias, "text"))
                        linearLayout.addView(view)
                    }
                }
                "Short" -> {
                    val view = createView(this, CustomField(field!!.alias, "number"))
                    linearLayout.addView(view)
                }
                "Double" -> {
                    val view = createView(this, CustomField(field!!.alias, "decimalNumber"))
                    linearLayout.addView(view)
                }
                "Date" -> {
                    val view = createView(this, CustomField(field!!.alias, "datePicker"))
                    linearLayout.addView(view)
                }
            }
        }

        saveButton = findViewById(R.id.button_save)
    }

    private fun getCodedValues(fieldName: String): String {
        for (type in Repository.types) {
            val domains = type?.domains
            if (domains != null) {
                for (domain in domains) {
                    if (domain.key == fieldName) {
                        val domainDetails = when (domain.value) {
                            is CodedValueDomain -> {
                                val codedValues =
                                    (domain.value as CodedValueDomain).codedValues.joinToString { it.name }
                                codedValues
                            }

                            is InheritedDomain -> {
                                val codedValues =
                                    (domain.value as CodedValueDomain).codedValues.joinToString { it.name }
                                codedValues
                            }

                            is RangeDomain -> {
                                "Range: ${(domain.value as RangeDomain).minValue} - ${(domain.value as RangeDomain).maxValue}"
                            }

                            else -> ""
                        }
                        return domainDetails
                    }
                }
            }
        }
        return ""
    }

    private fun createView(context: Context, customField: CustomField): TextInputLayout {
        val textInputLayout = TextInputLayout(context).apply {
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            boxBackgroundColor = Color.WHITE
            boxStrokeColor = Color.argb(255,204,204,204)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(
                0,
                this.resources.getDimensionPixelSize(R.dimen.text_input_margin_top),
                0,
                0
            )
            this.layoutParams = layoutParams
            setBoxCornerRadii(
                context.resources.getDimensionPixelSize(R.dimen.box_corner_radius).toFloat(),
                context.resources.getDimensionPixelSize(R.dimen.box_corner_radius).toFloat(),
                context.resources.getDimensionPixelSize(R.dimen.box_corner_radius).toFloat(),
                context.resources.getDimensionPixelSize(R.dimen.box_corner_radius).toFloat()
            )
        }

        val dynamicInputField  = when(customField.type) {
            "text" -> {
                createTextInput(textInputLayout.context, customField.name)
            }
            "number" -> {
                createNumberInput(textInputLayout.context, customField.name)
            }
            "decimalNumber" -> {
                createDecimalNumberInput(textInputLayout.context, customField.name)
            }
            "customText" -> {
                createCustomTextInput(textInputLayout.context, customField.name)
            }
            "datePicker" -> {
                createCustomDateInput(textInputLayout.context, customField.name)
            }
            else -> null
        }

        if (dynamicInputField != null) textInputLayout.addView(dynamicInputField)
        return textInputLayout
    }

    private fun createCustomDateInput(context: Context, hint: String): CustomDatePickerEditText {
        val textInputEditText = CustomDatePickerEditText(context).apply {
            this.hint = hint
            id = View.generateViewId()
            setPadding(
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding)
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setCompoundDrawablePadding(context.resources.getDimensionPixelSize(R.dimen.drawable_padding))
        }

        return textInputEditText
    }

    private fun createCustomTextInput(context: Context, hint: String): CustomTextInputEditText {
        val textInputEditText = CustomTextInputEditText(context).apply {
            id = View.generateViewId()
            setPadding(
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding)
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setCompoundDrawablePadding(context.resources.getDimensionPixelSize(R.dimen.drawable_padding))
            this.hint = hint
        }
        textInputEditText.setOptions(Repository.codedValuesList)
        textInputEditText.setTag(hint)
        textInputEditText.setActivityContext(this)
        customMap[hint] = textInputEditText
        return textInputEditText
    }

    private fun createDecimalNumberInput(context: Context, hint: String): TextInputEditText {
        val textInputEditText = TextInputEditText(context).apply {
            this.hint = hint
            id = View.generateViewId()
            setPadding(
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding)
            )
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setCompoundDrawablePadding(context.resources.getDimensionPixelSize(R.dimen.drawable_padding))
        }
        return textInputEditText
    }

    private fun createNumberInput(context: Context, hint: String): TextInputEditText {
        val textInputEditText = TextInputEditText(context).apply {
            this.hint = hint
            id = View.generateViewId()
            setPadding(
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding)
            )
            inputType = InputType.TYPE_CLASS_NUMBER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setCompoundDrawablePadding(context.resources.getDimensionPixelSize(R.dimen.drawable_padding))
        }
        return textInputEditText
    }

    private fun createTextInput(context: Context, hint: String): TextInputEditText {
        val textInputEditText = TextInputEditText(context).apply {
            this.hint = hint
            id = View.generateViewId()
            setPadding(
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding),
                context.resources.getDimensionPixelSize(R.dimen.text_input_padding)
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setCompoundDrawablePadding(context.resources.getDimensionPixelSize(R.dimen.drawable_padding))
        }

        return textInputEditText
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
