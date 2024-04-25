package com.learning.arcgiscodezero.offline

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.symbology.SimpleFillSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.portal.Portal
import com.arcgismaps.tasks.offlinemaptask.GenerateOfflineMapParameters
import com.arcgismaps.tasks.offlinemaptask.OfflineMapTask
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity13: ComponentActivity() {

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

    fun showMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun setApiKey() {

        ArcGISEnvironment.apiKey = ApiKey.create("AAPK1e43bdcf9fa04fa0a729106fdd7a97fbNbpa3VVhaR5eKzfmkAFb0Uy_soNrGAjpslTJLcWQiNV6T3YGoRy8Sfa7a5ZXkBcj")

    }

    private fun createAreaOfInterest(
        onOfflineAreaCreated: (Graphic) -> Unit
    ): Envelope {

        // create an envelope that defines the area to take offline
        val offlineArea = Envelope(
            xMin = -88.1526,
            yMin = 41.7694,
            xMax = -88.1490,
            yMax = 41.7714,
            spatialReference = SpatialReference.wgs84()
        )

        // create a graphic to display the area to take offline
        val lineSymbol = SimpleLineSymbol(
            style = SimpleLineSymbolStyle.Solid,
            color = Color.red,
            width = 2f
        )
        val fillSymbol = SimpleFillSymbol(
            style = SimpleFillSymbolStyle.Solid,
            color = Color.transparent,
            outline = lineSymbol
        )
        val offlineAreaGraphic = Graphic(
            geometry = offlineArea,
            symbol = fillSymbol
        )

        // add the offline area outline to the graphics overlay
        onOfflineAreaCreated(offlineAreaGraphic)

        return offlineArea
    }

    private suspend fun downloadOfflineMapArea(
        context: Context,
        coroutineScope: CoroutineScope,
        offlineMapTask: OfflineMapTask,
        parameters: GenerateOfflineMapParameters,
        updateProgress: (Int) -> Unit,
        onDownloadOfflineMapSuccess: (ArcGISMap) -> Unit,
        onDownloadOfflineMapFailure: () -> Unit,
    ) {
        // build a folder path named with today's date/time to store the offline map
        val downloadLocation =
            context.getExternalFilesDir(null)?.path + "OfflineMap_" + Calendar.getInstance().time

        // create a job with GenerateOfflineMapParameters and download directory path
        val generateOfflineMapJob = offlineMapTask.createGenerateOfflineMapJob(
            parameters = parameters,
            downloadDirectoryPath = downloadLocation
        )

        coroutineScope.launch {
            generateOfflineMapJob.progress.collect { progress ->
                updateProgress(progress)
            }
        }

        // start the job to download the offline map
        generateOfflineMapJob.start()
        showMessage( context, "Generate offline map job has started.")

        generateOfflineMapJob.result().onSuccess { generateOfflineMapResult ->
            onDownloadOfflineMapSuccess(generateOfflineMapResult.offlineMap)
        }.onFailure {
            onDownloadOfflineMapFailure()
        }
    }

    private fun createMap(): ArcGISMap {
        val portal = Portal(
            url = "https://www.arcgis.com",
            connection = Portal.Connection.Anonymous
        )
        val portalItem = PortalItem(
            portal = portal,
            itemId = "acc027394bc84c2fb04d1ed317aac674"
        )
        return ArcGISMap(portalItem)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val currentProgress = remember { mutableIntStateOf(0) }
        val showProgressBar = remember { mutableStateOf(false) }

        val graphicsOverlay = remember { GraphicsOverlay() }
        val graphicsOverlays = remember { listOf(graphicsOverlay) }

        val offlineArea = remember {
            createAreaOfInterest(
                onOfflineAreaCreated = { graphic -> graphicsOverlay.graphics.add(graphic) }
            )
        }

        var map = remember {
            createMap()
        }

        LaunchedEffect(Unit) {
            // create an offline map task using the current map
            val offlineMapTask = OfflineMapTask(map)

            // create a default set of parameters for generating the offline map from the area of interest
            offlineMapTask.createDefaultGenerateOfflineMapParameters(offlineArea)
                .onSuccess { parameters ->
                    // when the parameters are successfully created, show progress bar and message
                    showProgressBar.value = true
                    showMessage(context, "Default parameters have been created.")

                    // download the offline map
                    downloadOfflineMapArea(
                        context = context,
                        coroutineScope = coroutineScope,
                        offlineMapTask = offlineMapTask,
                        parameters = parameters,
                        updateProgress = { currentProgress.value = it },
                        onDownloadOfflineMapSuccess = { offlineMap ->
                            // hide progress bar and show message when the job executes successfully
                            showProgressBar.value = false
                            showMessage(context, "Downloaded offline map area successfully.")
                            // replace the current map with the result offline map
                            map = offlineMap
                            // clear the offline area outline
                            graphicsOverlay.graphics.clear()
                        },
                        onDownloadOfflineMapFailure = {
                            showProgressBar.value = false
                            showMessage(context, "Failed to download offline map area.")

                        }
                    )
                }
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                if (showProgressBar.value) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = currentProgress.value / 100f
                    )
                }
                MapView(
                    modifier = Modifier.fillMaxSize(),
                    arcGISMap = map,
                    graphicsOverlays = graphicsOverlays
                )
            }
        }
    }

}