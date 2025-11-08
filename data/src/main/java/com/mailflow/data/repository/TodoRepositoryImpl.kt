package com.mailflow.data.repository

import com.mailflow.data.remote.api.TodoApiService
import com.mailflow.data.remote.dto.AddTodoRequest
import com.mailflow.domain.repository.TodoRepository
import javax.inject.Inject

class TodoRepositoryImpl @Inject constructor(
    private val apiService: TodoApiService
) : TodoRepository {
    override suspend fun addTodo(listName: String, todoText: String): Result<Unit> {
        return try {
            val request = AddTodoRequest(text = todoText)
            val response = apiService.addTodo(listId = listName, request = request)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error ?: "Unknown API error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

