package com.mailflow.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
        val TODO_LIST_ID = stringPreferencesKey("todo_list_id")
        val TODO_LIST_URL = stringPreferencesKey("todo_list_url")
    }

    val todoListName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TODO_LIST_NAME] ?: DEFAULT_TODO_LIST_NAME
        }

    val todoListId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TODO_LIST_ID]
        }

    val todoListUrl: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TODO_LIST_URL]
        }

    suspend fun setTodoListName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TODO_LIST_NAME] = name
        }
    }

    suspend fun setTodoListId(listId: String?) {
        context.dataStore.edit { preferences ->
            if (listId != null) {
                preferences[PreferencesKeys.TODO_LIST_ID] = listId
            } else {
                preferences.remove(PreferencesKeys.TODO_LIST_ID)
            }
        }
    }

    suspend fun setTodoListUrl(url: String?) {
        context.dataStore.edit { preferences ->
            if (url != null) {
                preferences[PreferencesKeys.TODO_LIST_URL] = url
            } else {
                preferences.remove(PreferencesKeys.TODO_LIST_URL)
            }
        }
    }

    suspend fun setTodoListInfo(listId: String?, url: String?) {
        context.dataStore.edit { preferences ->
            if (listId != null) {
                preferences[PreferencesKeys.TODO_LIST_ID] = listId
            } else {
                preferences.remove(PreferencesKeys.TODO_LIST_ID)
            }

            if (url != null) {
                preferences[PreferencesKeys.TODO_LIST_URL] = url
            } else {
                preferences.remove(PreferencesKeys.TODO_LIST_URL)
            }
        }
    }

    companion object {
        private const val DEFAULT_TODO_LIST_NAME = "inbox-test"
    }
}
