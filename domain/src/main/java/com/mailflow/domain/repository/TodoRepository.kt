package com.mailflow.domain.repository

data class TodoListInfo(
    val listId: String,
    val url: String?
)

interface TodoRepository {
    suspend fun addTodo(listName: String, todoText: String): Result<Unit>
    suspend fun createList(listName: String): Result<TodoListInfo>
    suspend fun getOrCreateListInfo(listName: String): Result<TodoListInfo>
}
