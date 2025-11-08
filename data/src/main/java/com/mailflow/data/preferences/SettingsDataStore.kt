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
    }

    val todoListName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TODO_LIST_NAME] ?: DEFAULT_TODO_LIST_NAME
        }

    suspend fun setTodoListName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TODO_LIST_NAME] = name
        }
    }

    companion object {
        private const val DEFAULT_TODO_LIST_NAME = "inbox-test"
    }
}
