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
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
class RemoteDbRequester (
    activity: LazyInject<Activity> = appFactory.activityMust,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
) {
    private val activity by LazyExtractor(activity)

    companion object {
        private const val todoApiBase = "https://todo.igrek.dev/api/v1"

        private const val getAllTodosUrl = "$todoApiBase/todo"
        private const val postNewTodoUrl = "$todoApiBase/todo"
        private const val postNewTodosUrl = "$todoApiBase/todos"
        private val deleteTodoUrl = { id: Long -> "$todoApiBase/todo/$id" }

        private const val authTokenHeader = "X-Auth-Token"
    }

    private val httpRequester = HttpRequester()
    private val jsonType = MediaType.parse("application/json; charset=utf-8")
    private val jsonSerializer = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
        isLenient = false
        allowStructuredMapKeys = true
        prettyPrint = false
        useArrayPolymorphism = false
    }

    private var authToken = ""

    init {
        authToken = settingsState.get().userAuthToken
    }

    fun fetchAllRemoteTodos(): Deferred<Result<List<TodoDto>>> {
        val request: Request = Request.Builder()
                .url(getAllTodosUrl)
                .addHeader(authTokenHeader, authToken)
                .build()
        return httpRequester.httpRequestAsync(request) { response: Response ->
            val json = response.body()?.string() ?: ""
            val allDtos: AllTodoDto = jsonSerializer.decodeFromString(AllTodoDto.serializer(), json)
            allDtos.entities
        }
    }

    @SuppressLint("HardwareIds")
    fun createRemoteTodo(content: String): Deferred<Result<String>> {
        logger.info("Creating remote todo")
        val deviceId = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
        val timestampS = Date().time / 1000
        val todoDto = TodoDto(content = content, create_timestamp = timestampS, device_id = deviceId)
        val json = jsonSerializer.encodeToString(TodoDto.serializer(), todoDto)
        val request: Request = Request.Builder()
                .url(postNewTodoUrl)
                .post(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, authToken)
                .build()
        return httpRequester.httpRequestAsync(request) { content }
    }

    @SuppressLint("HardwareIds")
    fun createManyRemoteTodos(contents: List<String>): Deferred<Result<Unit>> {
        logger.info("Creating remote todos")
        val deviceId = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
        val timestampS = Date().time / 1000

        val todosDto = CreateTodosDto()
        contents.forEach { content ->
            val todoDto = TodoDto(content = content, create_timestamp = timestampS, device_id = deviceId)
            todosDto.todos.add(todoDto)
        }

        val json = jsonSerializer.encodeToString(CreateTodosDto.serializer(), todosDto)
        val request: Request = Request.Builder()
                .url(postNewTodosUrl)
                .post(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, authToken)
                .build()
        return httpRequester.httpRequestAsync(request) { }
    }

    fun deleteRemoteTodo(id: Long): Deferred<Result<Unit>> {
        logger.info("Deleting remote todo")
        val request: Request = Request.Builder()
                .url(deleteTodoUrl(id))
                .delete()
                .addHeader(authTokenHeader, authToken)
                .build()
        return GlobalScope.async {
            val dr = httpRequester.httpRequestAsync(request) { true }
            dr.await().map { }
        }
    }

}