package ru.execbit.aiosmscallslog

import com.mohamadamin.kpreferences.preference.Preference

object Settings {
    var smsSettingsChanged = false

    var callsNum by Preference("3", "calls_num")
    var callsConfirmation by Preference(true, "calls_confirmation")
    var callsShowUnknown by Preference(false, "calls_show_unknown")
    var callsTruncateMethod by Preference("by_last_name", "calls_truncate_method")

    var smsNum by Preference("3", "sms_num")
    var smsUpdateOnResume by Preference(false, "sms_update_on_resume")

    var callsIds by Preference("", "calls_ids")
    var smsIds by Preference("", "sms_ids")

    var lastPluginUpdateCheck by Preference(0L, "last_update_check")
    var notifyShowedForVersion by Preference(BuildConfig.VERSION_CODE, "notify_showed")
    var showChinesePmWarning by Preference(true, "show_chinese_pm_warning")

    var pluginUid by Preference("", "plugin_uid")
}