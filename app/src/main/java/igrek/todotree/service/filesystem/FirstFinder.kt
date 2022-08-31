package igrek.todotree.service.filesystem

import java.util.*

class FirstFinder<T> {
    private val rules: MutableList<Rule> = LinkedList<Rule>()

    fun addRule(whenn: BooleanCondition, then: Provider<T>): FirstFinder<T> {
        rules.add(Rule(whenn, then))
        return this
    }

    fun addRule(whenn: BooleanCondition, then: T): FirstFinder<T> {
        rules.add(Rule(whenn) { then })
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
            val whenn: BooleanCondition = rule.`when`
            if (whenn.test()) {
                val then: Provider<T> = rule.then
                val value: T? = then.get()
                if (value != null) // accept only not null values
                    return value
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