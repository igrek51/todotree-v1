package igrek.todotree.service.tree

import igrek.todotree.dagger.base.BaseDaggerTest
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.domain.treeitem.RootTreeItem
import igrek.todotree.domain.treeitem.TextTreeItem
import igrek.todotree.intent.ClipboardCommand
import igrek.todotree.intent.TreeCommand
import org.junit.Assert
import org.junit.Test
import java.util.*

class TreeManagerTest : BaseDaggerTest() {

    @Test
    fun testLinkPasting() {
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
    ${persistenceService.serializeTree(itemR)}
    """.trimIndent())

        // copy items
        treeManager.goTo(itemRa)
        val itemPosistions: MutableSet<Int> = TreeSet()
        itemPosistions.add(0)
        ClipboardCommand().copyItems(itemPosistions, true)

        // paste as link
        treeManager.goTo(itemRb)
        ClipboardCommand().pasteItemsAsLink(0)
        val link = treeManager.currentItem.getChild(0) as LinkTreeItem
        println("""
    After pasting link:
    ${persistenceService.serializeTree(itemR)}
    """.trimIndent())
        println("Link: $link")
        println("Link target: " + link.target)
        Assert.assertEquals(itemRa1, link.target)
        println("Link target path: " + link.targetPath)
        println("Link display target path: " + link.displayTargetPath)
        println("Link display name: " + link.displayName)
        Assert.assertEquals(itemRb, treeManager.currentItem)
        // click pasted link
        dbLock.unlockIfLocked(null)
        TreeCommand().itemClicked(0, link)
        Assert.assertEquals(itemRa1, treeManager.currentItem)
        println("current Item: " + treeManager.currentItem)
    }
}