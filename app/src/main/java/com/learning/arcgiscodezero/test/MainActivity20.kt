package com.learning.arcgiscodezero.test

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
import com.arcgismaps.geometry.PolylineBuilder
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme

class MainActivity20: ComponentActivity() {

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
                latitude = 42.4262,
                longitude = 18.64,
                scale = 310000.0
            )
        }
    }

    private fun setApiKey() {
        ArcGISEnvironment.apiKey = ApiKey.create("AAPK1e43bdcf9fa04fa0a729106fdd7a97fbNbpa3VVhaR5eKzfmkAFb0Uy_soNrGAjpslTJLcWQiNV6T3YGoRy8Sfa7a5ZXkBcj")
    }
    private val routePoints = listOf(
        Point(x = 18.76770, y = 42.42620), // kotor
        Point(18.75856, 42.43886),
        Point(18.76004, 42.45972),
        Point(18.73773, 42.47778),
        Point(18.71103, 42.48123),
        Point(18.68869, 42.48678),// gospa
        Point(18.68809, 42.47797),
        Point(18.68244, 42.47042),
        Point(18.67867, 42.45893),
        Point(18.65919, 42.44280),
        Point(18.61955, 42.43163),
        Point(18.59724, 42.43012),
        Point(18.58127, 42.43038),// marine base
        Point(18.58040, 42.43437),
        Point(18.56997, 42.43598),
        Point(18.55535, 42.43390),
        Point(18.54165, 42.42193),
        Point(18.54518, 42.40609),
        Point(18.55822, 42.39517),// mamula
        Point(18.57946, 42.37602),
        Point(x = 18.59634, y = 42.37386) // plava spilja
    )

    private val blueOutlineSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(0, 0, 255), 2f)
    private fun createPointGraphic(latitude: Double, longitude: Double, outlineSymbol: SimpleLineSymbol): Graphic {
        // Point geometry
        val point = Point(
            x = longitude,
            y = latitude,
            spatialReference = SpatialReference.wgs84()
        )

        // Point symbol
        val simpleMarkerSymbol = SimpleMarkerSymbol(
            style = SimpleMarkerSymbolStyle.Circle,
            color = Color.red,
            size = 10f
        )
        simpleMarkerSymbol.outline = outlineSymbol

        // Create and return the graphic
        return Graphic(geometry = point, symbol = simpleMarkerSymbol)
    }

    private fun createPolylineGraphic(points: List<Point>): Graphic {
        // Polyline symbol
        val polylineSymbol = SimpleLineSymbol(
            style = SimpleLineSymbolStyle.Solid,
            color = Color.fromRgba(0, 0, 255),
            width = 2f
        )

        // Polyline geometry using PolylineBuilder
        val polylineBuilder = PolylineBuilder(SpatialReference.wgs84()) {
            points.forEach { point ->
                addPoint(point.x, point.y)
            }
        }
        val polyline = polylineBuilder.toGeometry()

        // Create and return the graphic
        return Graphic(geometry = polyline, symbol = polylineSymbol)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {

        val map = remember {
            createMap()
        }

        // park kotor (latitude = 42.42620, longitude = 18.76770)
        // gospa od skrpjela (latitude = 42.48678, longitude = 18.68869)
        // tivat stara baza podmornica (latitude = 42.43038, longitude = 18.58127)
        // mamula (latitude = 42.39517, longitude = 18.55822)
        // plava spilja (latitude = 42.37386, longitude = 18.59634)

        // create a graphics overlay
        val graphicsOverlay = remember { GraphicsOverlay() }

        // add the point graphic to the graphics overlay
        graphicsOverlay.graphics.add(createPointGraphic(42.42620, 18.76770, blueOutlineSymbol))
        graphicsOverlay.graphics.add(createPointGraphic(42.48678, 18.68869, blueOutlineSymbol))
        graphicsOverlay.graphics.add(createPointGraphic(42.43038, 18.58127, blueOutlineSymbol))
        graphicsOverlay.graphics.add(createPointGraphic(42.39517, 18.55822, blueOutlineSymbol))
        graphicsOverlay.graphics.add(createPointGraphic(42.37386, 18.59634, blueOutlineSymbol))
        graphicsOverlay.graphics.add(createPolylineGraphic(routePoints))

        // create a list of graphics overlays used by the MapView
        val graphicsOverlays = remember { listOf(graphicsOverlay) }

        Scaffold(
            topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
        ) {
            MapView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                arcGISMap = map,
                graphicsOverlays = graphicsOverlays
            )
        }

    }
}