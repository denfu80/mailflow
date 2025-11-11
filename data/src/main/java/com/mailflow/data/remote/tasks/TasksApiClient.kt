package com.mailflow.data.remote.tasks

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.tasks.Tasks
import com.google.api.services.tasks.TasksScopes
import com.google.api.services.tasks.model.Task
import com.google.api.services.tasks.model.TaskList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksApiClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tasksService: Tasks? = null

    companion object {
        private const val TAG = "TasksApiClient"
    }

    fun getSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(TasksScopes.TASKS))
            .build()
    }

    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && hasTasksScope(account)
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    private fun hasTasksScope(account: GoogleSignInAccount): Boolean {
        return account.grantedScopes.contains(Scope(TasksScopes.TASKS))
    }

    fun initializeService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(TasksScopes.TASKS)
        ).apply {
            selectedAccount = account.account
        }

        tasksService = Tasks.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("MailFlow")
            .build()
    }

    /**
     * Lists all task lists for the authenticated user
     */
    suspend fun listTaskLists(): Result<List<TasksClient.GoogleTaskList>> = withContext(Dispatchers.IO) {
        try {
            var service = tasksService

            if (service == null) {
                Log.d(TAG, "Tasks service is null, attempting to re-initialize")
                val account = getSignedInAccount()
                if (account != null) {
                    Log.d(TAG, "Found signed-in account: ${account.email}")
                    initializeService(account)
                    service = tasksService
                } else {
                    Log.w(TAG, "No signed-in account found")
                }
            }

            if (service == null) {
                Log.e(TAG, "Tasks service could not be initialized")
                return@withContext Result.failure(IllegalStateException("Tasks service not initialized. Please sign in first."))
            }

            Log.d(TAG, "Fetching task lists")
            val taskListsResponse = service.tasklists()
                .list()
                .execute()

            val taskLists = taskListsResponse.items?.map { taskList ->
                TasksClient.GoogleTaskList(
                    id = taskList.id,
                    title = taskList.title ?: "Untitled",
                    updated = taskList.updated?.value ?: System.currentTimeMillis()
                )
            } ?: emptyList()

            Log.d(TAG, "Successfully fetched ${taskLists.size} task lists")
            Result.success(taskLists)
        } catch (e: Exception) {
            Log.e(TAG, "Error in listTaskLists: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Lists tasks from a specific task list
     */
    suspend fun listTasks(taskListId: String): Result<List<TasksClient.GoogleTask>> = withContext(Dispatchers.IO) {
        try {
            var service = tasksService

            if (service == null) {
                val account = getSignedInAccount()
                if (account != null) {
                    initializeService(account)
                    service = tasksService
                }
            }

            if (service == null) {
                return@withContext Result.failure(IllegalStateException("Tasks service not initialized"))
            }

            Log.d(TAG, "Fetching tasks from list: $taskListId")
            val tasksResponse = service.tasks()
                .list(taskListId)
                .execute()

            val tasks = tasksResponse.items?.map { task ->
                TasksClient.GoogleTask(
                    id = task.id,
                    title = task.title ?: "",
                    notes = task.notes,
                    status = task.status,
                    due = task.due?.value,
                    completed = task.completed?.value,
                    updated = task.updated?.value ?: System.currentTimeMillis()
                )
            } ?: emptyList()

            Log.d(TAG, "Successfully fetched ${tasks.size} tasks")
            Result.success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "Error in listTasks: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Creates a new task in the specified task list
     */
    suspend fun insertTask(
        taskListId: String,
        title: String,
        notes: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            var service = tasksService

            if (service == null) {
                val account = getSignedInAccount()
                if (account != null) {
                    initializeService(account)
                    service = tasksService
                }
            }

            if (service == null) {
                return@withContext Result.failure(IllegalStateException("Tasks service not initialized"))
            }

            Log.d(TAG, "Creating task in list $taskListId: $title")
            val task = Task().apply {
                this.title = title
                this.notes = notes
            }

            val insertedTask = service.tasks()
                .insert(taskListId, task)
                .execute()

            Log.d(TAG, "Successfully created task with ID: ${insertedTask.id}")
            Result.success(insertedTask.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error in insertTask: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Updates an existing task
     */
    suspend fun updateTask(
        taskListId: String,
        taskId: String,
        title: String? = null,
        notes: String? = null,
        status: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            var service = tasksService

            if (service == null) {
                val account = getSignedInAccount()
                if (account != null) {
                    initializeService(account)
                    service = tasksService
                }
            }

            if (service == null) {
                return@withContext Result.failure(IllegalStateException("Tasks service not initialized"))
            }

            // First, get the current task
            val currentTask = service.tasks()
                .get(taskListId, taskId)
                .execute()

            // Update only the fields that are provided
            currentTask.apply {
                if (title != null) this.title = title
                if (notes != null) this.notes = notes
                if (status != null) this.status = status
            }

            service.tasks()
                .update(taskListId, taskId, currentTask)
                .execute()

            Log.d(TAG, "Successfully updated task: $taskId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateTask: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a task
     */
    suspend fun deleteTask(taskListId: String, taskId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            var service = tasksService

            if (service == null) {
                val account = getSignedInAccount()
                if (account != null) {
                    initializeService(account)
                    service = tasksService
                }
            }

            if (service == null) {
                return@withContext Result.failure(IllegalStateException("Tasks service not initialized"))
            }

            service.tasks()
                .delete(taskListId, taskId)
                .execute()

            Log.d(TAG, "Successfully deleted task: $taskId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error in deleteTask: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        tasksService = null
    }
}
