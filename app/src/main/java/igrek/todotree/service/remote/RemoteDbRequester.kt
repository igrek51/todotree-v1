package igrek.todotree.service.remote

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.Settings
import igrek.todotree.info.logger.LoggerFactory.logger
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.settings.SettingsState
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
class RemoteDbRequester (
    private val settingsState: LazyInject<SettingsState> = appFactory.settingsState,
) {
    private val activity: Activity by LazyExtractor(appFactory.activity)

    companion object {
        private const val todoApiBase = "https://todo.igrek.dev/api/v1"
        private const val authTokenHeader = "X-Auth-Token"
    }

    private val httpRequester = HttpRequester()
    private val jsonType: MediaType = "application/json; charset=utf-8".toMediaType()
    private val jsonSerializer = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
        isLenient = false
        allowStructuredMapKeys = true
        prettyPrint = false
        useArrayPolymorphism = false
    }

    private val authToken
        get() = settingsState.get().userAuthToken

    fun fetchAllRemoteTodosAsync(): Deferred<Result<List<TodoDto>>> {
        val request: Request = Request.Builder()
                .url("$todoApiBase/todo")
                .addHeader(authTokenHeader, authToken)
                .build()
        return httpRequester.httpRequestAsync(request) { response: Response ->
            val json = response.body?.string() ?: ""
            val allDtos: List<TodoDto> = jsonSerializer.decodeFromString(ListSerializer(TodoDto.serializer()), json)
            allDtos
        }
    }

    @SuppressLint("HardwareIds")
    fun createRemoteTodoAsync(content: String): Deferred<Result<String>> {
        logger.info("Creating remote todo")
        val deviceId = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
        val timestampS = Date().time / 1000
        val todoDto = TodoDto(content = content, create_timestamp = timestampS, device_id = deviceId)
        val json = jsonSerializer.encodeToString(TodoDto.serializer(), todoDto)
        val request: Request = Request.Builder()
                .url("$todoApiBase/todo")
                .post(json.toRequestBody(jsonType))
                .addHeader(authTokenHeader, authToken)
                .build()
        return httpRequester.httpRequestAsync(request) { content }
    }

    @SuppressLint("HardwareIds")
    fun createManyRemoteTodosAsync(contents: List<String>): Deferred<Result<Unit>> {
        logger.info("Creating remote todos")
        val deviceId = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
        val timestampS = Date().time / 1000

        val tasks = contents.map { content ->
            TodoDto(content = content, create_timestamp = timestampS, device_id = deviceId)
        }

        val json = jsonSerializer.encodeToString(ListSerializer(TodoDto.serializer()), tasks)
        val request: Request = Request.Builder()
                .url("$todoApiBase/todo/many")
                .post(json.toRequestBody(jsonType))
                .addHeader(authTokenHeader, authToken)
                .build()
        return httpRequester.httpRequestAsync(request) { }
    }

    fun deleteRemoteTodoAsync(id: String): Deferred<Result<Unit>> {
        logger.info("Deleting remote todo")
        val request: Request = Request.Builder()
                .url("$todoApiBase/todo/$id")
                .delete()
                .addHeader(authTokenHeader, authToken)
                .build()
        return GlobalScope.async {
            val dr = httpRequester.httpRequestAsync(request) { true }
            dr.await().map { }
        }
    }

}