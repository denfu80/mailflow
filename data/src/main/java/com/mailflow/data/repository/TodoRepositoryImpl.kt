package com.mailflow.data.repository

import com.mailflow.data.preferences.SettingsDataStore
import com.mailflow.data.remote.api.TodoApiService
import com.mailflow.data.remote.dto.AddTodoRequest
import com.mailflow.data.remote.dto.CreateListRequest
import com.mailflow.domain.repository.TodoListInfo
import com.mailflow.domain.repository.TodoRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val apiService: TodoApiService,
    private val settingsDataStore: SettingsDataStore
) : TodoRepository {

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

    override suspend fun createList(listName: String): Result<TodoListInfo> {
        return try {
            val request = CreateListRequest(name = listName)
            val response = apiService.createList(request)
            if (response.success && response.listId != null) {
                val listInfo = TodoListInfo(
                    listId = response.listId,
                    url = response.url
                )
                // Save to persistent storage
                settingsDataStore.setTodoListInfo(
                    listId = response.listId,
                    url = response.url
                )
                Result.success(listInfo)
            } else {
                Result.failure(Exception(response.error ?: "Failed to create list"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrCreateListInfo(listName: String): Result<TodoListInfo> {
        return try {
            // Check if we already have a stored list ID
            val storedListId = settingsDataStore.todoListId.first()
            if (storedListId != null) {
                val storedUrl = settingsDataStore.todoListUrl.first()
                return Result.success(TodoListInfo(listId = storedListId, url = storedUrl))
            }

            // If not, create a new list
            createList(listName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getOrCreateListId(listName: String): String {
        // Check if we have a stored list ID
        val storedListId = settingsDataStore.todoListId.first()
        if (storedListId != null) {
            return storedListId
        }

        // If not, create a new list and store the ID
        val request = CreateListRequest(name = listName)
        val response = apiService.createList(request)
        if (response.success && response.listId != null) {
            settingsDataStore.setTodoListInfo(
                listId = response.listId,
                url = response.url
            )
            return response.listId
        } else {
            throw Exception(response.error ?: "Failed to create list")
        }
    }
}
