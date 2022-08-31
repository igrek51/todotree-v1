package igrek.todotree.service.preferences

enum class PropertyDefinition(val type: PropertyType, val defaultValue: Any) {
    lockDB(false), userAuthToken("");

    constructor(defaultValue: String) : this(PropertyType.STRING, defaultValue) {}
    constructor(defaultValue: Boolean) : this(PropertyType.BOOLEAN, defaultValue) {}
    constructor(defaultValue: Int) : this(PropertyType.INTEGER, defaultValue) {}
}