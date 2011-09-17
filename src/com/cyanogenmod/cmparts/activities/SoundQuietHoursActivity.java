/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.cmparts.activities;

import com.cyanogenmod.cmparts.R;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.content.DialogInterface;

import java.util.Calendar;
import java.util.Date;

public class SoundQuietHoursActivity extends PreferenceActivity implements
        OnPreferenceChangeListener {

    private static final int DIALOG_QUIET_HOURS_START = 1;

    private static final int DIALOG_QUIET_HOURS_END = 2;

    private static final String KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled";

    private static final String KEY_QUIET_HOURS_START = "quiet_hours_start";

    private static final String KEY_QUIET_HOURS_END = "quiet_hours_end";

    private static final String KEY_QUIET_HOURS_MUTE = "quiet_hours_mute";

    private static final String KEY_QUIET_HOURS_STILL = "quiet_hours_still";

    private static final String KEY_QUIET_HOURS_DIM = "quiet_hours_dim";

    private CheckBoxPreference mQuietHoursEnabled;

    private Preference mQuietHoursStart;

    private Preference mQuietHoursEnd;

    private CheckBoxPreference mQuietHoursMute;

    private CheckBoxPreference mQuietHoursStill;

    private CheckBoxPreference mQuietHoursDim;

    private String returnTime(String t) {
        if (t == null || t.equals("")) {
            return "";
        }
        int hr = Integer.parseInt(t.trim());
        int mn = hr;

        hr = hr / 60;
        mn = mn % 60;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hr);
        cal.set(Calendar.MINUTE, mn);
        Date date = cal.getTime();
        return DateFormat.getTimeFormat(getApplicationContext()).format(date);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.sound_category_quiet_hours_title);
        addPreferencesFromResource(R.xml.sound_quiet_hours_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mQuietHoursEnabled = (CheckBoxPreference) findPreference(KEY_QUIET_HOURS_ENABLED);
        mQuietHoursStart = prefSet.findPreference(KEY_QUIET_HOURS_START);
        mQuietHoursStart.setSummary(returnTime(Settings.System.getString(getContentResolver(),
                Settings.System.QUIET_HOURS_START)));
        mQuietHoursStart.setOnPreferenceChangeListener(this);
        mQuietHoursEnd = findPreference(KEY_QUIET_HOURS_END);
        mQuietHoursEnd.setSummary(returnTime(Settings.System.getString(getContentResolver(),
                Settings.System.QUIET_HOURS_END)));
        mQuietHoursEnd.setOnPreferenceChangeListener(this);
        mQuietHoursMute = (CheckBoxPreference) findPreference(KEY_QUIET_HOURS_MUTE);
        mQuietHoursStill = (CheckBoxPreference) findPreference(KEY_QUIET_HOURS_STILL);
        mQuietHoursDim = (CheckBoxPreference) findPreference(KEY_QUIET_HOURS_DIM);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mQuietHoursEnabled) {
            Settings.System.putInt(getContentResolver(), Settings.System.QUIET_HOURS_ENABLED,
                    mQuietHoursEnabled.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mQuietHoursMute) {
            Settings.System.putInt(getContentResolver(), Settings.System.QUIET_HOURS_MUTE,
                    mQuietHoursMute.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mQuietHoursStill) {
            Settings.System.putInt(getContentResolver(), Settings.System.QUIET_HOURS_STILL,
                    mQuietHoursStill.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mQuietHoursDim) {
            Settings.System.putInt(getContentResolver(), Settings.System.QUIET_HOURS_DIM,
                    mQuietHoursDim.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mQuietHoursStart) {
            showDialog(DIALOG_QUIET_HOURS_START);
            mQuietHoursStart.setSummary(returnTime(Settings.System.getString(getContentResolver(),
                    Settings.System.QUIET_HOURS_START)));
            return true;
        } else if (preference == mQuietHoursEnd) {
            showDialog(DIALOG_QUIET_HOURS_END);
            mQuietHoursEnd.setSummary(returnTime(Settings.System.getString(getContentResolver(),
                    Settings.System.QUIET_HOURS_END)));
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key.equals(KEY_QUIET_HOURS_START)) {
            Settings.System.putInt(getContentResolver(), Settings.System.QUIET_HOURS_START,
                    getBoolean(newValue) ? 1 : 0);
            mQuietHoursStart.setSummary(returnTime(Settings.System.getString(getContentResolver(),
                    Settings.System.QUIET_HOURS_START)));
            return true;
        } else if (key.equals(KEY_QUIET_HOURS_END)) {
            Settings.System.putInt(getContentResolver(), Settings.System.QUIET_HOURS_END,
                    getBoolean(newValue) ? 1 : 0);
            mQuietHoursEnd.setSummary(returnTime(Settings.System.getString(getContentResolver(),
                    Settings.System.QUIET_HOURS_END)));
            return true;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_QUIET_HOURS_START:
                return createTimePicker(Settings.System.QUIET_HOURS_START);
            case DIALOG_QUIET_HOURS_END:
                return createTimePicker(Settings.System.QUIET_HOURS_END);
        }
        return super.onCreateDialog(id);
    }

    private TimePickerDialog createTimePicker(final String key) {
        int value = Settings.System.getInt(getContentResolver(), key, -1);
        int hour;
        int minutes;
        if (value < 0) {
            Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minutes = calendar.get(Calendar.MINUTE);
        } else {
            hour = value / 60;
            minutes = value % 60;
        }
        TimePickerDialog dlg = new TimePickerDialog(this, /* context */
        new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker v, int hours, int minutes) {
                Settings.System.putInt(getContentResolver(), key, hours * 60 + minutes);
            };
        }, hour, minutes, DateFormat.is24HourFormat(this));
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dlg) {
                if (key.equals(KEY_QUIET_HOURS_START)) {
                    mQuietHoursStart.setSummary(returnTime(Settings.System.getString(
                            getContentResolver(), Settings.System.QUIET_HOURS_START)));
                } else {
                    mQuietHoursEnd.setSummary(returnTime(Settings.System.getString(
                            getContentResolver(), Settings.System.QUIET_HOURS_END)));
                }
            }
        });
        return dlg;
    }

    private boolean getBoolean(Object o) {
        return Boolean.valueOf(o.toString());
    }
}
