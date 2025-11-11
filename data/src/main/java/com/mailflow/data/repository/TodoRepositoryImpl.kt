package com.mailflow.data.repository

import com.mailflow.data.remote.api.TodoApiService
import com.mailflow.data.remote.dto.AddTodoRequest
import com.mailflow.data.remote.dto.CreateListRequest
import com.mailflow.domain.repository.TodoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val apiService: TodoApiService
) : TodoRepository {
    private val listIdCache = mutableMapOf<String, String>()

    override suspend fun addTodo(listName: String, todoText: String): Result<Unit> {
        return try {
            val listId = getOrCreateListId(listName)
            val request = AddTodoRequest(text = todoText)
            val response = apiService.addTodo(listId = listId, request = request)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error ?: "Unknown API error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getOrCreateListId(listName: String): String {
        if (listIdCache.containsKey(listName)) {
            return listIdCache.getValue(listName)
        }

        val request = CreateListRequest(name = listName)
        val response = apiService.createList(request)
        if (response.success && response.listId != null) {
            listIdCache[listName] = response.listId
            return response.listId
        } else {
            throw Exception(response.error ?: "Failed to create list")
        }
    }
}
