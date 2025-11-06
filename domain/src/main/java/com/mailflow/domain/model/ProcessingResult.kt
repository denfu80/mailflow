package com.mailflow.domain.model

sealed class ProcessingResult<out T> {
    data class Success<T>(val data: T) : ProcessingResult<T>()
    data class Error(val error: ProcessingError) : ProcessingResult<Nothing>()
    data object Loading : ProcessingResult<Nothing>()
}

data class ProcessingError(
    val type: ErrorType,
    val message: String,
    val cause: Throwable? = null
)

enum class ErrorType {
    NETWORK_ERROR,
    API_ERROR,
    VALIDATION_ERROR,
    DATABASE_ERROR,
    AUTHENTICATION_ERROR,
    PARSING_ERROR,
    UNKNOWN_ERROR
}

sealed class SyncResult {
    data class Success(
        val messagesFetched: Int,
        val messagesProcessed: Int,
        val errors: Int = 0
    ) : SyncResult()

    data class PartialSuccess(
        val messagesFetched: Int,
        val messagesProcessed: Int,
        val errors: Int,
        val errorMessages: List<String>
    ) : SyncResult()

    data class Failure(val error: ProcessingError) : SyncResult()
}

data class ProcessingJobResult(
    val jobId: String,
    val status: ProcessingStatus,
    val processedCount: Int,
    val failedCount: Int,
    val startTime: Long,
    val endTime: Long?,
    val error: ProcessingError? = null
)

enum class ProcessingStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
