package igrek.todotree.service.remote

import kotlinx.serialization.Serializable

@Serializable
data class TodoDto(
        var id: Long? = null,
        var content: String? = null,
        var create_timestamp: Long? = null,
        var device_id: String? = null,
)

@Serializable
data class AllTodoDto(
        var entities: List<TodoDto> = emptyList()
)

@Serializable
data class CreateTodosDto(
        var todos: MutableList<TodoDto> = mutableListOf()
)
