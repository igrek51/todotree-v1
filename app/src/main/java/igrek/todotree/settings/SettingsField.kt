package igrek.todotree.settings

enum class SettingsField constructor(
        val typeDef: PreferenceTypeDefinition<*>
) {

    LockDB(false),

    UserAuthToken(""),

    ;

    constructor(defaultValue: String) : this(StringPreferenceType(defaultValue))

    constructor(defaultValue: Long) : this(LongPreferenceType(defaultValue))

    constructor(defaultValue: Float) : this(FloatPreferenceType(defaultValue))

    constructor(defaultValue: Boolean) : this(BooleanPreferenceType(defaultValue))

    fun preferenceName(): String {
        return this.name.replaceFirstChar { it.lowercase() }
    }

}
