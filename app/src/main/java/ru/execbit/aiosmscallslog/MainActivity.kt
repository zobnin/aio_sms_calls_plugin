package ru.execbit.aiosmscallslog

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.preference.PreferenceActivity
import android.provider.Settings
import androidx.annotation.RequiresApi

@Suppress("DEPRECATION")
@SuppressLint("ExportedPreferenceActivity")
class MainActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()

        if (Build.VERSION.SDK_INT >= 23) {
            requestSmsAndPhonePermissions()
            requestIgnoreBatteryOptimization()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestSmsAndPhonePermissions() {
        requestPermissions(arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS,
        ), 100)
    }

    @TargetApi(23)
    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimization() {
        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager

        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 100) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                // Force update SMS plugin
                ru.execbit.aiosmscallslog.Settings.smsSettingsChanged = true
            }
        }
    }
}
