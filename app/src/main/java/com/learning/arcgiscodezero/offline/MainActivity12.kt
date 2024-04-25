package com.learning.arcgiscodezero.offline

import android.content.Context
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.MobileMapPackage
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme
import java.io.File

class MainActivity12: ComponentActivity() {
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

    fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun setApiKey() {

        ArcGISEnvironment.apiKey = ApiKey.create("AAPK1e43bdcf9fa04fa0a729106fdd7a97fbNbpa3VVhaR5eKzfmkAFb0Uy_soNrGAjpslTJLcWQiNV6T3YGoRy8Sfa7a5ZXkBcj")

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val context = LocalContext.current
        // get the path to mobile map package
        val mmpkFilePath = context.getExternalFilesDir(null)?.path + File.separator +
                stringResource(
                    id = R.string.mahourivieratrails_mmpk
                )

        // Create the ArcGIS Map which would use the map provided by the map package
        val map = remember {
            mutableStateOf(ArcGISMap(BasemapStyle.ArcGISTopographic))
        }

        LaunchedEffect(Unit) {

            // Load the mobile map package
            val mapPackage = MobileMapPackage(mmpkFilePath)

            mapPackage.load().onSuccess {
                map.value = mapPackage.maps.first()
            }.onFailure { error ->
                showError(context, "Failed to load mobile map package: ${error.message}")
            }

        }

        Scaffold(
            topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
        ) {
            MapView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                arcGISMap = map.value,
            )
        }
    }
}