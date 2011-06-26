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

import java.util.ArrayList;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.cyanogenmod.cmparts.R;
import com.cyanogenmod.cmparts.utils.ShortcutPickHelper;

public class InputActivity extends PreferenceActivity implements
        OnPreferenceChangeListener, ShortcutPickHelper.OnPickListener {

    private static final String TRACKBALL_WAKE_PREF = "pref_trackball_wake";

    private static final String VOLUME_WAKE_PREF = "pref_volume_wake";

    private static final String VOLBTN_MUSIC_CTRL_PREF = "pref_volbtn_music_controls";

    private static final String CAMBTN_MUSIC_CTRL_PREF = "pref_cambtn_music_controls";

    private static final String VOLBTN_ORIENT_PREF = "pref_volbtn_orientation";

    private static final String VOLBTN_ORIENT_PERSIST_PROP = "persist.sys.volbtn_orient_swap";

    private static final String VOLBTN_ORIENT_DEFAULT = "0";

    private static final String DOCK_OBSERVER_OFF_PREF = "pref_dock_observer_off";

    private static final String DOCK_OBSERVER_OFF_PERSIST_PROP = "persist.sys.dock_observer_off";

    private static final String DOCK_OBSERVER_OFF_DEFAULT = "0";

    private static final String KEYPAD_TYPE_PREF = "pref_keypad_type";

    private static final String KEYPAD_TYPE_PERSIST_PROP = "persist.sys.keypad_type";

    private static final String KEYPAD_TYPE_DEFAULT = "euro_qwerty";

    private static final String BUTTON_CATEGORY = "pref_category_button_settings";

    private static final String USER_DEFINED_KEY1 = "pref_user_defined_key1";

    private static final String USER_DEFINED_KEY2 = "pref_user_defined_key2";

    private static final String USER_DEFINED_KEY3 = "pref_user_defined_key3";

    private CheckBoxPreference mTrackballWakePref;

    private CheckBoxPreference mVolumeWakePref;

    private CheckBoxPreference mVolBtnMusicCtrlPref;

    private CheckBoxPreference mCamBtnMusicCtrlPref;

    private CheckBoxPreference mVolBtnOrientationPref;

    private CheckBoxPreference mDockObserverOffPref;

    private ListPreference mKeypadTypePref;

    private Preference mUserDefinedKey1Pref;

    private Preference mUserDefinedKey2Pref;

    private Preference mUserDefinedKey3Pref;

    private ShortcutPickHelper mPicker;

    private int mKeyNumber = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.input_settings_title_subhead);
        addPreferencesFromResource(R.xml.input_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        /* Trackball Wake */
        mTrackballWakePref = (CheckBoxPreference) prefSet.findPreference(TRACKBALL_WAKE_PREF);
        mTrackballWakePref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.TRACKBALL_WAKE_SCREEN, 1) == 1);

        /* Volume Wake */
        mVolumeWakePref = (CheckBoxPreference) prefSet.findPreference(VOLUME_WAKE_PREF);
        mVolumeWakePref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);

        /* Volume button music controls */
        mVolBtnMusicCtrlPref = (CheckBoxPreference) prefSet.findPreference(VOLBTN_MUSIC_CTRL_PREF);
        mVolBtnMusicCtrlPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.VOLBTN_MUSIC_CONTROLS, 1) == 1);
        mCamBtnMusicCtrlPref = (CheckBoxPreference) prefSet.findPreference(CAMBTN_MUSIC_CTRL_PREF);
        mCamBtnMusicCtrlPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.CAMBTN_MUSIC_CONTROLS, 0) == 1);

        mVolBtnOrientationPref = (CheckBoxPreference) prefSet.findPreference(VOLBTN_ORIENT_PREF);
        String volBtnOrientation = SystemProperties.get(VOLBTN_ORIENT_PERSIST_PROP, VOLBTN_ORIENT_DEFAULT);
        mVolBtnOrientationPref.setChecked("1".equals(volBtnOrientation));

        mDockObserverOffPref = (CheckBoxPreference) prefSet.findPreference(DOCK_OBSERVER_OFF_PREF);
        String dockObserverOff = SystemProperties.get(DOCK_OBSERVER_OFF_PERSIST_PROP, DOCK_OBSERVER_OFF_DEFAULT);
        mDockObserverOffPref.setChecked("1".equals(dockObserverOff));

        mKeypadTypePref = (ListPreference) prefSet.findPreference(KEYPAD_TYPE_PREF);
        String keypadType = SystemProperties.get(KEYPAD_TYPE_PERSIST_PROP, KEYPAD_TYPE_DEFAULT);
        mKeypadTypePref.setValue(keypadType);
        mKeypadTypePref.setOnPreferenceChangeListener(this);

        PreferenceCategory buttonCategory = (PreferenceCategory) prefSet
                .findPreference(BUTTON_CATEGORY);

        PreferenceCategory generalCategory = (PreferenceCategory) prefSet
                .findPreference("general_category");

        mUserDefinedKey1Pref = (Preference) prefSet.findPreference(USER_DEFINED_KEY1);
        mUserDefinedKey2Pref = (Preference) prefSet.findPreference(USER_DEFINED_KEY2);
        mUserDefinedKey3Pref = (Preference) prefSet.findPreference(USER_DEFINED_KEY3);

        if (!getResources().getBoolean(R.bool.has_trackball)) {
            buttonCategory.removePreference(mTrackballWakePref);
        }
        if (!getResources().getBoolean(R.bool.has_camera_button)) {
            buttonCategory.removePreference(mCamBtnMusicCtrlPref);
        }
        if (!"vision".equals(Build.DEVICE)) {
            buttonCategory.removePreference(mUserDefinedKey1Pref);
            buttonCategory.removePreference(mUserDefinedKey2Pref);
            buttonCategory.removePreference(mUserDefinedKey3Pref);
        }
        if (!getResources().getBoolean(R.bool.has_search_button))
                generalCategory.removePreference((Preference) prefSet.findPreference("input_search_key"));

        mPicker = new ShortcutPickHelper(this, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setAppSummary(mUserDefinedKey1Pref, Settings.System.USER_DEFINED_KEY1_APP);
        setAppSummary(mUserDefinedKey2Pref, Settings.System.USER_DEFINED_KEY2_APP);
        setAppSummary(mUserDefinedKey3Pref, Settings.System.USER_DEFINED_KEY3_APP);
    }

    private void setAppSummary(Preference pref, String key) {
        String value = Settings.System.getString(getContentResolver(), key);
        pref.setSummary(mPicker.getFriendlyNameForUri(value));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mTrackballWakePref) {
            value = mTrackballWakePref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.TRACKBALL_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
        } else if (preference == mVolumeWakePref) {
            value = mVolumeWakePref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.VOLUME_WAKE_SCREEN,
                    value ? 1 : 0);
            return true;
        } else if (preference == mVolBtnMusicCtrlPref) {
            value = mVolBtnMusicCtrlPref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.VOLBTN_MUSIC_CONTROLS,
                    value ? 1 : 0);
            return true;
        } else if (preference == mCamBtnMusicCtrlPref) {
            value = mCamBtnMusicCtrlPref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.CAMBTN_MUSIC_CONTROLS,
                    value ? 1 : 0);
            return true;
        } else if (preference == mVolBtnOrientationPref) {
            SystemProperties.set(VOLBTN_ORIENT_PERSIST_PROP,
                    mVolBtnOrientationPref.isChecked() ? "1" : "0");
            return true;
        } else if (preference == mDockObserverOffPref) {
            SystemProperties.set(DOCK_OBSERVER_OFF_PERSIST_PROP,
                    mDockObserverOffPref.isChecked() ? "1" : "0");
            return true;
        } else if (preference == mUserDefinedKey1Pref) {
            mKeyNumber = 1;
            mPicker.pickShortcut();
            return true;
        } else if (preference == mUserDefinedKey2Pref) {
            mKeyNumber = 2;
            mPicker.pickShortcut();
            return true;
        } else if (preference == mUserDefinedKey3Pref) {
            mKeyNumber = 3;
            mPicker.pickShortcut();
            return true;
        }

        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mKeypadTypePref) {
            String keypadType = (String) newValue;
            SystemProperties.set(KEYPAD_TYPE_PERSIST_PROP, keypadType);
            return true;
        }
        return false;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        String key;
        Preference pref;

        switch (mKeyNumber) {
            case 1:
                key = Settings.System.USER_DEFINED_KEY1_APP;
                pref = mUserDefinedKey1Pref;
                break;
            case 2:
                key = Settings.System.USER_DEFINED_KEY2_APP;
                pref = mUserDefinedKey2Pref;
                break;
            case 3:
                key = Settings.System.USER_DEFINED_KEY3_APP;
                pref = mUserDefinedKey3Pref;
                break;
            default:
                return;
        }

        if (Settings.System.putString(getContentResolver(), key, uri)) {
            pref.setSummary(friendlyName);
        }
    }
}
