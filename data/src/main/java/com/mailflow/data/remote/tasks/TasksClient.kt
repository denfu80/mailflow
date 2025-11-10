package com.mailflow.data.remote.tasks

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-level client for Google Tasks API
 * Provides simplified methods for task management
 */
@Singleton
class TasksClient @Inject constructor(
    private val tasksApiClient: TasksApiClient
) {
    companion object {
        private const val TAG = "TasksClient"
    }

    /**
     * Data class representing a Google Task List
     */
    data class GoogleTaskList(
        val id: String,
        val title: String,
        val updated: Long
    )

    /**
     * Data class representing a Google Task
     */
    data class GoogleTask(
        val id: String,
        val title: String,
        val notes: String?,
        val status: String?,
        val due: Long?,
        val completed: Long?,
        val updated: Long
    )

    /**
     * Checks if user is authenticated with Tasks API
     */
    fun isAuthenticated(): Boolean {
        return tasksApiClient.isSignedIn()
    }

    /**
     * Gets all task lists for the authenticated user
     */
    suspend fun getTaskLists(): Result<List<GoogleTaskList>> {
        if (!isAuthenticated()) {
            Log.w(TAG, "User not authenticated for Tasks API")
            return Result.failure(IllegalStateException("Not authenticated. Please sign in first."))
        }

        return tasksApiClient.listTaskLists()
    }

    /**
     * Gets tasks from a specific task list
     */
    suspend fun getTasks(taskListId: String): Result<List<GoogleTask>> {
        if (!isAuthenticated()) {
            return Result.failure(IllegalStateException("Not authenticated. Please sign in first."))
        }

        return tasksApiClient.listTasks(taskListId)
    }

    /**
     * Creates a new task in the specified task list
     */
    suspend fun createTask(
        taskListId: String,
        title: String,
        notes: String? = null
    ): Result<String> {
        if (!isAuthenticated()) {
            return Result.failure(IllegalStateException("Not authenticated. Please sign in first."))
        }

        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Task title cannot be blank"))
        }

        return tasksApiClient.insertTask(taskListId, title, notes)
    }

    /**
     * Finds a task list by name (case-insensitive)
     * Returns the task list ID if found, null otherwise
     */
    suspend fun findTaskListByName(name: String): Result<String?> {
        val taskListsResult = getTaskLists()
        if (taskListsResult.isFailure) {
            return Result.failure(taskListsResult.exceptionOrNull() ?: Exception("Failed to get task lists"))
        }

        val taskLists = taskListsResult.getOrNull() ?: emptyList()
        val taskList = taskLists.find { it.title.equals(name, ignoreCase = true) }

        return Result.success(taskList?.id)
    }

    /**
     * Convenience method to create a task in a task list by name
     * If the task list doesn't exist, it will use the first available task list
     */
    suspend fun createTaskInList(
        listName: String,
        title: String,
        notes: String? = null
    ): Result<String> {
        // First try to find the list by name
        val taskListIdResult = findTaskListByName(listName)
        if (taskListIdResult.isFailure) {
            return Result.failure(taskListIdResult.exceptionOrNull() ?: Exception("Failed to find task list"))
        }

        var taskListId = taskListIdResult.getOrNull()

        // If not found, use the first available task list
        if (taskListId == null) {
            Log.w(TAG, "Task list '$listName' not found, using first available task list")
            val taskListsResult = getTaskLists()
            if (taskListsResult.isFailure) {
                return Result.failure(taskListsResult.exceptionOrNull() ?: Exception("Failed to get task lists"))
            }

            val taskLists = taskListsResult.getOrNull()
            if (taskLists.isNullOrEmpty()) {
                return Result.failure(IllegalStateException("No task lists found. Please create a task list first."))
            }

            taskListId = taskLists.first().id
            Log.d(TAG, "Using task list: ${taskLists.first().title}")
        }

        return createTask(taskListId, title, notes)
    }

    /**
     * Updates an existing task
     */
    suspend fun updateTask(
        taskListId: String,
        taskId: String,
        title: String? = null,
        notes: String? = null,
        completed: Boolean? = null
    ): Result<Unit> {
        if (!isAuthenticated()) {
            return Result.failure(IllegalStateException("Not authenticated. Please sign in first."))
        }

        val status = when (completed) {
            true -> "completed"
            false -> "needsAction"
            null -> null
        }

        return tasksApiClient.updateTask(taskListId, taskId, title, notes, status)
    }

    /**
     * Marks a task as completed
     */
    suspend fun completeTask(taskListId: String, taskId: String): Result<Unit> {
        return updateTask(taskListId, taskId, completed = true)
    }

    /**
     * Deletes a task
     */
    suspend fun deleteTask(taskListId: String, taskId: String): Result<Unit> {
        if (!isAuthenticated()) {
            return Result.failure(IllegalStateException("Not authenticated. Please sign in first."))
        }

        return tasksApiClient.deleteTask(taskListId, taskId)
    }
}
