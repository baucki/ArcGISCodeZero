package com.learning.arcgiscodezero.stylesfeaturelayer

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
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.labeling.ArcadeLabelExpression
import com.arcgismaps.mapping.labeling.LabelDefinition
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.symbology.ClassBreak
import com.arcgismaps.mapping.symbology.ClassBreaksRenderer
import com.arcgismaps.mapping.symbology.FontStyle
import com.arcgismaps.mapping.symbology.FontWeight
import com.arcgismaps.mapping.symbology.PictureMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbol
import com.arcgismaps.mapping.symbology.SimpleFillSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleRenderer
import com.arcgismaps.mapping.symbology.TextSymbol
import com.arcgismaps.mapping.symbology.UniqueValue
import com.arcgismaps.mapping.symbology.UniqueValueRenderer
import com.arcgismaps.portal.Portal
import com.arcgismaps.toolkit.geoviewcompose.MapView
import com.learning.arcgiscodezero.R
import com.learning.arcgiscodezero.ui.theme.ArcGISCodeZeroTheme

class MainActivity8: ComponentActivity() {

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

    private fun createFeatureLayer(featureServiceUri: String): FeatureLayer {
        // create a service feature table from uri
        val serviceFeatureTable = ServiceFeatureTable(featureServiceUri)
        // return a feature layer created from the service feature table
        return FeatureLayer.createWithFeatureTable(serviceFeatureTable)

    }

    private fun createOpenSpaceLayer(featureServiceUri: String): FeatureLayer {
        // create fill symbol objects to represent the parks and open space layer
        val magentaFillSymbol = SimpleFillSymbol(SimpleFillSymbolStyle.Solid, Color.fromRgba(255, 0, 255), null)
        val greenFillSymbol = SimpleFillSymbol(SimpleFillSymbolStyle.Solid, Color.green, null)
        val blueFillSymbol = SimpleFillSymbol(SimpleFillSymbolStyle.Solid, Color.fromRgba(0, 0, 255), null)
        val redFillSymbol = SimpleFillSymbol(SimpleFillSymbolStyle.Solid, Color.red, null)

        // create a unique value for natural areas, regional open spaces, local parks, and regional parks
        val naturalAreas = UniqueValue(
            description = "Natural Areas",
            label =  "Natural Areas",
            symbol = magentaFillSymbol,
            values = listOf("Natural Areas")
        )
        val regionalOpenSpace = UniqueValue(
            description = "Regional Open Space",
            label =  "Regional Open Space",
            symbol = greenFillSymbol,
            values = listOf("Regional Open Space")
        )
        val localPark = UniqueValue(
            description = "Local Park",
            label = "Local Park",
            symbol = blueFillSymbol,
            values = listOf("Local Park")
        )
        val regionalRecreationPark = UniqueValue(
            description = "Regional Recreation Park",
            label = "Regional Recreation Park",
            symbol = redFillSymbol,
            values = listOf("Regional Recreation Park")
        )
        // Create a unique value list with the fill symbols
        val uniqueValuesList = listOf(
            naturalAreas,
            regionalOpenSpace,
            localPark,
            regionalRecreationPark
        )

        // create and assign a unique value renderer to the feature layer
        val openSpaceUniqueValueRenderer =
            UniqueValueRenderer(
                fieldNames = listOf("TYPE"),
                uniqueValues = uniqueValuesList,
                defaultLabel = "Open Spaces",
                defaultSymbol = null
            )

        // create a parks and open spaces feature layer
        val featureLayer = createFeatureLayer(featureServiceUri).apply {
            // style the feature layer using the unique value renderer
            renderer = openSpaceUniqueValueRenderer
            // set layer opacity to semi-transparent
            opacity = 0.2f
        }

        return featureLayer
    }

