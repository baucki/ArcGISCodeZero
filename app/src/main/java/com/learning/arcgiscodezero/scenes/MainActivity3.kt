package com.learning.arcgiscodezero.scenes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISScene
import com.arcgismaps.mapping.ArcGISTiledElevationSource
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Surface
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.view.Camera
import com.arcgismaps.toolkit.geoviewcompose.SceneView
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme

// Display location
class MainActivity3: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApiKey()
        setContent {
            ArcGISCodeZeroTheme {
                MainScreen()
            }
        }
    }
}

private fun createScene(): ArcGISScene {
    // add base surface for elevation data
    val elevationSource = ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")
    val surface = Surface().apply {
        elevationSources.add(elevationSource)
        // add an exaggeration factor to increase the 3D effect of the elevation.
        elevationExaggeration = 2.5f
    }

    val cameraLocation = Point(
        x = -118.794,
        y = 33.909,
        z = 5330.0,
        spatialReference = SpatialReference.wgs84()
    )

    val camera = Camera(
        locationPoint = cameraLocation,
        heading = 355.0,
        pitch = 72.0,
        roll = 0.0
    )

    return ArcGISScene(BasemapStyle.ArcGISImagery).apply {

        baseSurface = surface

        initialViewpoint = Viewpoint(cameraLocation, camera)

    }
}

private fun setApiKey() {

    ArcGISEnvironment.apiKey = ApiKey.create("AAPK1e43bdcf9fa04fa0a729106fdd7a97fbNbpa3VVhaR5eKzfmkAFb0Uy_soNrGAjpslTJLcWQiNV6T3YGoRy8Sfa7a5ZXkBcj")

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen() {

    val scene = remember {
        createScene()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
    ) {

        SceneView(
            modifier = Modifier.fillMaxSize().padding(it),
            arcGISScene = scene
        )

    }

}
