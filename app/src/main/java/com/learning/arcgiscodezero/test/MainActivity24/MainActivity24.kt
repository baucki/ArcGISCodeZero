package com.learning.arcgiscodezero.test.MainActivity24

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.data.CodedValueDomain
import com.arcgismaps.data.Feature
import com.arcgismaps.data.FeatureQueryResult
import com.arcgismaps.data.FieldType
import com.arcgismaps.data.InheritedDomain
import com.arcgismaps.data.QueryParameters
import com.arcgismaps.data.RangeDomain
import com.arcgismaps.data.ServiceFeatureTable
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
import com.learning.arcgiscodezero.test.MainActivity24.Repository.feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity24 : AppCompatActivity(), DeleteConfirmationDialogFragment.ConfirmationListener {

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
        val searchButton = findViewById<ImageButton>(R.id.searchButton)

        featureAttributesAdapter = FeatureAttributesAdapter(emptyList())

        ArcGISEnvironment.apiKey = ApiKey.create("AAPK1e43bdcf9fa04fa0a729106fdd7a97fbNbpa3VVhaR5eKzfmkAFb0Uy_soNrGAjpslTJLcWQiNV6T3YGoRy8Sfa7a5ZXkBcj")
        lifecycle.addObserver(mapView)

        val map = ArcGISMap(BasemapStyle.ArcGISStreets).apply {
            operationalLayers.add(featureLayer)
            Repository.featureLayer = featureLayer
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
        searchButton.setOnClickListener {
            featureLayer.clearSelection()

            val queryParameters = QueryParameters().apply {
                whereClause = ("upper(vrsta) LIKE '%Palma%'")
            }

            lifecycleScope.launch {
                val featureQueryResult = serviceFeatureTable.queryFeatures(queryParameters).getOrElse {
                    showSnackbar("error")
                } as FeatureQueryResult

                for (feature in featureQueryResult) {
                    featureLayer.selectFeature(feature)
                }
            }
        }

        lifecycleScope.launch {
            fetchFeatureLayers("http://192.168.1.18:6080/arcgis/rest/services/Servis_SP4_FieldTools/FeatureServer", map)
        }
    }

    private suspend fun fetchFeatureLayers(serviceUrl: String, map: ArcGISMap) {
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url("$serviceUrl?f=json").build()
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                if (responseData != null) {
                    val jsonResponse = JSONObject(responseData)
                    val layers = jsonResponse.getJSONArray("layers")

                    for (i in 0 until layers.length()) {
                        val layer = layers.getJSONObject(i)
                        val layerId = layer.getInt("id")
                        val layerUrl = "$serviceUrl/$layerId"
                        val serviceFeatureTable = ServiceFeatureTable(layerUrl)
                        val featureLayer = FeatureLayer.createWithFeatureTable(serviceFeatureTable)
                        withContext(Dispatchers.Main) {
                            map.operationalLayers.add(featureLayer)
                        }
                        println(layerUrl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initListeners() {
        editButton.findViewById<Button>(R.id.editButton).setOnClickListener {
            startActivity(Intent(this, EditFeatureActivity::class.java))
        }
        deleteButton.findViewById<Button>(R.id.deleteButton).setOnClickListener {
            val dialog = DeleteConfirmationDialogFragment()
            dialog.show(supportFragmentManager, "deleteConfirmationDialog")
        }
    }
    override fun onConfirmDelete() {
        try {
            lifecycleScope.launch {
                serviceFeatureTable.deleteFeature(feature!!).apply {
                    onSuccess {
                        serviceFeatureTable.applyEdits()
                    }
                    onFailure {
                        val rootView = findViewById<View>(android.R.id.content)
                        Snackbar.make(rootView, "Failed to update feature", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView, "An error occurred", Snackbar.LENGTH_SHORT).show()
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
                    feature = identifiedFeature
                    val featureAttributes = identifiedFeature.attributes
                    val aliasAttributes = mutableMapOf<String, Any?>()

                    val featureTable = identifiedFeature.featureTable as ServiceFeatureTable
                    val fields = featureTable.fields
                    val types = featureTable.featureTypes

                    Repository.fields = fields
                    Repository.types = types

                    for (type in types) {
                        val domains = type.domains
                        for (domain in domains) {
                            val domainDetails = when (domain.value) {
                                is CodedValueDomain -> {
                                    val codedValues = (domain.value as CodedValueDomain).codedValues.joinToString { it.name }
                                    "Coded Values: $codedValues"
                                }
                                is InheritedDomain -> {
                                    val codedValues = (domain.value as CodedValueDomain).codedValues.joinToString { it.name }
                                    "Coded Values: $codedValues"
                                }
                                is RangeDomain -> {
                                    "Range: ${(domain.value as RangeDomain).minValue} - ${(domain.value as RangeDomain).maxValue}"
                                }
                                else -> "No domain"
                            }
                            println("${type.id}, ${type.name}, ${domain.key}, $domainDetails")
                        }
                    }

                    for (field in fields) {
//                        val fieldType = when (field.fieldType) {
//                            FieldType.Text -> "Text"
//                            FieldType.Int16 -> "Short"
//                            FieldType.Int32 -> "Integer"
//                            FieldType.Int64 -> "Long"
//                            FieldType.Float32 -> "Float"
//                            FieldType.Float64 -> "Double"
//                            FieldType.Date -> "Date"
//                            FieldType.DateOnly -> "DateOnly"
//                            FieldType.Oid -> "OID"
//                            FieldType.Geometry -> "Geometry"
//                            FieldType.GlobalId -> "GlobalId"
//                            FieldType.Blob -> "Blob"
//                            FieldType.Raster -> "Raster"
//                            FieldType.Guid -> "GUID"
//                            FieldType.Xml -> "XML"
//                            else -> "Unknown"
//                        }
//                        val domain = field.domain
//                        val domainDetails = when (domain) {
//                            is CodedValueDomain -> {
//                                val codedValues = domain.codedValues.joinToString { it.name }
//                                "Coded Values: $codedValues"
//                            }
//                            is RangeDomain -> "Range: ${domain.minValue} - ${domain.maxValue}"
//                            else -> "No Domain"
//                        }
//                        println("Field Name: ${field.name}, Field Type: $fieldType, Domain: $domainDetails")
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
                            "tip" to 2.toShort(),
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