    private fun createTrailsLayer(featureServiceUri: String): FeatureLayer {
        // create simple symbol objects to represent the trails layer
        val firstClassSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(255, 0, 255), 3.0f)
        val secondClassSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(255, 0, 255), 4.0f)
        val thirdClassSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(255, 0, 255), 5.0f)
        val fourthClassSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(255, 0, 255), 6.0f)
        val fifthClassSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(255, 0, 255), 7.0f)

        // create 5 class breaks
        val firstClassBreak = ClassBreak(
            description = "Under 500",
            label = "0 - 500",
            minValue = 0.0,
            maxValue = 500.0,
            symbol = firstClassSymbol
        )
        val secondClassBreak = ClassBreak(
            description = "501 to 1000",
            label = "501 - 1000",
            minValue = 501.0,
            maxValue = 1000.0,
            symbol = secondClassSymbol
        )
        val thirdClassBreak = ClassBreak(
            description = "1001 to 1500",
            label = "1001 - 1500",
            minValue = 1001.0,
            maxValue = 1500.0,
            symbol = thirdClassSymbol
        )
        val fourthClassBreak = ClassBreak(
            description = "1501 to 2000",
            label = "1501 - 2000",
            minValue = 1501.0,
            maxValue = 2000.0,
            symbol = fourthClassSymbol
        )
        val fifthClassBreak = ClassBreak(
            description = "2001 to 2300",
            label = "2001 - 2300",
            minValue = 2001.0,
            maxValue = 2300.0,
            symbol = fifthClassSymbol
        )
        val elevationBreaks = listOf(
            firstClassBreak,
            secondClassBreak,
            thirdClassBreak,
            fourthClassBreak,
            fifthClassBreak
        )

        // create and assign class breaks renderer to the feature layer
        val elevationClassBreakRenderer = ClassBreaksRenderer("ELEV_GAIN", elevationBreaks)

        // create a trails feature layer
        val featureLayer = createFeatureLayer(featureServiceUri).apply {
            // style the feature layer using the class breaks renderer
            renderer = elevationClassBreakRenderer
            // set the layer opacity to semi-transparent
            opacity = 0.75f
        }

        return  featureLayer
    }

    private fun createBikeOnlyTrailsLayer(featureServiceUri: String): FeatureLayer {
        // create blue dot style simple line symbol to represent the bike trails
        val bikeTrailsSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Dot, Color.fromRgba(0, 0, 255), 2.0f)
        // create a simple renderer for the feature layer
        val bikeTrailRenderer = SimpleRenderer(bikeTrailsSymbol)

        // create a bike trails feature layer
        val featureLayer = createFeatureLayer(featureServiceUri).apply {
            // style the feature layer using the simple renderer
            renderer = bikeTrailRenderer
            // write a definition expression to filter for trails that permit the use of bikes
            definitionExpression = "USE_BIKE = 'Yes'"
        }

        return featureLayer
    }

    private fun createNoBikeTrailsLayer(featureServiceUri: String): FeatureLayer {
        // create a yellow dot style simple line symbol to represent no bike trails
        val noBikeTrailSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Dot, Color.fromRgba(255, 255, 0,), 2.0f)
        // create a simple renderer for the feature layer
        val noBikeTrailRenderer = SimpleRenderer(noBikeTrailSymbol)

        // create a no-bike trail feature layer
        val featureLayer = createFeatureLayer(featureServiceUri). apply {
            // style the feature layer using the simple renderer
            renderer = noBikeTrailRenderer
            // write a definition expression to filter for trails that don't permit the use od bikes
            definitionExpression = "USE_BIKE = 'No'"
        }

        return featureLayer
    }

    private fun createTrailheadsLayer(featureServiceUri: String, trailheadImage: String): FeatureLayer {
        // create a new picture marker symbol that uses the trailhead image
        val pictureMarkerSymbol = PictureMarkerSymbol(trailheadImage).apply {
            height = 18.0f
            width = 18.0f
        }
        // create a new simple renderer based on the picture marker symbol
        val simpleRenderer = SimpleRenderer(pictureMarkerSymbol)
        // create the label definition
        val trailHeadsDefinition = makeLabelDefinition(labelAttribute = "TRL_NAME")

        // create a trail heads feature layer
        val featureLayer: FeatureLayer = createFeatureLayer(featureServiceUri).apply {
            // style the feature layer using the simple renderer
            renderer = simpleRenderer
            // set labeling on the layer to be enabled
            labelsEnabled = true
            // add the label definition to the layers label definition collection
            labelDefinitions.add(trailHeadsDefinition)
        }

        return featureLayer
    }

    private fun makeLabelDefinition(labelAttribute: String): LabelDefinition {
        // create a text symbol for the label definition
        val labelTextSymbol = TextSymbol().apply {
            color = Color.white
            size = 12.0f
            haloColor = Color.red
            haloWidth = 1.0f
            fontFamily = "Arial"
            fontStyle = FontStyle.Italic
            fontWeight = FontWeight.Normal
        }

        // create an arcade label expression based on the field name
        val labelExpression = ArcadeLabelExpression("\$feature.$labelAttribute")

        // create and return the label definition
        return LabelDefinition(labelExpression, labelTextSymbol)
    }
    private fun createMap(): ArcGISMap {

        val parksAndOpenSpaces =
            "https://services3.arcgis.com/GVgbJbqm8hXASVYi/arcgis/rest/services/Parks_and_Open_Space/FeatureServer/0"
        val trails =
            "https://services3.arcgis.com/GVgbJbqm8hXASVYi/arcgis/rest/services/Trails/FeatureServer/0"
        val trailheads =
            "https://services3.arcgis.com/GVgbJbqm8hXASVYi/arcgis/rest/services/Trailheads/FeatureServer/0"
        val trailheadImage =
            "https://static.arcgis.com/images/Symbols/NPS/npsPictograph_0231b.png"

        return ArcGISMap(BasemapStyle.ArcGISTopographic).apply {
            initialViewpoint = Viewpoint(
                latitude = 34.0270,
                longitude = -118.8050,
                scale = 72000.0
            )
            operationalLayers.addAll(
                listOf(
                    createOpenSpaceLayer(parksAndOpenSpaces),
                    createTrailsLayer(trails),
                    createBikeOnlyTrailsLayer(trails),
                    createNoBikeTrailsLayer(trails),
                    createTrailheadsLayer(trailheads, trailheadImage)
                )
            )
        }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {

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
    }
}