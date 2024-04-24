package com.learning.arcgiscodezero.maps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.PolygonBuilder
import com.arcgismaps.geometry.PolylineBuilder
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.symbology.SimpleFillSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme

class MainActivity : ComponentActivity() {
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

        return ArcGISMap(BasemapStyle.ArcGISTopographic).apply {

            initialViewpoint = Viewpoint(
                latitude = 34.0270,
                longitude = -118.8050,

                scale = 72000.0

            )

        }

    }

    private fun setApiKey() {

        ArcGISEnvironment.apiKey = ApiKey.create("AAPK1e43bdcf9fa04fa0a729106fdd7a97fbNbpa3VVhaR5eKzfmkAFb0Uy_soNrGAjpslTJLcWQiNV6T3YGoRy8Sfa7a5ZXkBcj")

    }

    // create a blue outline symbol
    private val blueOutlineSymbol by lazy {
        SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(0, 0, 255), 2f)
    }

    private val pointGraphic by lazy {

        // create a point geometry with a location and spatial reference
        // Point(latitude, longitude, spatial reference)
        val point = Point(
            x = -118.8065,
            y = 34.0005,
            spatialReference = SpatialReference.wgs84()
        )

        // create a point symbol that is an small red circle and assign the blue outline symbol to its outline property
        val simpleMarkerSymbol = SimpleMarkerSymbol(
            style = SimpleMarkerSymbolStyle.Circle,
            color = Color.red,
            size = 10f
        )
        simpleMarkerSymbol.outline = blueOutlineSymbol

        // create a graphic with the point geometry and symbol
        Graphic(
            geometry = point,
            symbol = simpleMarkerSymbol
        )
    }

    private val polyLineGraphic by lazy {

        // create a blue line symbol for the polyline
        val polylineSymbol = SimpleLineSymbol(
            style = SimpleLineSymbolStyle.Solid,
            color = Color.fromRgba(0, 0, 255),
            width = 3f
        )

        val polylineBuilder = PolylineBuilder(SpatialReference.wgs84()) {
            addPoint(-118.8215, 34.0139)
            addPoint(-118.8148, 34.0080)
            addPoint(-118.8088, 34.0016)
        }

        // then get the polyline from the polyline builder
        val polyline = polylineBuilder.toGeometry()

        // create a polyline graphic with the polyline geometry and symbol
        Graphic(
            geometry = polyline,
            symbol = polylineSymbol
        )
    }

    private val polygonGraphic by lazy {
        // create a polygon builder with spatial reference and add five vertices (points) to it.
        // then get the polygon from the polygon builder
        val polygonBuilder = PolygonBuilder(SpatialReference.wgs84()) {
            addPoint(-118.8189, 34.0137)
            addPoint(-118.8067, 34.0215)
            addPoint(-118.7914, 34.0163)
            addPoint(-118.7959, 34.0085)
            addPoint(-118.8085, 34.0035)
        }

        val polygon = polygonBuilder.toGeometry()

        // create a red fill symbol with an alpha component of 128
        val polygonFillSymbol = SimpleFillSymbol(
            style = SimpleFillSymbolStyle.Solid,
            color = Color.fromRgba(255, 0, 0, 128),
            outline = blueOutlineSymbol
        )

        // create a polygon graphic from the polygon geometry and symbol
        Graphic(
            geometry = polygon,
            symbol = polygonFillSymbol
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {

        val map = remember {
            createMap()
        }

        // create a graphics overlay
        val graphicsOverlay = remember { GraphicsOverlay() }

        // add the point graphic to the graphics overlay
        graphicsOverlay.graphics.add(pointGraphic)

        // add the polyline graphic to the graphics overlay
        graphicsOverlay.graphics.add(polyLineGraphic)

        // add the polygon graphic to the graphics overlay
        graphicsOverlay.graphics.add(polygonGraphic)

        // create a list of graphics overlays used by the MapView
        val graphicOverlays = remember { listOf(graphicsOverlay) }

        Scaffold(
            topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
        ) {
            MapView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                arcGISMap = map,
                graphicsOverlays = graphicOverlays
            )
        }

    }
}

