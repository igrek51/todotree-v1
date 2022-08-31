package igrek.todotree.service.preferences

import igrek.todotree.service.preferences.PropertyType

enum class PropertyDefinition(val type: PropertyType, val defaultValue: Any) {
    lockDB(false), userAuthToken("");

    constructor(defaultValue: String) : this(PropertyType.STRING, defaultValue) {}
    constructor(defaultValue: Boolean) : this(PropertyType.BOOLEAN, defaultValue) {}
    constructor(defaultValue: Int) : this(PropertyType.INTEGER, defaultValue) {}
}