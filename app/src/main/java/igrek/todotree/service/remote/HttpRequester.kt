package igrek.todotree.service.remote

import igrek.todotree.info.logger.LoggerFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class HttpRequester {

    private val okHttpClient: OkHttpClient = OkHttpClient()
    private val logger = LoggerFactory.logger

    fun <T> httpRequestAsync(request: Request, successor: (Response) -> T): Deferred<Result<T>> {
        return GlobalScope.async {
            httpRequestSync(request, successor)
        }
    }

    private fun <T> httpRequestSync(request: Request, successor: (Response) -> T): Result<T> {
        try {

            val response: Response = okHttpClient.newCall(request).execute()
            return if (!response.isSuccessful) {
                logger.error("Unexpected response code: $response, ${response.body().toString()}")
                Result.failure(RuntimeException(response.toString()))
            } else {
                try {
                    logger.debug("Successful http response")
                    val responseData = successor(response)
                    Result.success(responseData)
                } catch (e: Throwable) {
                    logger.error("onResponse error: ${e.message}", e)
                    Result.failure(RuntimeException(e.message))
                }
            }

        } catch (e: java.io.IOException) {
            logger.error("Request sending error: ${e.message}", e)
            return Result.failure(RuntimeException(e.message))
        }
    }

}