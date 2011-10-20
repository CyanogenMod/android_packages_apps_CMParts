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

import java.io.File;

import android.app.admin.DevicePolicyManager;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.cyanogenmod.cmparts.R;

public class LockscreenUnlockActivity extends PreferenceActivity {

    private final static String LOCKSCREEN_DISABLE_ON_SECURITY = "pref_lockscreen_disable_on_security";

    private static final String TRACKBALL_UNLOCK_PREF = "pref_trackball_unlock";

    private static final String SLIDER_UNLOCK_PREF = "pref_slider_unlock";

    private static final String MENU_UNLOCK_PREF = "pref_menu_unlock";

    private static final String LOCKSCREEN_QUICK_UNLOCK_CONTROL = "lockscreen_quick_unlock_control";

    private static final String LOCKSCREEN_DISABLE_UNLOCK_TAB = "lockscreen_disable_unlock_tab";

    private static final String LOCKSCREEN_UNLOCK_SETTINGS = "pref_category_unlock_settings";

    private CheckBoxPreference mLockscreenDisableOnSecurity;

    private CheckBoxPreference mTrackballUnlockPref;

    private CheckBoxPreference mSliderUnlockPref;

    private CheckBoxPreference mMenuUnlockPref;

    private CheckBoxPreference mQuickUnlockScreenPref;

    private CheckBoxPreference mDisableUnlockTab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.lockscreen_settings_title_subhead);
        addPreferencesFromResource(R.xml.lockscreen_unlock_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        /* skip lockscreen on pin/pattern/password */
        mLockscreenDisableOnSecurity = (CheckBoxPreference) prefSet
            .findPreference(LOCKSCREEN_DISABLE_ON_SECURITY);
        mLockscreenDisableOnSecurity.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_DISABLE_ON_SECURITY, 0) == 1);

        /* Quick Unlock Screen Control */
        mQuickUnlockScreenPref = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_QUICK_UNLOCK_CONTROL);
        mQuickUnlockScreenPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, 0) == 1);

        /* Trackball Unlock */
        mTrackballUnlockPref = (CheckBoxPreference) prefSet.findPreference(TRACKBALL_UNLOCK_PREF);
        mTrackballUnlockPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.TRACKBALL_UNLOCK_SCREEN, 0) == 1);

        /* Slider Unlock */
        mSliderUnlockPref = (CheckBoxPreference) prefSet.findPreference(SLIDER_UNLOCK_PREF);
        mSliderUnlockPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.SLIDER_UNLOCK_SCREEN, 0) == 1);

        /* Menu Unlock */
        mMenuUnlockPref = (CheckBoxPreference) prefSet.findPreference(MENU_UNLOCK_PREF);
        mMenuUnlockPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.MENU_UNLOCK_SCREEN, 0) == 1);

        /* Disabling of unlock tab on lockscreen */
        mDisableUnlockTab = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_DISABLE_UNLOCK_TAB);
        refreshDisableUnlock();

        PreferenceCategory generalCategory = (PreferenceCategory) prefSet
                .findPreference(LOCKSCREEN_UNLOCK_SETTINGS);

        if (!getResources().getBoolean(R.bool.has_trackball)) {
            generalCategory.removePreference(mTrackballUnlockPref);
        }
        if (!getResources().getBoolean(R.bool.has_slider)) {
            generalCategory.removePreference(mSliderUnlockPref);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        refreshDisableUnlock();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mLockscreenDisableOnSecurity) {
            value = mLockscreenDisableOnSecurity.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_DISABLE_ON_SECURITY, value ? 1 : 0);
            return true;
        } else if (preference == mQuickUnlockScreenPref) {
            value = mQuickUnlockScreenPref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, value ? 1 : 0);
            return true;
        } else if (preference == mTrackballUnlockPref) {
            value = mTrackballUnlockPref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.TRACKBALL_UNLOCK_SCREEN,
                    value ? 1 : 0);
            refreshDisableUnlock();
            return true;
        } else if (preference == mSliderUnlockPref) {
          value = mSliderUnlockPref.isChecked();
          Settings.System.putInt(getContentResolver(), Settings.System.SLIDER_UNLOCK_SCREEN,
                  value ? 1 : 0);
          refreshDisableUnlock();
          return true;
        } else if (preference == mMenuUnlockPref) {
            value = mMenuUnlockPref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.MENU_UNLOCK_SCREEN,
                    value ? 1 : 0);
            refreshDisableUnlock();
            return true;
        } else if (preference == mDisableUnlockTab) {
            value = mDisableUnlockTab.isChecked();
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_GESTURES_DISABLE_UNLOCK, value ? 1 : 0);
        }
        return false;
    }

    void refreshDisableUnlock() {
        if (!doesUnlockAbilityExist()) {
            mDisableUnlockTab.setEnabled(false);
            mDisableUnlockTab.setChecked(false);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.LOCKSCREEN_GESTURES_DISABLE_UNLOCK, 0);
        } else {
            mDisableUnlockTab.setEnabled(true);
        }
    }

    private boolean doesUnlockAbilityExist() {
        final File mStoreFile = new File(Environment.getDataDirectory(),
                "/misc/lockscreen_gestures");
        boolean GestureCanUnlock = false;
        boolean trackCanUnlock = Settings.System.getInt(getContentResolver(),
                Settings.System.TRACKBALL_UNLOCK_SCREEN, 0) == 1;
        boolean menuCanUnlock = Settings.System.getInt(getContentResolver(),
                Settings.System.MENU_UNLOCK_SCREEN, 0) == 1;
        GestureLibrary gl = GestureLibraries.fromFile(mStoreFile);
        if (gl.load()) {
            for (String name : gl.getGestureEntries()) {
                String[] payload = name.split("___", 2);
                if ("UNLOCK".equals(payload[1])) {
                    GestureCanUnlock = true;
                    break;
                }
            }
        }
        if (GestureCanUnlock || trackCanUnlock || menuCanUnlock) {
            return true;
        } else {
            return false;
        }
    }
}
