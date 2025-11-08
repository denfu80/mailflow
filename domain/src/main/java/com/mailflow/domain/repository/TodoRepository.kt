package com.mailflow.domain.repository

interface TodoRepository {
    suspend fun addTodo(listName: String, todoText: String): Result<Unit>
}
