package com.learning.arcgiscodezero.searchandlocation

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.location.LocationDisplayAutoPanMode
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.arcgismaps.toolkit.geoviewcompose.rememberLocationDisplay
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme
import kotlinx.coroutines.launch

class MainActivity9: ComponentActivity() {
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
    fun checkPermissions(context: Context): Boolean {
        // Check permissions to see if both permissions are granted.
        // Coarse location permission.
        val permissionCheckCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        // Fine location permission.
        val permissionCheckFineLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return permissionCheckCoarseLocation && permissionCheckFineLocation
    }


    private fun createMap(): ArcGISMap {

        val portal = Portal(
            url = "https://www.arcgis.com",
            connection = Portal.Connection.Anonymous
        )

        val portalItem = PortalItem(
            portal = portal,
            itemId = "41281c51f9de45edaf1c8ed44bb10e30"
        )

        return ArcGISMap(portalItem)

    }

    private fun setApiKey() {

        ArcGISEnvironment.apiKey = ApiKey.create("AAPK1e43bdcf9fa04fa0a729106fdd7a97fbNbpa3VVhaR5eKzfmkAFb0Uy_soNrGAjpslTJLcWQiNV6T3YGoRy8Sfa7a5ZXkBcj")

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        ArcGISEnvironment.applicationContext = context.applicationContext

        // create and remember a location display with a recenter auto pan mode
        val locationDisplay = rememberLocationDisplay().apply {
            setAutoPanMode(LocationDisplayAutoPanMode.Recenter)
        }

        if (checkPermissions(context)) {
            // permissions are already granted
            LaunchedEffect(Unit) {
                locationDisplay.dataSource.start()
            }
        } else {
            RequestPermissions(
                context = context,
                onPermissionsGranted = {
                    coroutineScope.launch {
                        locationDisplay.dataSource.start()
                    }
                }
            )
        }

        val map = remember { createMap() }

        Scaffold(
            topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
        ) {
            MapView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                arcGISMap = map,
                locationDisplay = locationDisplay
            )
        }
    }

    @Composable
    fun RequestPermissions(context: Context, onPermissionsGranted: () -> Unit) {
        // create an activity result launcher using permissions contract and handle the result
        val activityResultLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // check if both fine & coarse location permissions are true
            if (permissions.all {it.value}) {
                onPermissionsGranted()
            } else {
//            showError(context, "Location permissions were denied")
            }
        }
        LaunchedEffect(Unit) {
            activityResultLauncher.launch(
                // Request both fine and coarse location permissions
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

    }
}
