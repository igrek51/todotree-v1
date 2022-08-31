package igrek.todotree.service.tree.persistence

internal class ItemAttribute(val name: String, val value: String) {

    override fun equals(obj: Any?): Boolean {
        if (obj !is ItemAttribute) return false
        val attr2 = obj
        return name == attr2.name && value == attr2.value
    }

    override fun toString(): String {
        return "$name = $value"
    }
}