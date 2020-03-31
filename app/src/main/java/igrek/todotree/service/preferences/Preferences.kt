package igrek.todotree.service.preferences

import android.content.Context
import android.content.SharedPreferences
import igrek.todotree.info.logger.LoggerFactory
import java.util.*

open class Preferences(context: Context) {
    private val propertyValues: MutableMap<String, Any?> = HashMap()
    private val sharedPreferences: SharedPreferences
    private val logger = LoggerFactory.logger

    protected open fun createSharedPreferences(context: Context): SharedPreferences {
        return context.applicationContext
                .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private fun loadAll() {
        for (propertyDefinition in PropertyDefinition.values()) {
            loadProperty(propertyDefinition)
        }
    }

    private fun loadProperty(propertyDefinition: PropertyDefinition) {
        val propertyName = propertyDefinition.name
        val value: Any?
        if (exists(propertyName)) {
            value = when (propertyDefinition.type) {
                PropertyType.STRING -> sharedPreferences.getString(propertyName, null)
                PropertyType.BOOLEAN -> sharedPreferences.getBoolean(propertyName, false)
                PropertyType.INTEGER -> sharedPreferences.getInt(propertyName, 0)
            }
            logger.debug("preferences property loaded: $propertyName = $value")
        } else {
            value = propertyDefinition.defaultValue
            logger.debug("Missing preferences property, loading default value: $propertyName = $value")
        }
        propertyValues[propertyName] = value
    }

    open fun saveAll() {
        for (propertyDefinition in PropertyDefinition.values()) {
            saveProperty(propertyDefinition)
        }
    }

    private fun saveProperty(propertyDefinition: PropertyDefinition) {
        val propertyName = propertyDefinition.name
        if (propertyValues.containsKey(propertyName)) {
            val propertyValue = propertyValues[propertyName]
            when (propertyDefinition.type) {
                PropertyType.STRING -> setString(propertyName, castIfNotNull(propertyValue, String::class.java))
                PropertyType.BOOLEAN -> setBoolean(propertyName, castIfNotNull(propertyValue, Boolean::class.java))
                PropertyType.INTEGER -> setInt(propertyName, castIfNotNull(propertyValue, Int::class.java))
            }
            logger.debug("Shared preferences property saved: $propertyName = $propertyValue")
        } else {
            logger.warn("No shared preferences property found in map")
        }
    }

    open fun <T> getValue(propertyDefinition: PropertyDefinition, clazz: Class<T>): T? {
        val propertyName = propertyDefinition.name
        if (!propertyValues.containsKey(propertyName)) return null
        val propertyValue = propertyValues[propertyName]
        return castIfNotNull(propertyValue, clazz)
    }

    private fun <T> castIfNotNull(o: Any?, clazz: Class<T>): T? {
        return if (o == null) null else o as T
    }

    fun setValue(propertyDefinition: PropertyDefinition, value: Any?) {
        val propertyName = propertyDefinition.name
        propertyValues[propertyName] = value
    }

    open fun clear() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    private fun setBoolean(name: String, value: Boolean?) {
        val editor = sharedPreferences.edit()
        if (value == null) {
            editor.remove(name)
        } else {
            editor.putBoolean(name, value)
        }
        editor.apply()
    }

    private fun setInt(name: String, value: Int?) {
        val editor = sharedPreferences.edit()
        if (value == null) {
            editor.remove(name)
        } else {
            editor.putInt(name, value)
        }
        editor.apply()
    }

    private fun setString(name: String, value: String?) {
        val editor = sharedPreferences.edit()
        if (value == null) {
            editor.remove(name)
        } else {
            editor.putString(name, value)
        }
        editor.apply()
    }

    open fun exists(name: String?): Boolean {
        return sharedPreferences.contains(name)
    }

    companion object {
        private const val SHARED_PREFERENCES_NAME = "ToDoTreeUserPreferences"
    }

    init {
        sharedPreferences = createSharedPreferences(context)
        loadAll()
    }
}