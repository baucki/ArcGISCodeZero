package com.learning.arcgiscodezero.test

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.data.Feature
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.symbology.HorizontalAlignment
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.symbology.TextSymbol
import com.arcgismaps.mapping.symbology.VerticalAlignment
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.portal.Portal
import com.arcgismaps.tasks.geocode.GeocodeParameters
import com.arcgismaps.tasks.geocode.GeocodeResult
import com.arcgismaps.tasks.geocode.LocatorTask
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.arcgismaps.toolkit.geoviewcompose.MapViewProxy
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.loadable.LoadStatus
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity23: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApiKey()
        setContent {
            ArcGISCodeZeroTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
    private fun createMap(): ArcGISMap {

        return ArcGISMap(BasemapStyle.ArcGISImagery).apply {
            initialViewpoint = Viewpoint(
                longitude = 18.7777,
                latitude = 42.4242,
                scale = 100000.0
            )
        }

    }

    fun showMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun setApiKey() {
        ArcGISEnvironment.apiKey = ApiKey.create("AAPK1e43bdcf9fa04fa0a729106fdd7a97fbNbpa3VVhaR5eKzfmkAFb0Uy_soNrGAjpslTJLcWQiNV6T3YGoRy8Sfa7a5ZXkBcj")
    }

    private suspend fun searchAddress(
        context: Context,
        coroutineScope: CoroutineScope,
        query: String,
        currentSpatialReference: SpatialReference?,
        graphicsOverlay: GraphicsOverlay,
        mapViewProxy: MapViewProxy
    ) {

        val geocodeServerUri = "https://utility.arcgis.com/usrsvcs/servers/0d82541a520c4d04b152d8c787d066e2/rest/services/World/GeocodeServer"
        val locatorTask = LocatorTask(geocodeServerUri)

        // create geocode parameters
        val geocodeParameters = GeocodeParameters().apply {
            resultAttributeNames.add("*")
            maxResults = 1
            outputSpatialReference = currentSpatialReference
        }

        // search for the address
        locatorTask.geocode(searchText = query, parameters = geocodeParameters)
            .onSuccess { geocodeResults: List<GeocodeResult> ->
                handleGeocodeResults(
                    context, coroutineScope, geocodeResults, graphicsOverlay, mapViewProxy
                )
            }.onFailure { error ->
                showMessage(context, "The locatorTask.geocode() call failed: ${error.message}")
            }
    }

    private fun createTextGraphic(geocodeResult: GeocodeResult): Graphic {
        val textSymbol = TextSymbol(
            text = geocodeResult.label,
            color = Color.black,
            size = 18f,
            horizontalAlignment = HorizontalAlignment.Center,
            verticalAlignment = VerticalAlignment.Bottom
        ).apply {
            offsetY = 8f
            haloColor = Color.white
            haloWidth = 2f
        }
        return Graphic(
            geometry = geocodeResult.displayLocation,
            symbol = textSymbol
        )
    }

    private fun createMarkerGraphic(geocodeResult: GeocodeResult): Graphic {
        val simpleMarkerSymbol = SimpleMarkerSymbol(
            style = SimpleMarkerSymbolStyle.Square,
            color = Color.red,
            size = 12.0f
        )
        return Graphic(
            geometry = geocodeResult.displayLocation,
            attributes = geocodeResult.attributes,
            symbol = simpleMarkerSymbol
        )
    }

    private fun handleGeocodeResults(
        context: Context,
        coroutineScope: CoroutineScope,
        geocodeResult: List<GeocodeResult>,
        graphicsOverlay: GraphicsOverlay,
        mapViewProxy: MapViewProxy

    ) {
        if (geocodeResult.isNotEmpty()) {
            val geocodeResult = geocodeResult[0]
            // create a text graphic to display the address text, and add it to the graphics overlay.
            val textGraphic = createTextGraphic(geocodeResult)
            // create a red square marker graphic, and add it to the graphics overlay
            val markerGraphic = createMarkerGraphic(geocodeResult)
            // clear previous results and add graphics.
            graphicsOverlay.graphics.apply {
                clear()
                add(textGraphic)
                add(markerGraphic)
            }
            coroutineScope.launch {
                val centerPoint = geocodeResult.displayLocation
                    ?: return@launch showMessage(context, "The locatorTask.geocode() call failed")

                // animate the map view to the center point
                mapViewProxy.setViewpointCenter(centerPoint)
                    .onFailure { error ->
                        showMessage(context, "Failed to set Viewpoint center: ${error.message}")
                    }
            }
        } else {
            showMessage(context, "No address found for the given query")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        // the focus manager is used to dismiss keyboard after search query submission
        val focusManager = LocalFocusManager.current
        var queryText by remember { mutableStateOf("") }
        val currentJob = remember { mutableStateOf<Job?>(null) }
        val graphicsOverlay = remember { GraphicsOverlay() }
        val graphicsOverlays = remember { listOf(graphicsOverlay) }
        val mapViewProxy = remember { MapViewProxy() }
        val currentSpatialReference = remember { mutableStateOf<SpatialReference?>(null) }

        val map = remember {
            createMap()
        }
        Scaffold(
            topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                SearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    query = queryText,
                    onQueryChange = { query -> queryText = query },
                    onSearch = { currentQuery ->
                        focusManager.clearFocus()
                        // cancel any previous search job
                        currentJob.value?.cancel()
                        // start a new search job
                        currentJob.value = coroutineScope.launch {
                            searchAddress(
                                context,
                                coroutineScope,
                                currentQuery,
                                currentSpatialReference.value,
                                graphicsOverlay,
                                mapViewProxy
                            )
                        }
                    },
                    active = false,
                    onActiveChange = {},
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    placeholder = { Text("Search for a restaurant address") }
                ) {}
                MapView(
                    modifier = Modifier.fillMaxSize(),
                    arcGISMap = map,
                    graphicsOverlays = graphicsOverlays,
                    mapViewProxy = mapViewProxy,
                    onSpatialReferenceChanged = { spatialReference ->
                        currentSpatialReference.value = spatialReference
                    }
                )
            }
        }
    }
}