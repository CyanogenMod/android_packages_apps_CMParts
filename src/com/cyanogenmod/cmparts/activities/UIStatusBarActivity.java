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

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;

import com.cyanogenmod.cmparts.R;
import com.cyanogenmod.cmparts.activities.ColorPickerDialog.OnColorChangedListener;

public class UIStatusBarActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    private static final String PREF_STATUS_BAR_AM_PM = "pref_status_bar_am_pm";

    private static final String PREF_STATUS_BAR_CLOCK = "pref_status_bar_clock";

    private static final String PREF_STATUS_BAR_CM_BATTERY = "pref_status_bar_cm_battery";

    private static final String PREF_STATUS_BAR_ONEPERC_BATTERY = "pref_status_bar_oneperc_battery";

    private static final String PREF_STATUS_BAR_CM_BATTERY_COLOR = "pref_status_bar_cm_battery_color";

    private static final String PREF_STATUS_BAR_COMPACT_CARRIER = "pref_status_bar_compact_carrier";

    private static final String PREF_FORCE_PLMN_DISPLAY = "pref_status_bar_force_plmn_display";

    private static final String ONEPERC_BATT_PERSIST_PROP = "persist.sys.one_percent_batt";

    private static final String ONEPERC_BATT_DEFAULT = "0";

    private static final String PREF_STATUS_BAR_BRIGHTNESS_CONTROL =
            "pref_status_bar_brightness_control";

    private static final String PREF_STATUS_BAR_CM_SIGNAL = "pref_status_bar_cm_signal";

    private static final String PREF_STATUS_BAR_HEADSET = "pref_status_bar_headset";

    private ListPreference mStatusBarAmPm;

    private ListPreference mStatusBarCmSignal;

    private ListPreference mStatusBarCmBattery;

    private ListPreference mStatusBarCmBatteryColor;

    private CheckBoxPreference mStatusBarClock;

    private CheckBoxPreference mStatusBarOnepercBattery;

    private CheckBoxPreference mStatusBarCompactCarrier;

    private CheckBoxPreference mStatusBarForcePlmnDisplay;

    private CheckBoxPreference mStatusBarBrightnessControl;

    private CheckBoxPreference mStatusBarHeadset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.ui_status_bar_title);
        addPreferencesFromResource(R.xml.ui_status_bar);

        PreferenceScreen prefSet = getPreferenceScreen();

        mStatusBarClock = (CheckBoxPreference) prefSet.findPreference(PREF_STATUS_BAR_CLOCK);
        mStatusBarCompactCarrier = (CheckBoxPreference) prefSet
                .findPreference(PREF_STATUS_BAR_COMPACT_CARRIER);
        mStatusBarCmBattery = (ListPreference) prefSet
                .findPreference(PREF_STATUS_BAR_CM_BATTERY);
        mStatusBarOnepercBattery = (CheckBoxPreference) prefSet
                .findPreference(PREF_STATUS_BAR_ONEPERC_BATTERY);
        String onepercBattery = SystemProperties.get(ONEPERC_BATT_PERSIST_PROP, ONEPERC_BATT_DEFAULT);
        mStatusBarOnepercBattery.setChecked("1".equals(onepercBattery));
        mStatusBarForcePlmnDisplay = (CheckBoxPreference) prefSet.findPreference(PREF_FORCE_PLMN_DISPLAY);
        mStatusBarCmBatteryColor = (ListPreference) prefSet
                .findPreference(PREF_STATUS_BAR_CM_BATTERY_COLOR);
        mStatusBarBrightnessControl = (CheckBoxPreference) prefSet
                .findPreference(PREF_STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarHeadset = (CheckBoxPreference) prefSet
                .findPreference(PREF_STATUS_BAR_HEADSET);

        mStatusBarClock.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK, 1) == 1));
        mStatusBarCompactCarrier.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_COMPACT_CARRIER, 0) == 1));
        mStatusBarForcePlmnDisplay.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_FORCE_PLMN_DISPLAY, 0) == 1));
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE, 0) == 1));
        mStatusBarHeadset.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_HEADSET, 1) == 1));

        try {
            if (Settings.System.getInt(getContentResolver(), 
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mStatusBarBrightnessControl.setEnabled(false);
                mStatusBarBrightnessControl.setSummary(R.string.ui_status_bar_toggle_info);
            }
        } catch (SettingNotFoundException e) {
        }

        mStatusBarAmPm = (ListPreference) prefSet.findPreference(PREF_STATUS_BAR_AM_PM);
        mStatusBarCmSignal = (ListPreference) prefSet.findPreference(PREF_STATUS_BAR_CM_SIGNAL);

        int statusBarAmPm = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_AM_PM, 2);
        mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
        mStatusBarAmPm.setOnPreferenceChangeListener(this);

        int signalStyle = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_CM_SIGNAL_TEXT, 0);
        mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
        mStatusBarCmSignal.setOnPreferenceChangeListener(this);

        int statusBarCmBattery = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_CM_BATTERY, 0);
        mStatusBarCmBattery.setValue(String.valueOf(statusBarCmBattery));
        mStatusBarCmBattery.setOnPreferenceChangeListener(this);

        mStatusBarCmBatteryColor.setValue(Settings.System.getString(getContentResolver(),
                Settings.System.STATUS_BAR_CM_BATTERY_COLOR));
        mStatusBarCmBatteryColor.setOnPreferenceChangeListener(this);
        mStatusBarCmBatteryColor.setEnabled(statusBarCmBattery == 2);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarAmPm) {
            int statusBarAmPm = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_AM_PM,
                    statusBarAmPm);
            return true;
        } else if (preference == mStatusBarCmSignal) {
            int signalStyle = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_CM_SIGNAL_TEXT,
                    signalStyle);
            return true;
        } else if (preference == mStatusBarCmBattery) {
            int statusBarCmBattery = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_CM_BATTERY,
                    statusBarCmBattery);
            mStatusBarCmBatteryColor.setEnabled(statusBarCmBattery == 2);
            return true;
        } else if (preference == mStatusBarCmBatteryColor) {
            String statusBarCmBatteryColor = (String) newValue;
            if ("custom".equals(statusBarCmBatteryColor)) {
                int color = -1;
                String colorString = Settings.System.getString(getContentResolver(),
                        Settings.System.STATUS_BAR_CM_BATTERY_COLOR);
                if (!TextUtils.isEmpty(colorString)) {
                    try {
                        color = Color.parseColor(colorString);
                    } catch (IllegalArgumentException e) { }
                }
                new ColorPickerDialog(this, mColorListener, color).show();
            } else {
                Settings.System.putString(getContentResolver(),
                        Settings.System.STATUS_BAR_CM_BATTERY_COLOR, statusBarCmBatteryColor);
            }
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        /* Preference Screens */
        if (preference == mStatusBarClock) {
            value = mStatusBarClock.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_CLOCK,
                    value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarOnepercBattery) {
            SystemProperties.set(ONEPERC_BATT_PERSIST_PROP,
                    mStatusBarOnepercBattery.isChecked() ? "1" : "0");
            return true;
        } else if (preference == mStatusBarCompactCarrier) {
            value = mStatusBarCompactCarrier.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_COMPACT_CARRIER, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarForcePlmnDisplay) {
            value = mStatusBarForcePlmnDisplay.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_FORCE_PLMN_DISPLAY,
                    value ? 1 : 0);
        } else if (preference == mStatusBarBrightnessControl) {
            value = mStatusBarBrightnessControl.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarHeadset) {
            value = mStatusBarHeadset.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.STATUS_BAR_HEADSET,
                    value ? 1 : 0);
            return true;
        }
        return false;
    }

    private OnColorChangedListener mColorListener = new OnColorChangedListener() {
        @Override
        public void colorUpdate(int color) {
            // no-op
        }

        @Override
        public void colorChanged(int color) {
            String colorString = String.format("#%02x%02x%02x", Color.red(color),
                    Color.green(color), Color.blue(color));
            Settings.System.putString(getContentResolver(),
                    Settings.System.STATUS_BAR_CM_BATTERY_COLOR, colorString);
        }
    };
}
