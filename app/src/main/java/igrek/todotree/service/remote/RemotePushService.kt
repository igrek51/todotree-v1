package igrek.todotree.service.remote

import igrek.todotree.domain.treeitem.RemoteTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.info.UiInfoService
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.service.tree.TreeManager
import igrek.todotree.ui.treelist.TreeListLayout
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
class RemotePushService(
    private val treeManager: LazyInject<TreeManager> = appFactory.treeManager,
    private val uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    private val remoteDbRequester: LazyInject<RemoteDbRequester> = appFactory.remoteDbRequester,
) {
    private val treeListLayout: TreeListLayout by LazyExtractor(appFactory.treeListLayout)

    private val remoteItemToId = hashMapOf<TextTreeItem, String>()

    fun pushNewItemAsync(content: String): Deferred<Result<String>> {
        if (content.isBlank()) {
            uiInfoService.get().showToast("Nothing to do")
            return GlobalScope.async { Result.success(content) }
        }
        GlobalScope.launch(Dispatchers.Main) {
            uiInfoService.get().showSnackbar("Pushing...")
        }

        return remoteDbRequester.get().createRemoteTodoAsync(content)
    }

    fun pushNewItemsAsync(contents: List<String>): Deferred<Result<Unit>> {
        if (contents.isEmpty()) {
            uiInfoService.get().showToast("Nothing to do")
            return GlobalScope.async { Result.success(Unit) }
        }
        GlobalScope.launch(Dispatchers.Main) {
            uiInfoService.get().showSnackbar("Pushing...")
        }

        return remoteDbRequester.get().createManyRemoteTodosAsync(contents)
    }

    fun populateRemoteItemAsync(item: RemoteTreeItem): Deferred<Result<List<TodoDto>>> {
        // clear current children
        repeat(item.children.size) {
            item.remove(0)
        }
        treeListLayout.updateItemsList()
        return GlobalScope.async {
            val dr = remoteDbRequester.get().fetchAllRemoteTodosAsync()
            val result = dr.await()
            result.onSuccess { todoDtos ->
                withContext(Dispatchers.Main) {
                    populateFetchedRemoteItemsId(item, todoDtos)
                }
            }
            result
        }
    }

    private fun populateFetchedRemoteItemsId(remoteItem: RemoteTreeItem, todoDtos: List<TodoDto>) {
        remoteItemToId.clear()
        val orderedTasks = todoDtos.sortedBy { it.create_timestamp }
        orderedTasks.forEach { todoTaskDto ->
            val newItem = TextTreeItem(todoTaskDto.content ?: "")
            remoteItem.add(newItem)
            todoTaskDto.id?.let { remoteItemToId[newItem] = it }
        }
        treeListLayout.updateItemsList()
    }

    fun removeRemoteItemAsync(position: Int): Deferred<Result<Unit>> {
        val item = treeManager.get().getChild(position)
        val itemId = remoteItemToId[item]
        itemId ?: run {
            return GlobalScope.async {
                Result.failure(RuntimeException("remote item ID not found"))
            }
        }
        return remoteDbRequester.get().deleteRemoteTodoAsync(itemId)
    }
}