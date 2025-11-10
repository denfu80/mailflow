package com.mailflow.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mailflow.domain.model.TodoBackendType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val TODO_LIST_NAME = stringPreferencesKey("todo_list_name")
        val TODO_BACKEND_TYPE = stringPreferencesKey("todo_backend_type")
        val GOOGLE_TASKS_LIST_NAME = stringPreferencesKey("google_tasks_list_name")
    }

    val todoListName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TODO_LIST_NAME] ?: DEFAULT_TODO_LIST_NAME
        }

    val todoBackendType: Flow<TodoBackendType> = context.dataStore.data
        .map { preferences ->
            val typeString = preferences[PreferencesKeys.TODO_BACKEND_TYPE] ?: DEFAULT_BACKEND_TYPE.name
            try {
                TodoBackendType.valueOf(typeString)
            } catch (e: IllegalArgumentException) {
                DEFAULT_BACKEND_TYPE
            }
        }

    val googleTasksListName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.GOOGLE_TASKS_LIST_NAME] ?: DEFAULT_GOOGLE_TASKS_LIST_NAME
        }

    suspend fun setTodoListName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TODO_LIST_NAME] = name
        }
    }

    suspend fun setTodoBackendType(type: TodoBackendType) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TODO_BACKEND_TYPE] = type.name
        }
    }

    suspend fun setGoogleTasksListName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GOOGLE_TASKS_LIST_NAME] = name
        }
    }

    companion object {
        private const val DEFAULT_TODO_LIST_NAME = "inbox-test"
        private const val DEFAULT_GOOGLE_TASKS_LIST_NAME = "My Tasks"
        private val DEFAULT_BACKEND_TYPE = TodoBackendType.EXTERNAL_API
    }
}
