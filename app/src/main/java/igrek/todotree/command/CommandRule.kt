package igrek.todotree.command

import igrek.todotree.util.StringSimplifier


open class CommandRule(
    val condition: (key: String) -> Boolean,
    val activator: (key: String) -> Unit,
)

class SubCommandRule(
    vararg prefixes: String,
    subcommandActivator: (key: String) -> Unit,

) : CommandRule(condition = { key ->

    val simpleKey = StringSimplifier.simplify(key)
    prefixes.any { prefix -> simpleKey.startsWith("$prefix ") }

}, activator = { key: String ->

    val simpleKey = StringSimplifier.simplify(key)
    for (prefix in prefixes) {
        if (simpleKey.startsWith("$prefix ")) {
            subcommandActivator(key.drop(prefix.length + 1))
            break
        }
    }
})

class SimpleKeyRule(
    vararg exactKeys: String,
    activator: (key: String) -> Unit,
) : CommandRule(condition = { key ->

    val simplified = StringSimplifier.simplify(key)
    exactKeys.contains(simplified)

}, activator)


class NestedSubcommandRule(
    vararg val prefixes: String,
    val rules: List<CommandRule>,
) : CommandRule(condition = { key ->

    val simpleKey = StringSimplifier.simplify(key)
    prefixes.any { prefix -> simpleKey.startsWith("$prefix ") }

}, activator = { key: String ->
})

class ActivationResult(
    val activator: (key: String) -> Unit,
    private val key: String,
) {
    fun run() {
        activator(key)
    }
}


fun findActivator(rules: List<CommandRule>, key: String): ActivationResult? {
    for (rule in rules) {

        if (rule is NestedSubcommandRule) {
            val simpleKey = StringSimplifier.simplify(key)

            for (prefix in rule.prefixes) {
                if (simpleKey.startsWith("$prefix ")) {
                    val subkey = key.drop(prefix.length + 1)
                    findActivator(rule.rules, subkey)?.let { activation ->
                        return activation
                    }
                }
            }

        } else {
            if (rule.condition(key)) {
                return ActivationResult(rule.activator, key)
            }
        }
    }
    return null
}
