package com.learning.arcgiscodezero.authentication

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
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.httpcore.authentication.OAuthUserConfiguration
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.ArcGISMapImageLayer
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.toolkit.authentication.Authenticator
import com.arcgismaps.toolkit.authentication.AuthenticatorState
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme

class MainActivity14: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        // Create a layer to display the ArcGIS World Traffic service
        val trafficLayer = ArcGISMapImageLayer("https://traffic.arcgis.com/arcgis/rest/services/World/Traffic/MapServer")

        return ArcGISMap(BasemapStyle.ArcGISTopographic).apply {
            initialViewpoint = Viewpoint(
                latitude = 34.02700,
                longitude = -118.80543,
                scale = 72000.0
            )

            operationalLayers.add(trafficLayer)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {

        val oAuthClientId = stringResource(id = R.string.oauth_client_id)
        val oAuthRedirectUriScheme = stringResource(id = R.string.oauth_redirect_uri_scheme)
        val oAuthRedirectUriHost = stringResource(id = R.string.oauth_redirect_uri_host)

        val oAuthRedirectUri = oAuthRedirectUriScheme + "://" + oAuthRedirectUriHost

        val authenticatorState = remember {
            AuthenticatorState().apply {
                oAuthUserConfiguration = OAuthUserConfiguration(
                    portalUrl = "https://www.arcgis.com",
                    clientId = oAuthClientId,
                    redirectUrl = oAuthRedirectUri
                )
            }
        }

        val map = remember {
            createMap()
        }
        Scaffold(
            topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
        ) {
            MapView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                arcGISMap = map,
            )
        }

        Authenticator(authenticatorState = authenticatorState)
    }
}