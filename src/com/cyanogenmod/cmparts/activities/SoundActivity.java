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
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import java.util.Calendar;

public class SoundActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    private static final int DIALOG_QUIET_HOURS_START = 1;

    private static final int DIALOG_QUIET_HOURS_END = 2;

    private static final String NOTIFICATIONS_FOCUS = "notif-focus";

    private static final String NOTIFICATIONS_SPEAKER = "notif-speaker";

    private static final String NOTIFICATIONS_ATTENUATION = "notif-attn";

    private static final String NOTIFICATIONS_LIMITVOL = "notif-limitvol";

    private static final String VOLUME_CONTROL_SILENT = "vol-ctrl-silent";

    private static final String VIBRATE_IN_CALL = "vibrate-in-call";

    private static final String LOCK_VOLUME_KEYS = "lock-volume-keys";

    private static final String RINGS_SPEAKER = "ring-speaker";

    private static final String RINGS_ATTENUATION = "ring-attn";

    private static final String RINGS_LIMITVOL = "ring-limitvol";

    private static final String ALARMS_SPEAKER = "alarm-speaker";

    private static final String ALARMS_ATTENUATION = "alarm-attn";

    private static final String ALARMS_LIMITVOL = "alarm-limitvol";

    private static final String CAMERA_SHUTTER_MUTE = "camera-mute";

    private static final String PREFIX = "persist.sys.";

    private static final String CAMERA_CATEGORY = "camera_category";
    private static final String CAMERA_SHUTTER_DISABLE = "ro.camera.sound.disabled";

    private static String getKey(String suffix) {
        return PREFIX + suffix;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.sound_settings_title_subhead);
        addPreferencesFromResource(R.xml.sound_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        CheckBoxPreference p = (CheckBoxPreference) prefSet.findPreference(NOTIFICATIONS_FOCUS);
        p.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATIONS_AUDIO_FOCUS, 1) != 0);
        p.setOnPreferenceChangeListener(this);

        p = (CheckBoxPreference) prefSet.findPreference(NOTIFICATIONS_SPEAKER);
        p.setChecked(SystemProperties.getBoolean(getKey(NOTIFICATIONS_SPEAKER), false));
        p.setOnPreferenceChangeListener(this);

        p = (CheckBoxPreference) prefSet.findPreference(VOLUME_CONTROL_SILENT);
        p.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.VOLUME_CONTROL_SILENT, 0) != 0);
        p.setOnPreferenceChangeListener(this);

        p = (CheckBoxPreference) prefSet.findPreference(VIBRATE_IN_CALL);
        p.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.VIBRATE_IN_CALL, 1) != 0);
        p.setOnPreferenceChangeListener(this);

        p = (CheckBoxPreference) prefSet.findPreference(LOCK_VOLUME_KEYS);
        p.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCK_VOLUME_KEYS, 0) != 0);
        p.setOnPreferenceChangeListener(this);

        p = (CheckBoxPreference) prefSet.findPreference(RINGS_SPEAKER);
        p.setChecked(SystemProperties.getBoolean(getKey(RINGS_SPEAKER), false));
        p.setOnPreferenceChangeListener(this);

        p = (CheckBoxPreference) prefSet.findPreference(ALARMS_SPEAKER);
        p.setChecked(SystemProperties.getBoolean(getKey(ALARMS_SPEAKER), false));
        p.setOnPreferenceChangeListener(this);

        ListPreference lp = (ListPreference) prefSet.findPreference(NOTIFICATIONS_ATTENUATION);
        lp.setValue(String.valueOf(SystemProperties.getInt(getKey(NOTIFICATIONS_ATTENUATION), 6)));
        lp.setSummary(lp.getEntry());
        lp.setOnPreferenceChangeListener(this);

        lp = (ListPreference) prefSet.findPreference(RINGS_ATTENUATION);
        lp.setValue(String.valueOf(SystemProperties.getInt(getKey(RINGS_ATTENUATION), 6)));
        lp.setSummary(lp.getEntry());
        lp.setOnPreferenceChangeListener(this);

        lp = (ListPreference) prefSet.findPreference(ALARMS_ATTENUATION);
        lp.setValue(String.valueOf(SystemProperties.getInt(getKey(ALARMS_ATTENUATION), 6)));
        lp.setSummary(lp.getEntry());
        lp.setOnPreferenceChangeListener(this);

        lp = (ListPreference) prefSet.findPreference(NOTIFICATIONS_LIMITVOL);
        lp.setValue(String.valueOf(SystemProperties.getInt(getKey(NOTIFICATIONS_LIMITVOL), 1)));
        lp.setSummary(lp.getEntry());
        lp.setOnPreferenceChangeListener(this);

        lp = (ListPreference) prefSet.findPreference(RINGS_LIMITVOL);
        lp.setValue(String.valueOf(SystemProperties.getInt(getKey(RINGS_LIMITVOL), 1)));
        lp.setSummary(lp.getEntry());
        lp.setOnPreferenceChangeListener(this);

        lp = (ListPreference) prefSet.findPreference(ALARMS_LIMITVOL);
        lp.setValue(String.valueOf(SystemProperties.getInt(getKey(ALARMS_LIMITVOL), 1)));
        lp.setSummary(lp.getEntry());
        lp.setOnPreferenceChangeListener(this);

        if (SystemProperties.getBoolean(CAMERA_SHUTTER_DISABLE, false)) {
            // we cannot configure camera sound, hide camera settigs
            prefSet.removePreference(prefSet.findPreference(CAMERA_CATEGORY));
        } else {
            p = (CheckBoxPreference) prefSet.findPreference(CAMERA_SHUTTER_MUTE);
            p.setChecked(SystemProperties.getBoolean(getKey(CAMERA_SHUTTER_MUTE), false));
            p.setOnPreferenceChangeListener(this);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key.equals(NOTIFICATIONS_FOCUS)) {
            Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATIONS_AUDIO_FOCUS,
                    getBoolean(newValue) ? 1 : 0);
        } else if (key.equals(VOLUME_CONTROL_SILENT)) {
            Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_CONTROL_SILENT,
                    getBoolean(newValue) ? 1 : 0);
        } else if (key.equals(VIBRATE_IN_CALL)) {
            Settings.System.putInt(getContentResolver(), Settings.System.VIBRATE_IN_CALL,
                    getBoolean(newValue) ? 1 : 0);
        } else if (key.equals(LOCK_VOLUME_KEYS)) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCK_VOLUME_KEYS,
                    getBoolean(newValue) ? 1 : 0);
        } else if (key.equals(NOTIFICATIONS_SPEAKER) || key.equals(RINGS_SPEAKER)
                || key.equals(ALARMS_SPEAKER)) {
            SystemProperties.set(getKey(key), getBoolean(newValue) ? "1" : "0");
        } else if (key.equals(CAMERA_SHUTTER_MUTE)) {
            if (getBoolean(newValue)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.sound_camera_shutter_disable_warning_title);
                builder.setMessage(R.string.sound_camera_shutter_disable_warning);
                builder.setPositiveButton(com.android.internal.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SystemProperties.set(getKey(CAMERA_SHUTTER_MUTE), "1");
                        }
                    });
                final CheckBoxPreference p = (CheckBoxPreference) preference;
                builder.setNegativeButton(com.android.internal.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            p.setChecked(false);
                        }
                    });
                builder.show();
            } else {
                SystemProperties.set(getKey(CAMERA_SHUTTER_MUTE), "0");
            }
        } else {
            SystemProperties.set(getKey(key), String.valueOf(getInt(newValue)));
            mHandler.sendMessage(mHandler.obtainMessage(0, key));
        }
        return true;
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
        return dlg;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (msg.obj != null) {
                        ListPreference p = (ListPreference) findPreference(msg.obj.toString());
                        p.setSummary(p.getEntry());
                    }
                    break;
            }
        }
    };

    private boolean getBoolean(Object o) {
        return Boolean.valueOf(o.toString());
    }

    private int getInt(Object o) {
        return Integer.valueOf(o.toString());
    }
}
