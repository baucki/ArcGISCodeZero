package com.learning.arcgiscodezero.query

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.data.QueryParameters
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity7: ComponentActivity() {
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

    private fun setApiKey() {

        ArcGISEnvironment.apiKey = ApiKey.create("AAPK1e43bdcf9fa04fa0a729106fdd7a97fbNbpa3VVhaR5eKzfmkAFb0Uy_soNrGAjpslTJLcWQiNV6T3YGoRy8Sfa7a5ZXkBcj")

    }

    private fun createMap(featureLayer: FeatureLayer): ArcGISMap {

        return ArcGISMap(BasemapStyle.ArcGISTopographic).apply {
            initialViewpoint = Viewpoint(
                latitude = 34.0270,
                longitude = -118.8050,
                scale = 72000.0
            )

            operationalLayers.add(featureLayer)

        }

    }

    /*
    *   Query the [serviceFeatureTable] based on the [whereExpression] on the given
    *   [queryExtent] and select the resulting features on the [featureLayer]
    */
    private suspend fun queryFeatureLayer(
        context: Context,
        serviceFeatureTable: ServiceFeatureTable,
        featureLayer: FeatureLayer,
        whereExpression: String,
        queryExtent: Envelope?
    ) {

        // Clear any previous selections.
        featureLayer.clearSelection()
        // Create query parameters with the where expression and the current extent
        // and have geometry values returned in the results.
        val queryParameters = QueryParameters().apply {
            whereClause = whereExpression
            returnGeometry = true
            geometry = queryExtent
        }

        try {
            // Query the feature table with the query parameters.
            val featureQueryResult = serviceFeatureTable.queryFeatures(queryParameters).getOrThrow()
            // Iterate through the result and select the features on the feature layer.
            val resultIterator = featureQueryResult.iterator()
            if (resultIterator.hasNext()) {
                resultIterator.forEach { feature ->
                    featureLayer.selectFeature(feature)
                }
            } else {
                showMessage(
                    context,
                    "No parcels found in the current extent, using Where expression: $whereExpression"
                )
            }
        } catch (e: Exception) {

            showMessage(context, "Feature search failed for: $whereExpression, ${e.message}")

        }

    }




    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val currentQueryJob = remember { mutableStateOf<Job?>(null) }
        // Store the current viewpoint geometry extent of the map.
        val currentExtent = remember { mutableStateOf<Envelope?>(null) }

        // Create a service feature table from a Los Angeles County parcels feature service.
        val serviceFeatureTable = ServiceFeatureTable(
            uri = "https://services3.arcgis.com/GVgbJbqm8hXASVYi/arcgis/rest/services/LA_County_Parcels/FeatureServer/0"
        )
        val featureLayer = remember { FeatureLayer.createWithFeatureTable(serviceFeatureTable) }

        val map = remember {
            createMap(featureLayer)
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
        ) {
            Column(
                Modifier.fillMaxSize().padding(it)
            ) {
                QueryDropDownMenu(
                    onItemClicked = { sqlQueryExpression ->
                        // Cancel the previous query job if it exists.
                        currentQueryJob.value?.cancel()
                        currentQueryJob.value = coroutineScope.launch {
                            queryFeatureLayer(
                                context = context,
                                serviceFeatureTable = serviceFeatureTable,
                                featureLayer = featureLayer,
                                whereExpression = sqlQueryExpression,
                                queryExtent = currentExtent.value
                            )
                        }
                    })

                MapView(
                    modifier = Modifier.fillMaxSize(),
                    arcGISMap = map,
                    onViewpointChangedForBoundingGeometry = { viewpoint ->
                        currentExtent.value = viewpoint.targetGeometry.extent
                    }
                )
            }
        }
    }

    private fun showMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun QueryDropDownMenu(onItemClicked: (String) -> Unit) {
        val expanded = remember { mutableStateOf(false) }
        val selection = remember { mutableStateOf("") }
        val sqlQueryExpressions = listOf(
            "UseType = \'Government\'",
            "UseType = \'Residential\'",
            "UseType = \'Irrigated Farm\'",
            "TaxRateArea = 10853",
            "TaxRateArea = 10860",
            "Roll_LandValue > 1000000",
            "Roll_LandValue < 1000000"
        )

        ExposedDropdownMenuBox(
            expanded = expanded.value,
            onExpandedChange = {
                expanded.value = !expanded.value
            }
        ) {

            TextField(
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                readOnly = true,
                value = selection.value,
                onValueChange = { },
                label = { Text("Select a query expression") },
            )

            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = {
                    expanded.value = false
                }
            ) {
                sqlQueryExpressions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(text = selectionOption) },
                        onClick = {
                            selection.value = selectionOption
                            expanded.value = false
                            onItemClicked(selection.value)
                        }
                    )
                }
            }

        }

    }
}