package igrek.todotree.service.tree.persistence

internal class ItemAttribute(val name: String, val value: String) {

    override fun equals(obj: Any?): Boolean {
        if (obj !is ItemAttribute) return false
        return name == obj.name && value == obj.value
    }

    override fun toString(): String {
        return "$name = $value"
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}