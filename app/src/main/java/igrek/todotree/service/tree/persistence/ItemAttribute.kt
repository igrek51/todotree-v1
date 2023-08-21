package igrek.todotree.service.tree.persistence

internal class ItemAttribute(val name: String, val value: String) {

    override fun equals(other: Any?): Boolean {
        if (other !is ItemAttribute) return false
        return name == other.name && value == other.value
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