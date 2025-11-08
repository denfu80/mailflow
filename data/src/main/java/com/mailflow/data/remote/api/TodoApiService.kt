package com.mailflow.data.remote.api

import com.mailflow.data.remote.dto.AddTodoRequest
import com.mailflow.data.remote.dto.AddTodoResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface TodoApiService {
    @POST("lists/{listId}/todos")
    suspend fun addTodo(
        @Path("listId") listId: String,
        @Body request: AddTodoRequest
    ): AddTodoResponse
}
