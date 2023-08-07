package ru.execbit.aiosmscallslog

import com.mohamadamin.kpreferences.preference.Preference

object Settings {
    var callsNum by Preference("3", "calls_num")
    var callsShowUnknown by Preference(false, "calls_show_unknown")
    var callsShowOnlyMissed by Preference(false, "calls_show_only_missed")
    var callsTruncateMethod by Preference("by_last_name", "calls_truncate_method")
    var callsAutoHide by Preference(false, "calls_auto_hide")
    var callsEnableSearch by Preference(true, "calls_enable_search")
    var smsNum by Preference("3", "sms_num")
    var smsShowRead by Preference(true, "sms_show_read")
    var smsAutoHide by Preference(false, "sms_auto_hide")
    var smsEnableSearch by Preference(true, "sms_enable_search")
    var lastPluginUpdateCheck by Preference(0L, "last_update_check")
    var notifyShowedForVersion by Preference(BuildConfig.VERSION_CODE, "notify_showed")
    var pluginUid by Preference("", "plugin_uid")
}