package com.jefferson.antenas.data.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.jefferson.antenas.data.model.FlexibleStringAdapter

data class Banner(
    @JsonAdapter(FlexibleStringAdapter::class)
    @SerializedName(value = "id", alternate = ["_id"]) val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName(value = "imageUrl", alternate = ["image_url"]) val imageUrl: String,
    @SerializedName(value = "actionText", alternate = ["action_text"]) val actionText: String
)