package com.mailflow.core.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max

class RateLimiter(
    private val maxRequests: Int,
    private val windowMs: Long
) {
    private val mutex = Mutex()
    private val requestTimestamps = mutableListOf<Long>()

    suspend fun acquire() {
        mutex.withLock {
            val now = System.currentTimeMillis()

            requestTimestamps.removeAll { it < now - windowMs }

            if (requestTimestamps.size >= maxRequests) {
                val oldestRequest = requestTimestamps.first()
                val waitTime = (oldestRequest + windowMs) - now

                if (waitTime > 0) {
                    delay(waitTime)
                    requestTimestamps.removeAll { it < System.currentTimeMillis() - windowMs }
                }
            }

            requestTimestamps.add(System.currentTimeMillis())
        }
    }

    suspend fun reset() {
        mutex.withLock {
            requestTimestamps.clear()
        }
    }

    suspend fun getRemainingRequests(): Int {
        return mutex.withLock {
            val now = System.currentTimeMillis()
            requestTimestamps.removeAll { it < now - windowMs }
            max(0, maxRequests - requestTimestamps.size)
        }
    }

    companion object {
        fun perMinute(requests: Int): RateLimiter {
            return RateLimiter(requests, 60_000L)
        }

        fun perHour(requests: Int): RateLimiter {
            return RateLimiter(requests, 3_600_000L)
        }
    }
}
