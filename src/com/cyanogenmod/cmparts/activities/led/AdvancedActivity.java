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

package com.cyanogenmod.cmparts.activities.led;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.Toast;

import com.cyanogenmod.cmparts.R;

public class AdvancedActivity extends PreferenceActivity implements
            Preference.OnPreferenceChangeListener {

    private CheckBoxPreference mPulseAlwaysPref;
    private CheckBoxPreference mBlendColorsPref;
    private CheckBoxPreference mPulseSuccessionPref;
    private CheckBoxPreference mPulseRandomPref;
    private CheckBoxPreference mPulseInOrderPref;
    private Preference mResetPref;

    private static final boolean SHOLES_DEVICE = Build.DEVICE.contains("sholes");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.led_advanced);
        setResult(RESULT_CANCELED);

        mPulseAlwaysPref = (CheckBoxPreference) findPreference("always_pulse");
        mPulseAlwaysPref.setOnPreferenceChangeListener(this);
        mBlendColorsPref = (CheckBoxPreference) findPreference("blend_colors");
        mPulseSuccessionPref = (CheckBoxPreference) findPreference("pulse_succession");
        mPulseRandomPref = (CheckBoxPreference) findPreference("pulse_random");
        mPulseInOrderPref = (CheckBoxPreference) findPreference("pulse_in_order");
        mResetPref = findPreference("reset");

        boolean hasDualLed = getResources().getBoolean(R.bool.has_dual_notification_led) ||
                             getResources().getBoolean(R.bool.has_mixable_dual_notification_led);

        /* Hide options only relevant to RGB lights if no RGB LED is present */
        if (hasDualLed) {
            PreferenceScreen screen = getPreferenceScreen();
            screen.removePreference(mBlendColorsPref);
            screen.removePreference(mPulseSuccessionPref);
            screen.removePreference(mPulseRandomPref);
            screen.removePreference(mPulseInOrderPref);
        } else {
            mBlendColorsPref.setOnPreferenceChangeListener(this);
            mPulseSuccessionPref.setOnPreferenceChangeListener(this);
            mPulseRandomPref.setOnPreferenceChangeListener(this);
            mPulseInOrderPref.setOnPreferenceChangeListener(this);
        }

        initSettings();
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object objValue) {
        if (pref == mPulseAlwaysPref) {
            boolean value = (Boolean) objValue;
            return putInt(Settings.System.TRACKBALL_SCREEN_ON, value ? 1 : 0);
        } else if (pref == mPulseSuccessionPref) {
            boolean value = (Boolean) objValue;
            return handlePulsePrefChange(mPulseSuccessionPref, value,
                    Settings.System.TRACKBALL_NOTIFICATION_SUCCESSION);
        } else if (pref == mPulseRandomPref) {
            boolean value = (Boolean) objValue;
            return handlePulsePrefChange(mPulseRandomPref, value,
                    Settings.System.TRACKBALL_NOTIFICATION_RANDOM);
        } else if (pref == mPulseInOrderPref) {
            boolean value = (Boolean) objValue;
            return handlePulsePrefChange(mPulseInOrderPref, value,
                    Settings.System.TRACKBALL_NOTIFICATION_PULSE_ORDER);
        } else if (pref == mBlendColorsPref) {
            boolean value = (Boolean) objValue;
            if (putInt(Settings.System.TRACKBALL_NOTIFICATION_BLEND_COLOR, value ? 1 : 0)) {
                mPulseSuccessionPref.setEnabled(!value);
                mPulseRandomPref.setEnabled(!value);
                mPulseInOrderPref.setEnabled(!value);
                return true;
            }
            return false;
        }

        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference pref) {
        if (pref == mResetPref) {
            doReset();
            return true;
        }

        return super.onPreferenceTreeClick(screen, pref);
    }

    private boolean handlePulsePrefChange(final CheckBoxPreference pref, final boolean value, final String opt) {
        if (!value) {
            return putInt(opt, 0);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(R.string.notification_battery_warning_title);
        alertDialog.setMessage(getResources().getString(R.string.notification_battery_warning));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (putInt(opt, value ? 1 : 0)) {
                    pref.setChecked(true);
                }
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                (DialogInterface.OnClickListener) null);
        alertDialog.show();

        return false;
    }

    private void doReset() {
        /* XXX: confirmation? */
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(CategoryActivity.KEY_CATEGORY_LIST, null);
        editor.commit();

        Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_PACKAGE_COLORS, "");
        Toast.makeText(this, R.string.trackball_reset_all, Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
    }

    private void initSettings() {
        boolean blendEnabled = getInt(Settings.System.TRACKBALL_NOTIFICATION_BLEND_COLOR, 0) == 1;

        if (SHOLES_DEVICE && blendEnabled) {
            blendEnabled = false;
            mBlendColorsPref.setEnabled(false);
            putInt(Settings.System.TRACKBALL_NOTIFICATION_BLEND_COLOR, 0);
        }

        mPulseAlwaysPref.setChecked(getInt(Settings.System.TRACKBALL_SCREEN_ON, 0) == 1);
        mBlendColorsPref.setChecked(blendEnabled);
        mPulseSuccessionPref.setChecked(getInt(Settings.System.TRACKBALL_NOTIFICATION_SUCCESSION, 0) == 1);
        mPulseSuccessionPref.setEnabled(!blendEnabled);
        mPulseRandomPref.setChecked(getInt(Settings.System.TRACKBALL_NOTIFICATION_RANDOM, 0) == 1);
        mPulseRandomPref.setEnabled(!blendEnabled);
        mPulseInOrderPref.setChecked(getInt(Settings.System.TRACKBALL_NOTIFICATION_PULSE_ORDER, 0) == 1);
        mPulseInOrderPref.setEnabled(!blendEnabled);
    }

    private boolean putInt(String option, int value) {
        return Settings.System.putInt(getContentResolver(), option, value);
    }

    private int getInt(String option, int defValue) {
        return Settings.System.getInt(getContentResolver(), option, defValue);
    }
}
