package com.mailflow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AddTodoRequest(
    @SerializedName("text") val text: String,
    @SerializedName("creatorName") val creatorName: String = "MailFlow"
)
