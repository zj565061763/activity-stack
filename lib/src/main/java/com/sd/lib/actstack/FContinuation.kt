package com.sd.lib.actstack

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class FContinuation<T> {
    private val _continuationHolder: MutableList<CancellableContinuation<T>> = Collections.synchronizedList(mutableListOf())

    suspend fun await(): T {
        return suspendCancellableCoroutine { cont ->
            _continuationHolder.add(cont)
            cont.invokeOnCancellation {
                _continuationHolder.remove(cont)
            }
        }
    }

    fun resume(value: T) {
        foreach {
            it.resume(value)
        }
    }

    fun resumeWithException(exception: Throwable) {
        foreach {
            it.resumeWithException(exception)
        }
    }

    fun cancel(cause: Throwable? = null) {
        foreach(remove = false) {
            it.cancel(cause)
        }
    }

    fun size(): Int {
        return _continuationHolder.size
    }

    private fun foreach(remove: Boolean = true, block: (CancellableContinuation<T>) -> Unit) {
        while (_continuationHolder.isNotEmpty()) {
            val copyHolder = _continuationHolder.toList()
            copyHolder.forEach {
                try {
                    block(it)
                } finally {
                    if (remove) {
                        _continuationHolder.remove(it)
                    }
                }
            }
        }
    }
}