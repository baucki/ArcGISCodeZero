package com.learning.arcgiscodezero.test.test24

import com.arcgismaps.data.Feature
import com.arcgismaps.data.FeatureType
import com.arcgismaps.data.Field
import com.arcgismaps.data.FieldType
import com.arcgismaps.mapping.layers.FeatureLayer

object Repository {
    var featureLayer: FeatureLayer? = null
    var feature: Feature? = null

    var featureLayerList: MutableList<FeatureLayer?> = mutableListOf()
    var codedValuesList: ArrayList<String> = ArrayList()
    var fieldInfoList: MutableList<FieldInfo> = mutableListOf()
    var fields: List<Field?> = emptyList()
    var types: List<FeatureType?> = emptyList()
    val fieldTypeMap = mapOf(
        FieldType.Text to "Text",
        FieldType.Int16 to "Short",
        FieldType.Int32 to "Integer",
        FieldType.Int64 to "Long",
        FieldType.Float32 to "Float",
        FieldType.Float64 to "Double",
        FieldType.Date to "Date",
        FieldType.DateOnly to "DateOnly",
        FieldType.Oid to "OID",
        FieldType.Geometry to "Geometry",
        FieldType.GlobalId to "GlobalId",
        FieldType.Blob to "Blob",
        FieldType.Raster to "Raster",
        FieldType.Guid to "GUID",
        FieldType.Xml to "XML"
    )
    var typeObject: String = ""
    var dataTypeObject: String = ""
    var selectedKey: String = ""
    var typeObjectNamesMap: MutableMap<Any, String> = mutableMapOf()
    var typeObjectIdMap: MutableMap<String, Any> = mutableMapOf()
    data class FieldInfo(
        val id: Int,
        val name: String,
        val value: Any?,
        val type: String
    )
}