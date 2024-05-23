package com.learning.arcgiscodezero.test.MainActivity24

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.data.Feature
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.view.MapView
import com.arcgismaps.mapping.view.ScreenCoordinate
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.learning.arcgiscodezero.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity24 : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var featureAttributesAdapter: FeatureAttributesAdapter

    private lateinit var editButton: Button
    private lateinit var deleteButton: Button

    private var isAddingFeature = false

    private val serviceFeatureTable = ServiceFeatureTable("http://192.168.1.18:6080/arcgis/rest/services/Servis_SP4_FieldTools/FeatureServer/0")
//    private val serviceFeatureTable = ServiceFeatureTable("https://services1.arcgis.com/4yjifSiIG17X0gW4/arcgis/rest/services/GDP_per_capita_1960_2016/FeatureServer/0")
    private val featureLayer = FeatureLayer.createWithFeatureTable(serviceFeatureTable)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        val toggleButton = findViewById<ToggleButton>(R.id.toggleButton)

        featureAttributesAdapter = FeatureAttributesAdapter(emptyList())

        ArcGISEnvironment.apiKey = ApiKey.create("AAPK1e43bdcf9fa04fa0a729106fdd7a97fbNbpa3VVhaR5eKzfmkAFb0Uy_soNrGAjpslTJLcWQiNV6T3YGoRy8Sfa7a5ZXkBcj")
        lifecycle.addObserver(mapView)

        val map = ArcGISMap(BasemapStyle.ArcGISStreets).apply {
            operationalLayers.add(featureLayer)
            featureLayer.isVisible = true
        }

        mapView.apply {
            mapView.map = map
            // Belgrade Viewpoint
            setViewpoint(
                Viewpoint(
                    Point(
                        x = 20.4489,
                        y = 44.8066,
                        spatialReference = SpatialReference.wgs84()
                    ),
                7e4)
            )
            // USA Viewpoint
//            setViewpoint(
//                Viewpoint(
//                    Envelope(
//                        -1131596.019761,
//                        3893114.069099,
//                        3926705.982140,
//                        7977912.461790
//                    )
//                )
//            )
            selectionProperties.color = Color.red

            lifecycleScope.launch {
                onSingleTapConfirmed.collect { tapEvent ->
                    val screenCoordinate = tapEvent.screenCoordinate
                    identifyFeature(screenCoordinate)
                }
            }
        }
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            isAddingFeature = isChecked
        }
    }

    private fun initListeners() {
        editButton.findViewById<Button>(R.id.editButton).setOnClickListener {
            startActivity(Intent(this, EditFeatureActivity::class.java))
        }
    }

    private fun displayFeatureAttributes(featureAttributes: Map<String, Any?>) {
        runOnUiThread {
            val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_attributes, null)
            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(bottomSheetView)

            editButton = bottomSheetView.findViewById(R.id.editButton)
            deleteButton = bottomSheetView.findViewById(R.id.deleteButton)

            initListeners()

            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView.parent as View)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this)
            featureAttributesAdapter.updateData(listOf(featureAttributes))
            recyclerView.adapter = featureAttributesAdapter

            bottomSheetDialog.show()
        }
    }
    private suspend fun identifyFeature(screenCoordinate: ScreenCoordinate) {
        featureLayer.clearSelection()
        val identifyLayerResult =
            mapView.identifyLayer(featureLayer, screenCoordinate, 5.0, false, 1)

        identifyLayerResult.apply {
            onSuccess { identifyLayerResult ->
                val geoElements = identifyLayerResult.geoElements

                if (!geoElements.isEmpty() && geoElements[0] is Feature) {
                    val identifiedFeature = geoElements[0] as Feature
                    val featureAttributes = identifiedFeature.attributes
                    val aliasAttributes = mutableMapOf<String, Any?>()

                    val featureTable = identifiedFeature.featureTable as ServiceFeatureTable
                    val fields = featureTable.fields

                    for (field in fields) {
                        if (field.alias == "objectid" || field.alias == "globalid") continue
                        val alias = field.alias
                        val attributeName = field.name
                        val attributeValue = featureAttributes[attributeName]
                        aliasAttributes[alias] = attributeValue
                        if (attributeValue.toString().contains("java.util.GregorianCalendar")) {
                            val dateString = attributeValue.toString()

                            val startIndex = dateString.indexOf("time=") + "time=".length
                            val endIndex = dateString.indexOf(",", startIndex)
                            val timeInMillis = dateString.substring(startIndex, endIndex).toLong()

                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = timeInMillis

                            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                            val formattedDate = sdf.format(calendar.time)
                            aliasAttributes[alias] = formattedDate
                        }
                    }
                    featureLayer.selectFeature(identifiedFeature)
                    displayFeatureAttributes(aliasAttributes)
                } else {
                    if (isAddingFeature) {
                        lifecycleScope.launch {

                        }
                        val mapPoint = mapView.screenToLocation(screenCoordinate)
                        val attributes = mutableMapOf<String, Any?>(
                            "vrsta" to null,
                            "fitopatoloske_promene" to null,
                            "entomoloske_promene" to null,
                            "slomljene_grane" to null,
                            "suve_grane" to null,
                            "suhovrhost" to null,
                            "isecene_debele_grane" to null,
                            "premaz" to null,
                            "ocena_kondicije" to null,
                            "ocena_dekorativnosti" to null,
                            "procena_starosti" to null,
                            "vreme_sadnje" to null,
                            "rasadnik" to null,
                            "cena_sadnice" to null,
                            "visina_stabla" to null,
                            "visina_debla" to null,
                            "prsni_precnik" to null,
                            "sirina_krosnje" to null,
                            "fitopatoloska_oboljenja" to null,
                            "entomoloska_oboljenja" to null,
                            "ottrulez_debla_izrazenost" to null,
                            "ottrulez_debla_velicina" to null,
                            "ottrulez_grana_izrazenost" to null,
                            "ottrulez_grana_velicina" to null,
                            "napomena" to null,
                            "pripada_drvoredu" to null,
                            "tip_kragne" to null,
                            "globalid" to null,
                            "ostalo" to null,
                            "list" to null,
                            "stablo" to null,
                            "koren" to null,
                            "grana" to null,
                            "krosnja" to null
                        )

                        val feature = serviceFeatureTable.createFeature(attributes, mapPoint)
                        serviceFeatureTable.addFeature(feature).apply {
                            onSuccess {
                                serviceFeatureTable.applyEdits()
                                showSnackbar("Feature added successfully.")
                            }
                            onFailure {
                                println(it.message)
                                showSnackbar("Failed to add feature: ${it.message}")
                            }
                        }
                    } else {
                        showSnackbar("No feature identified.")
                    }
                }
            }
        }
    }
    private fun showSnackbar(message: String) {
        Snackbar.make(mapView, message, Snackbar.LENGTH_SHORT).show()
    }
}