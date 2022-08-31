package igrek.todotree.settings

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentTransaction
import androidx.preference.*
import igrek.todotree.util.RetryDelayed
import igrek.todotree.inject.LazyExtractor
import igrek.todotree.inject.LazyInject
import igrek.todotree.inject.appFactory
import igrek.todotree.R
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

class SettingsFragment(
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) : PreferenceFragmentCompat() {
    private val preferencesState by LazyExtractor(preferencesState)

    private var decimalFormat1: DecimalFormat = DecimalFormat("#.#")
    private var decimalFormat3: DecimalFormat = DecimalFormat("#.###")

    companion object {
        const val SEEKBAR_RESOLUTION = 10000
    }

    init {
        decimalFormat1.roundingMode = RoundingMode.HALF_UP
        decimalFormat3.roundingMode = RoundingMode.HALF_UP
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_def, rootKey)
        lateInit()
        Handler(Looper.getMainLooper()).post {
            lateInit()
        }
    }

    private fun lateInit() {
        setupComponents()
        refreshFragment()
    }

    private fun setupComponents() {

        setupSwitchPreference("lockDB",
            onLoad = { preferencesState.lockDB },
            onSave = { value: Boolean ->
                preferencesState.lockDB = value
            }
        )

        setupEditTextPreference("userAuthToken",
            onLoad = { preferencesState.userAuthToken },
            onSave = { value: String ->
                preferencesState.userAuthToken = value
            }
        )
    }

    @Suppress("DEPRECATION")
    private fun refreshFragment() {
        fragmentManager?.let { fragmentManager ->
            val ft: FragmentTransaction = fragmentManager.beginTransaction()
            if (Build.VERSION.SDK_INT >= 26) {
                ft.setReorderingAllowed(false)
            }
            ft.detach(this).attach(this).commitAllowingStateLoss()
        }
    }

    private fun toggleAllMultiPreference(excludeLanguagesPreference: MultiSelectListPreference) {
        if (multiPreferenceAllSelected(excludeLanguagesPreference)) {
            excludeLanguagesPreference.values = emptySet()
        } else {
            excludeLanguagesPreference.values = excludeLanguagesPreference.entryValues
                .map { s -> s.toString() }.toSet()
        }
        excludeLanguagesPreference.callChangeListener(excludeLanguagesPreference.values)
    }

    private fun setupListPreference(
        key: String,
        entriesMap: LinkedHashMap<String, String>,
        onLoad: () -> String?,
        onSave: (id: String) -> Unit,
    ) {
        val preference = findPreference(key) as ListPreference
        preference.entryValues = entriesMap.keys.toTypedArray()
        preference.entries = entriesMap.values.toTypedArray()
        preference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                onSave(newValue.toString())
                true
            }
        preference.value = onLoad()
    }

    private fun setupMultiListPreference(
        key: String,
        entriesMap: LinkedHashMap<String, String>,
        onLoad: () -> Set<String>?,
        onSave: (ids: Set<String>) -> Unit,
        stringConverter: (ids: Set<String>, entriesMap: LinkedHashMap<String, String>) -> String
    ): MultiSelectListPreference {
        val preference = findPreference(key) as MultiSelectListPreference
        preference.entryValues = entriesMap.keys.toTypedArray()
        preference.entries = entriesMap.values.toTypedArray()

        RetryDelayed(5, 500, KotlinNullPointerException::class.java) {
            preference.values = onLoad()
        }

        preference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { pref, newValue ->
                if (newValue != null && newValue is Set<*>) {
                    @Suppress("unchecked_cast")
                    val newSet = newValue as Set<String>
                    onSave(newSet)
                    pref.summary = stringConverter(newSet, entriesMap)
                }
                true
            }
        preference.summary = stringConverter(preference.values, entriesMap)
        return preference
    }

    private fun multiPreferenceAllSelected(multiPreference: MultiSelectListPreference): Boolean {
        if (multiPreference.entryValues.size != multiPreference.values.size)
            return false
        val values = multiPreference.values
        multiPreference.entryValues.forEach { value ->
            if (value !in values)
                return false
        }
        return true
    }

    private fun setupSeekBarPreference(
        key: String,
        min: Number,
        max: Number,
        onLoad: () -> Float,
        onSave: (value: Float) -> Unit,
        stringConverter: (value: Float) -> String,
    ) {
        val preference = findPreference(key) as SeekBarPreference
        preference.isAdjustable = true
        preference.max = SEEKBAR_RESOLUTION
        val currentValueF: Float = onLoad()
        val minF = min.toFloat()
        val maxF = max.toFloat()
        preference.value = calculateProgress(minF, maxF, currentValueF)
        preference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { pref, newValue ->
                val progress = newValue.toString().toFloat() / SEEKBAR_RESOLUTION
                val valueF = progress * (maxF - minF) + minF
                pref.summary = stringConverter(valueF)
                onSave(valueF)
                true
            }
        preference.summary = stringConverter(currentValueF)
    }

    private fun setupSwitchPreference(
        key: String,
        onLoad: () -> Boolean,
        onSave: (value: Boolean) -> Unit,
    ) {
        val preference = findPreference(key) as SwitchPreference
        preference.isChecked = onLoad()
        preference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                onSave(newValue as Boolean)
                true
            }
    }

    private fun setupEditTextPreference(
        key: String,
        onLoad: () -> String,
        onSave: (value: String) -> Unit,
    ) {
        val preference = findPreference(key) as EditTextPreference
        preference.text = onLoad()
        preference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                onSave(newValue as String)
                true
            }
    }

    private fun setupClickPreference(
        key: String,
        onClick: () -> Unit,
    ) {
        val button = findPreference(key)
        button.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            onClick.invoke()
            true
        }
    }

    private fun calculateProgress(min: Float, max: Float, value: Float): Int {
        val resolution = SEEKBAR_RESOLUTION
        if (value < min) {
            return 0
        }
        if (value > max) {
            return resolution
        }

        val progress = (value - min) / (max - min)
        return (progress * resolution).roundToInt()
    }

    private fun decimal3(value: Float): String {
        return decimalFormat3.format(value.toDouble())
    }

    private fun decimal1(value: Float): String {
        return decimalFormat1.format(value.toDouble())
    }

}