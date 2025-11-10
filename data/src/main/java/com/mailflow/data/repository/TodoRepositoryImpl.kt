package com.mailflow.data.repository

import android.util.Log
import com.mailflow.data.preferences.SettingsDataStore
import com.mailflow.data.remote.api.TodoApiService
import com.mailflow.data.remote.dto.AddTodoRequest
import com.mailflow.data.remote.tasks.TasksClient
import com.mailflow.domain.model.TodoBackendType
import com.mailflow.domain.repository.TodoRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TodoRepositoryImpl @Inject constructor(
    private val apiService: TodoApiService,
    private val tasksClient: TasksClient,
    private val settingsDataStore: SettingsDataStore
) : TodoRepository {

    companion object {
        private const val TAG = "TodoRepositoryImpl"
    }

    override suspend fun addTodo(listName: String, todoText: String): Result<Unit> {
        // Get the configured backend type
        val backendType = settingsDataStore.todoBackendType.first()

        Log.d(TAG, "Adding todo using backend: $backendType")

        return when (backendType) {
            TodoBackendType.EXTERNAL_API -> addTodoToExternalApi(listName, todoText)
            TodoBackendType.GOOGLE_TASKS -> addTodoToGoogleTasks(listName, todoText)
        }
    }

    private suspend fun addTodoToExternalApi(listName: String, todoText: String): Result<Unit> {
        return try {
            Log.d(TAG, "Adding todo to external API: $listName")
            val request = AddTodoRequest(text = todoText)
            val response = apiService.addTodo(listId = listName, request = request)
            if (response.success) {
                Log.d(TAG, "Successfully added todo to external API")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to add todo to external API: ${response.error}")
                Result.failure(Exception(response.error ?: "Unknown API error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding todo to external API", e)
            Result.failure(e)
        }
    }

    private suspend fun addTodoToGoogleTasks(listName: String, todoText: String): Result<Unit> {
        return try {
            Log.d(TAG, "Adding todo to Google Tasks: $listName")
            // Use the configured Google Tasks list name
            val googleTasksListName = settingsDataStore.googleTasksListName.first()
            val taskIdResult = tasksClient.createTaskInList(
                listName = googleTasksListName,
                title = todoText
            )

            if (taskIdResult.isSuccess) {
                val taskId = taskIdResult.getOrNull()
                Log.d(TAG, "Successfully added todo to Google Tasks with ID: $taskId")
                Result.success(Unit)
            } else {
                val error = taskIdResult.exceptionOrNull()
                Log.e(TAG, "Failed to add todo to Google Tasks", error)
                Result.failure(error ?: Exception("Failed to create task"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding todo to Google Tasks", e)
            Result.failure(e)
        }
    }
}

