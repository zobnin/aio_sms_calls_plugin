package ru.execbit.aiolauncher.plugin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import ru.execbit.aiolauncher.models.PluginError
import ru.execbit.aiolauncher.models.PluginIntentActions
import ru.execbit.aiolauncher.models.PluginResult
import ru.execbit.aiosmscallslog.Settings

fun Context.sendPluginResult(result: PluginResult) {
    val i = Intent(PluginIntentActions.AIO_UPDATE).apply {
        `package` = "ru.execbit.aiolauncher"
        putExtra("api", 2)
        putExtra("result", result)
        putExtra("uid", Settings.pluginUid)
    }

    sendBroadcast(i)
}

fun Context.sendInvalidAioVersionError(cn: ComponentName) {
    sendPluginResult(
        PluginResult(
            from = cn,
            data = PluginError(5, "Update AIO Launcher")
        )
    )
}

