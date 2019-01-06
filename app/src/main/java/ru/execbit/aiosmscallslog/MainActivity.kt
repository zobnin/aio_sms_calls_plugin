package ru.execbit.aiosmscallslog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.WindowManager

@SuppressLint("ExportedPreferenceActivity")
class MainActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        fragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()

        checkForChinesePhone()
    }

    private fun checkForChinesePhone() {
        if (Settings.showChinesePmWarning) {
            val pmIntent = ChinesePhoneTools.getPowerManagerIntent(this)
            pmIntent?.let {
                showChinesePhoneWarning(pmIntent)
            }
            Settings.showChinesePmWarning = false
        }
    }

    private fun showChinesePhoneWarning(intent: Intent) {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.warning)
            setMessage(R.string.chinese_pm_warning)
            setCancelable(false)

            setPositiveButton(R.string.open_settings) { _, _ ->
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            setNegativeButton(R.string.cancel) { _, _ -> }

            create()
            show()
        }
    }
}
