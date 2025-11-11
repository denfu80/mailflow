package com.mailflow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateListRequest(
    @SerializedName("name") val name: String,
    @SerializedName("creatorName") val creatorName: String = "MailFlow"
)
