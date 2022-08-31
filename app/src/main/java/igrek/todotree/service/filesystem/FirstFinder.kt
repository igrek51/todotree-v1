package igrek.todotree.service.filesystem

import igrek.todotree.service.filesystem.FirstFinder.Provider
import java.util.*

class FirstFinder<T> {
    // remembers inserting order
    private val rules: MutableList<Rule> = LinkedList<Rule>()

    fun addRule(`when`: BooleanCondition, then: Provider<T>): FirstFinder<T> {
        rules.add(Rule(`when`, then))
        return this
    }

    fun addRule(`when`: BooleanCondition, then: T): FirstFinder<T> {
        rules.add(Rule(`when`, Provider<T> { then }))
        return this
    }

    fun addRule(then: Provider<T>): FirstFinder<T> {
        return addRule({ true }, then)
    }

    fun addRule(defaultValue: T): FirstFinder<T> {
        return addRule({ true }) { defaultValue }
    }

    fun find(): T? {
        for (rule in rules) {
            val `when`: BooleanCondition = rule.`when`
            if (`when`.test()) {
                val then: Provider<T> = rule.then
                if (then != null) {
                    val value: T? = then.get()
                    if (value != null) // accept only not null values
                        return value
                }
            }
        }
        return null
    }

    private inner class Rule(var `when`: BooleanCondition, var then: Provider<T>)
    fun interface BooleanCondition {
        fun test(): Boolean
    }

    fun interface Provider<T> {
        fun get(): T
    }
}