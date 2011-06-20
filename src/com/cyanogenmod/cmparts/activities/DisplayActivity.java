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
import com.cyanogenmod.cmparts.activities.CPUActivity;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemProperties;
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

    private CheckBoxPreference mElectronBeamAnimationOn;

    private CheckBoxPreference mElectronBeamAnimationOff;

    private CheckBoxPreference mRotation0Pref;
    private CheckBoxPreference mRotation90Pref;
    private CheckBoxPreference mRotation180Pref;
    private CheckBoxPreference mRotation270Pref;
<<<<<<< HEAD

    private CheckBoxPreference mOMAPDSSmodePref;

    private static final String OMAP_DSS_MODE_PREF = "pref_omap_dss_mode";

    public static final String OMAP_DSS_MODE_PERSIST_PROP = "persist.sys.omap_dss_mode";

    public static final String OMAP_DSS_MODE_DEFAULT = "1";

    public static final String OMAP_DSS_MODE_FILE = "/sys/devices/omapdss/display0/update_mode";

    private CheckBoxPreference mNaOnPlugPref;

    private static final String NA_ON_PLUG_PREF = "pref_na_on_plug";

    public static final String NA_ON_PLUG_PERSIST_PROP = "persist.sys.no_action_on_plug";

    public static final String NA_ON_PLUG_DEFAULT = "0";
=======
>>>>>>> cyanogen/gingerbread

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.display_settings_title_subhead);
        addPreferencesFromResource(R.xml.display_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        /* Preference Screens */
        mBacklightScreen = (PreferenceScreen) prefSet.findPreference(BACKLIGHT_SETTINGS);
        // No reason to show backlight if no light sensor on device
        if (((SensorManager) getSystemService(SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_LIGHT) == null) {
            ((PreferenceCategory) prefSet.findPreference(GENERAL_CATEGORY))
                    .removePreference(mBacklightScreen);
        }

        /* Electron Beam control */
        boolean animateScreenLights = getResources().getBoolean(
                com.android.internal.R.bool.config_animateScreenLights);
        mElectronBeamAnimationOn = (CheckBoxPreference)prefSet.findPreference(ELECTRON_BEAM_ANIMATION_ON);
        mElectronBeamAnimationOn.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.ELECTRON_BEAM_ANIMATION_ON,
                getResources().getBoolean(com.android.internal.R.bool.config_enableScreenOnAnimation) ? 1 : 0) == 1);
        mElectronBeamAnimationOff = (CheckBoxPreference)prefSet.findPreference(ELECTRON_BEAM_ANIMATION_OFF);
        mElectronBeamAnimationOff.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.ELECTRON_BEAM_ANIMATION_OFF,
                getResources().getBoolean(com.android.internal.R.bool.config_enableScreenOffAnimation) ? 1 : 0) == 1);

        /* Hide Electron Beam controls if electron beam is disabled */
        if (animateScreenLights) {
            prefSet.removePreference(mElectronBeamAnimationOn);
            prefSet.removePreference(mElectronBeamAnimationOff);
        }

        /* Rotation */
        mRotation0Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_0_PREF);
        mRotation90Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_90_PREF);
        mRotation180Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_180_PREF);
        mRotation270Pref = (CheckBoxPreference) prefSet.findPreference(ROTATION_270_PREF);
        int mode = Settings.System.getInt(getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION_MODE, 13);
        mRotation0Pref.setChecked((mode & 8) != 0);
        mRotation90Pref.setChecked((mode & 1) != 0);
        mRotation180Pref.setChecked((mode & 2) != 0);
        mRotation270Pref.setChecked((mode & 4) != 0);

        /* Milestone specific kernel bug workaround (temporary) */
        mOMAPDSSmodePref = (CheckBoxPreference) prefSet.findPreference(OMAP_DSS_MODE_PREF);
        String omapDssMode = SystemProperties.get(OMAP_DSS_MODE_PERSIST_PROP, OMAP_DSS_MODE_DEFAULT);
        mOMAPDSSmodePref.setChecked("1".equals(omapDssMode));

        /* Keep display off on plug */
        mNaOnPlugPref = (CheckBoxPreference) prefSet.findPreference(NA_ON_PLUG_PREF);
        String naOnPlug = SystemProperties.get(NA_ON_PLUG_PERSIST_PROP, NA_ON_PLUG_DEFAULT);
        mNaOnPlugPref.setChecked("1".equals(naOnPlug));
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
            if (mRotation0Pref.isChecked()) mode |= 8;
            if (mRotation90Pref.isChecked()) mode |= 1;
            if (mRotation180Pref.isChecked()) mode |= 2;
            if (mRotation270Pref.isChecked()) mode |= 4;
            if (mode == 0) {
                mode |= 8;
                mRotation0Pref.setChecked(true);
            }
            Settings.System.putInt(getContentResolver(),
                     Settings.System.ACCELEROMETER_ROTATION_MODE, mode);
<<<<<<< HEAD
        }

        if (preference == mOMAPDSSmodePref) {
            SystemProperties.set(OMAP_DSS_MODE_PERSIST_PROP,
                    mOMAPDSSmodePref.isChecked() ? "1" : "0");
            CPUActivity.writeOneLine(OMAP_DSS_MODE_FILE, (String) (mOMAPDSSmodePref.isChecked() ? "1" : "2"));
        }

        if (preference == mNaOnPlugPref) {
            SystemProperties.set(NA_ON_PLUG_PERSIST_PROP,
                    mNaOnPlugPref.isChecked() ? "1" : "0");
=======
>>>>>>> cyanogen/gingerbread
        }

        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

}
