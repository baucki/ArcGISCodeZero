package com.learning.arcgiscodezero.routing

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.Point
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.SingleTapConfirmedEvent
import com.arcgismaps.tasks.networkanalysis.DirectionManeuver
import com.arcgismaps.tasks.networkanalysis.RouteParameters
import com.arcgismaps.tasks.networkanalysis.RouteResult
import com.arcgismaps.tasks.networkanalysis.RouteTask
import com.arcgismaps.tasks.networkanalysis.Stop
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity11: ComponentActivity() {
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

    private fun createMap(): ArcGISMap {
        return ArcGISMap(BasemapStyle.ArcGISTopographic).apply {
            initialViewpoint = Viewpoint(
                latitude = 34.0539,
                longitude = -118.2453,
                scale = 144447.638572
            )
        }
    }

    private fun addStop(
        routeStops: MutableList<Stop>,
        stop: Stop,
        graphicsOverlay: GraphicsOverlay
    ) {
        routeStops.add(stop)
        // create a green circle symbol for the stop
        val stopMarker = SimpleMarkerSymbol(
            style = SimpleMarkerSymbolStyle.Circle,
            color = Color.green,
            size = 20f
        )
        // get the stop's geometry
        val routeStopGeometry = stop.geometry
        // add graphic to graphics overlay
        graphicsOverlay.graphics.add(
            Graphic(
                geometry = routeStopGeometry,
                symbol = stopMarker
            )
        )
    }

    private suspend fun findRoute(
        context: Context,
        routeStops: MutableList<Stop>,
        graphicsOverlay: GraphicsOverlay,
        directionList: MutableList<String>
    ) {
        val routeTask = RouteTask(
            url = "https://route-api.arcgis.com/arcgis/rest/services/World/Route/NAServer/Route_World"
        )

        // create a job to find the route
        try {
            val routeParameters: RouteParameters = routeTask.createDefaultParameters().getOrThrow()
            routeParameters.setStops(routeStops)
            routeParameters.returnDirections = true

            // solve a route using the route parameters created
            val routeResult: RouteResult = routeTask.solveRoute(routeParameters).getOrThrow()
            val routes = routeResult.routes

            // if route is found
            if (routes.isNotEmpty()) {
                val route = routes[0]
                val routeGraphic = Graphic(
                    geometry = route.routeGeometry,
                    symbol = SimpleLineSymbol(
                        style = SimpleLineSymbolStyle.Solid,
                        color = Color.green,
                        width = 2f
                    )
                )
                // add the route graphic to the graphic overlay
                graphicsOverlay.graphics.add(routeGraphic)
                // get the direction text for each maneuver and display it as a list on the UI
                directionList.clear()
                route.directionManeuvers.forEach { directionManeuver: DirectionManeuver ->
                    directionList.add(directionManeuver.directionText)
                }
            }
        } catch (e: Exception) {
//            showMessage(context, "Failed to find route: ${e.message}")
        }
    }

    private fun clearStops(
        routeStops: MutableList<Stop>,
        directionList: MutableList<String>,
        graphicsOverlay: GraphicsOverlay
    ) {
        graphicsOverlay.graphics.clear()
        routeStops.clear()
        directionList.clear()
        directionList.add("Tap to add two points to the map to find a route between them.")
    }

    private fun onSingleTapConfirmed(
        context: Context,
        coroutineScope: CoroutineScope,
        currentJob: MutableState<Job?>,
        event: SingleTapConfirmedEvent,
        routeStops: MutableList<Stop>,
        graphicsOverlay: GraphicsOverlay,
        directionList: MutableList<String>
    ) {

        currentJob.value?.cancel()
        // retrieve the tapped map point from the SingleTapConfirmedEvent
        val point: Point = event.mapPoint ?: return showMessage(context, "No map point retrieved from tap.")
        val stop = Stop(point)

        when (routeStops.size) {
            // on first tap, add a stop
            0 -> {
                addStop(routeStops, stop, graphicsOverlay)
            }
            // on second tap, add a stop and find route between them
            1 -> {
                addStop(routeStops, stop, graphicsOverlay)
                currentJob.value = coroutineScope.launch {
                    findRoute(context, routeStops, graphicsOverlay, directionList)
                }
                // showMessage(context, "Calculating route...")
            }
            // on further tap, clear and add a new stop
            else -> {
                clearStops(routeStops, directionList, graphicsOverlay)
                addStop(routeStops, stop, graphicsOverlay)
            }
        }
    }

    @Composable
    private fun RouteList(directionList: MutableList<String>) {
        LazyColumn {
            items(directionList.size) { index ->
                Text(text = directionList[index] + ".")
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = androidx.compose.ui.graphics.Color.LightGray
                )
            }
        }
        
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val directionList = remember { mutableStateListOf("Tap to add two points to the map to find a route between them.") }
        val routeStops = remember { mutableListOf<Stop>() }
        val currentJob = remember { mutableStateOf<Job?>(null) }
        // create a graphic overlay to display the selected stops and route
        val graphicsOverlay = remember { GraphicsOverlay() }
        val graphicsOverlays = remember { listOf(graphicsOverlay) }


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
                MapView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f),
                    arcGISMap = map,
                    graphicsOverlays = graphicsOverlays,
                    onSingleTapConfirmed = { event ->
                        onSingleTapConfirmed(
                            context,
                            coroutineScope,
                            currentJob,
                            event,
                            routeStops,
                            graphicsOverlay,
                            directionList
                        )
                    }
                )
                RouteList(directionList = directionList)
            }
        }
    }
}