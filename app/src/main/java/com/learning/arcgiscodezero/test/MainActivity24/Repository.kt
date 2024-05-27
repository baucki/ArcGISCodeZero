package com.learning.arcgiscodezero.test.MainActivity24

import com.arcgismaps.data.Feature
import com.arcgismaps.data.FeatureType
import com.arcgismaps.data.Field
import com.arcgismaps.mapping.layers.FeatureLayer

object Repository {
    var featureLayer: FeatureLayer? = null
    var feature: Feature? = null

    var featureLayerList: FeatureLayer? = null
    var fields: List<Field?> = emptyList()
    var types: List<FeatureType?> = emptyList()
}