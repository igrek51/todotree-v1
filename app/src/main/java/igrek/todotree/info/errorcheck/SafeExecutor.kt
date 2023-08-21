package igrek.todotree.info.errorcheck

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SafeExecutor(
        action: () -> Unit,
) {

    init {
        execute(action)
    }

    private fun execute(action: () -> Unit) {
        try {
            action.invoke()
        } catch (t: Throwable) {
            UiErrorHandler().handleError(t)
        }
    }

}

inline fun safeExecute(block: () -> Unit) {
    try {
        block()
    } catch (t: Throwable) {
        UiErrorHandler().handleError(t)
    }
}

fun safeAsyncExecutor(block: suspend () -> Unit): () -> Unit = {
    GlobalScope.launch {
        try {
            block()
        } catch (t: Throwable) {
            UiErrorHandler().handleError(t)
        }
    }
}
