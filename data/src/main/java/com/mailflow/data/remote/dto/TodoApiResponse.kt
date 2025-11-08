package com.mailflow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AddTodoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("todoId") val todoId: String?,
    @SerializedName("error") val error: String?
)
