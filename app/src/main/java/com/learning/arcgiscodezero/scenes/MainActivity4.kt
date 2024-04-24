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
import com.arcgismaps.mapping.ArcGISScene
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal
import com.arcgismaps.toolkit.geoviewcompose.SceneView
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme

class MainActivity4: ComponentActivity() {
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

    val portal = Portal(
        url = "https://www.arcgis.com",
        connection = Portal.Connection.Anonymous
    )

    val portalItem = PortalItem(
        portal = portal,
        itemId = "579f97b2f3b94d4a8e48a5f140a6639b"
    )

    return ArcGISScene(portalItem)
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