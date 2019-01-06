package ru.execbit.aiosmscallslog

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment

class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        setSettingsSummary()
    }

    private fun setSettingsSummary() {
        findPreference("calls_num")?.summary = Settings.callsNum
        findPreference("sms_num")?.summary = Settings.smsNum
        findPreference("calls_truncate_method")?.setListSummary(
            Settings.callsTruncateMethod, R.array.truncate_methods, R.array.truncate_methods_values)
    }

    private fun Preference.setListSummary(value: String, arrayRes: Int, arrayValuesRes: Int) {
        try {
            val arr = resources.getStringArray(arrayRes)
            val arrValues = resources.getStringArray(arrayValuesRes)
            val idx = arrValues.indexOf(value)

            if (idx >= 0) {
                summary = arr[idx]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        setSettingsSummary()
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}
