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

import android.content.res.Resources;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

public class DisplayActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    /* Preference Screens */
    private static final String BACKLIGHT_SETTINGS = "backlight_settings";

    private static final String GENERAL_CATEGORY = "general_category";

    private static final String ELECTRON_BEAM_ANIMATION_ON = "electron_beam_animation_on";

    private static final String ELECTRON_BEAM_ANIMATION_OFF = "electron_beam_animation_off";

    private PreferenceScreen mBacklightScreen;

    /* Other */
    private static final String ROTATION_0_PREF = "pref_rotation_0";
    private static final String ROTATION_90_PREF = "pref_rotation_90";
    private static final String ROTATION_180_PREF = "pref_rotation_180";
    private static final String ROTATION_270_PREF = "pref_rotation_270";

    private static final int ROTATION_0_MODE = 8;
    private static final int ROTATION_90_MODE = 1;
    private static final int ROTATION_180_MODE = 2;
    private static final int ROTATION_270_MODE = 4;

    private CheckBoxPreference mElectronBeamAnimationOn;

    private CheckBoxPreference mElectronBeamAnimationOff;

    private CheckBoxPreference mRotation0Pref;
    private CheckBoxPreference mRotation90Pref;
    private CheckBoxPreference mRotation180Pref;
    private CheckBoxPreference mRotation270Pref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.display_settings_title_subhead);
        addPreferencesFromResource(R.xml.display_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        Resources res = getResources();

        mBacklightScreen = (PreferenceScreen) prefSet.findPreference(BACKLIGHT_SETTINGS);

        /* Hide backlight settings if unsupported */
        if (!supportsBacklightSettings()) {
            ((PreferenceCategory) prefSet.findPreference(GENERAL_CATEGORY))
                    .removePreference(mBacklightScreen);
        }

        /* Electron Beam control */
        mElectronBeamAnimationOn = (CheckBoxPreference)prefSet.findPreference(ELECTRON_BEAM_ANIMATION_ON);
        mElectronBeamAnimationOff = (CheckBoxPreference)prefSet.findPreference(ELECTRON_BEAM_ANIMATION_OFF);
        if (res.getBoolean(com.android.internal.R.bool.config_enableScreenAnimation)) {
            mElectronBeamAnimationOn.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.ELECTRON_BEAM_ANIMATION_ON,
                    res.getBoolean(com.android.internal.R.bool.config_enableScreenOnAnimation) ? 1 : 0) == 1);
            mElectronBeamAnimationOff.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.ELECTRON_BEAM_ANIMATION_OFF,
                    res.getBoolean(com.android.internal.R.bool.config_enableScreenOffAnimation) ? 1 : 0) == 1);
        } else {
            /* Hide Electron Beam controls if disabled */
            ((PreferenceCategory) prefSet.findPreference(GENERAL_CATEGORY))
                .removePreference(mElectronBeamAnimationOn);
            ((PreferenceCategory) prefSet.findPreference(GENERAL_CATEGORY))
                .removePreference(mElectronBeamAnimationOff);
        }

        /* Rotation */
        mRotation0Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_0_PREF);
        mRotation90Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_90_PREF);
        mRotation180Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_180_PREF);
        mRotation270Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_270_PREF);
        int mode = Settings.System.getInt(getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION_MODE,
                        ROTATION_0_MODE|ROTATION_90_MODE|ROTATION_270_MODE);
        mRotation0Pref.setChecked((mode & ROTATION_0_MODE) != 0);
        mRotation90Pref.setChecked((mode & ROTATION_90_MODE) != 0);
        mRotation180Pref.setChecked((mode & ROTATION_180_MODE) != 0);
        mRotation270Pref.setChecked((mode & ROTATION_270_MODE) != 0);
    }

    /** Whether backlight settings are supported or not */
    public static boolean supportsBacklightSettings(Context c) {
        return (((SensorManager) c.getSystemService(SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_LIGHT) != null &&
            c.getResources().getBoolean(R.bool.supports_backlight_settings));
    }

    public boolean supportsBacklightSettings() {
        return supportsBacklightSettings(this);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        /* Preference Screens */
        if (preference == mBacklightScreen) {
            startActivity(mBacklightScreen.getIntent());
        }
        if (preference == mElectronBeamAnimationOn) {
            value = mElectronBeamAnimationOn.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ELECTRON_BEAM_ANIMATION_ON, value ? 1 : 0);
        }

        if (preference == mElectronBeamAnimationOff) {
            value = mElectronBeamAnimationOff.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ELECTRON_BEAM_ANIMATION_OFF, value ? 1 : 0);
        }

        if (preference == mRotation0Pref ||
            preference == mRotation90Pref ||
            preference == mRotation180Pref ||
            preference == mRotation270Pref) {
            int mode = 0;
            if (mRotation0Pref.isChecked()) mode |= ROTATION_0_MODE;
            if (mRotation90Pref.isChecked()) mode |= ROTATION_90_MODE;
            if (mRotation180Pref.isChecked()) mode |= ROTATION_180_MODE;
            if (mRotation270Pref.isChecked()) mode |= ROTATION_270_MODE;
            if (mode == 0) {
                mode |= ROTATION_0_MODE;
                mRotation0Pref.setChecked(true);
            }
            Settings.System.putInt(getContentResolver(),
                     Settings.System.ACCELEROMETER_ROTATION_MODE, mode);
        }

        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

}
