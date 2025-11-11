package com.mailflow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("listId") val listId: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("error") val error: String? = null
)
