//package com.learning.arcgiscodezero.test.MainActivity24
//
//import android.os.Bundle
//import android.view.MotionEvent
//import android.view.View
//import androidx.activity.ComponentActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.esri.arcgisruntime.data.Feature
//import com.esri.arcgisruntime.data.ServiceFeatureTable
//import com.esri.arcgisruntime.layers.FeatureLayer
//import com.esri.arcgisruntime.mapping.ArcGISMap
//import com.esri.arcgisruntime.mapping.Basemap
//import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
//import com.esri.arcgisruntime.mapping.view.MapView
//import com.google.android.material.snackbar.Snackbar
//import com.learning.arcgiscodezero.R
//
//class MainActivity24 : ComponentActivity() {
//    private lateinit var mapView: MapView
//    private lateinit var featureLayer: FeatureLayer
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var featureAttributesAdapter: FeatureAttributesAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        mapView = findViewById(R.id.mapView)
//        recyclerView = findViewById(R.id.recyclerView)
//        featureAttributesAdapter = FeatureAttributesAdapter(emptyList())
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        recyclerView.adapter = featureAttributesAdapter
//
//
//        // Create a map with the Basemap type
//        val map = ArcGISMap(Basemap.createStreets())
//
//        // Create the feature layer using a URL to the feature service
//        val serviceFeatureTable = ServiceFeatureTable("https://sampleserver6.arcgisonline.com/arcgis/rest/services/USA/MapServer/0")
//        featureLayer = FeatureLayer(serviceFeatureTable)
//
//        // Add the feature layer to the map
//        map.operationalLayers.add(featureLayer)
//
//        // Set the map to the map view
//        mapView.map = map
//        mapView.isAttributionTextVisible = true // Add this line to enable gestures
//
//
//        // Set an on-touch listener for the map view to identify features
//        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
//            override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
//                identifyFeature(motionEvent.x, motionEvent.y)
//                return super.onSingleTapConfirmed(motionEvent)
//            }
//        }
//    }
//
//    private fun displayFeatureAttributes(featureAttributes: Map<String, Any?>) {
//        runOnUiThread {
//            featureAttributesAdapter.updateData(listOf(featureAttributes))
//            recyclerView.visibility = View.VISIBLE
//        }
//    }
//
//    private fun identifyFeature(x: Float, y: Float) {
//        val screenPoint = android.graphics.Point(x.toInt(), y.toInt())
//        val identifyLayerResultFuture = mapView.identifyLayerAsync(featureLayer, screenPoint, 5.0, false, 1)
//
//        identifyLayerResultFuture.addDoneListener {
//            try {
//                val identifyLayerResult = identifyLayerResultFuture.get()
//                val geoElements = identifyLayerResult.elements
//
//                if (!geoElements.isEmpty() && geoElements[0] is Feature) {
//                    val identifiedFeature = geoElements[0] as Feature
//                    val featureAttributes = identifiedFeature.attributes
//                    displayFeatureAttributes(featureAttributes)
////                    val message = "Feature ID: ${featureAttributes["objectid"]}"
////                    showSnackbar(message)
////                    val featureTable = featureLayer.featureTable
////                    val fieldsInfo = featureTable.fields.map { "${it.name} - ${it.alias ?: "No Alias"}" }
////                    val fieldsInfoString = fieldsInfo.joinToString(separator = "\n")
////                    showSnackbar("Feature Attributes:\n$fieldsInfoString")
////                    Log.d("random tag", fieldsInfoString)
//                } else {
//                    showSnackbar("No feature identified.")
//                }
//            } catch (e: Exception) {
//                showSnackbar("Error identifying feature: ${e.message}")
//            }
//        }
//    }
//
//    private fun showSnackbar(message: String) {
//        Snackbar.make(mapView, message, Snackbar.LENGTH_SHORT).show()
//    }
//
//    override fun onPause() {
//        mapView.pause()
//        super.onPause()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mapView.resume()
//    }
//
//    override fun onDestroy() {
//        mapView.dispose()
//        super.onDestroy()
//    }
//}