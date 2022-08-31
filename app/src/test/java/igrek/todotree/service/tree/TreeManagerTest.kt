package igrek.todotree.service.tree

import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.inject.SingletonInject
import igrek.todotree.inject.appFactory
import igrek.todotree.intent.ClipboardCommand
import igrek.todotree.intent.TreeCommand
import igrek.todotree.mock.SettingsServiceMock
import igrek.todotree.mock.SystemClipboardManagerMock
import igrek.todotree.service.access.DatabaseLock
import igrek.todotree.service.tree.persistence.TreePersistenceService
import igrek.todotree.settings.SettingsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class TreeManagerTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun testLinkPasting() = runBlocking(Dispatchers.Main) {
        val treeManager = TreeManager()
        val treePersistenceService = TreePersistenceService()
        val settingsService = SettingsServiceMock()
        val settingsState = SettingsState(settingsService = SingletonInject { settingsService })
        val systemClipboardManager = SystemClipboardManagerMock()
        val databaseLock = DatabaseLock(settingsState = SingletonInject { settingsState })
        appFactory.treeManager = SingletonInject { treeManager }
        appFactory.treePersistenceService = SingletonInject { treePersistenceService }
        appFactory.settingsService = SingletonInject { settingsService }
        appFactory.settingsState = SingletonInject { settingsState }
        appFactory.databaseLock = SingletonInject { databaseLock }
        appFactory.systemClipboardManager = SingletonInject { systemClipboardManager }

        // build db tree
        val itemR = RootTreeItem()
        val itemRa = TextTreeItem(null, "a")
        itemR.add(itemRa)
        val itemRa1 = TextTreeItem(null, "a1")
        itemRa.add(itemRa1)
        val itemRb = TextTreeItem(null, "b")
        itemR.add(itemRb)
        val itemRbc = TextTreeItem(null, "bc")
        itemRb.add(itemRbc)
        treeManager.rootItem = itemR
        println("""
    Serialized:
    ${treePersistenceService.serializeTree(itemR)}
    """.trimIndent())

        // copy items
        treeManager.goTo(itemRa)
        val itemPosistions: MutableSet<Int> = TreeSet()
        itemPosistions.add(0)
        ClipboardCommand(
            treeManager=SingletonInject { treeManager },
            systemClipboardManager=SingletonInject { systemClipboardManager },
        ).copyItems(itemPosistions, true)

        // paste as link
        treeManager.goTo(itemRb)

        ClipboardCommand(
            treeManager=SingletonInject { treeManager },
            systemClipboardManager=SingletonInject { systemClipboardManager },
        ).pasteItemsAsLink(0)
        val link = treeManager.currentItem!!.getChild(0) as LinkTreeItem
        println("""
    After pasting link:
    ${treePersistenceService.serializeTree(itemR)}
    """.trimIndent())
        println("Link: $link")
        println("Link target: " + link.target)
        Assert.assertEquals(itemRa1, link.target)
        println("Link target path: " + link.targetPath)
        println("Link display target path: " + link.displayTargetPath)
        println("Link display name: " + link.displayName)
        Assert.assertEquals(itemRb, treeManager.currentItem)
        // click pasted link
        databaseLock.unlockIfLocked(null)
        TreeCommand().itemClicked(0, link)
        Assert.assertEquals(itemRa1, treeManager.currentItem)
        println("current Item: " + treeManager.currentItem)
    }
}