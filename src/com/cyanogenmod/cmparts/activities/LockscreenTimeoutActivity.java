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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.internal.widget.LockPatternUtils;
import com.cyanogenmod.cmparts.R;

public class LockscreenTimeoutActivity extends PreferenceActivity implements
        OnPreferenceChangeListener {

    private static final String LOCKSCREEN_SECURITY_CATEGORY_PREF = "pref_lockscreen_delay_category_security";

    private static final String LOCKSCREEN_SECURITY_NOTE_PREF = "pref_lockscreen_security_delay_note";

    private static final String LOCKSCREEN_SECURITY_TIMEOUT_DELAY_PREF = "pref_lockscreen_security_timeout_delay";

    private static final String LOCKSCREEN_SECURITY_SCREENOFF_DELAY_PREF = "pref_lockscreen_security_screenoff_delay";

    private static final String LOCKSCREEN_SLIDE_CATEGORY_PREF = "pref_lockscreen_delay_category_slide";

    private static final String LOCKSCREEN_SLIDE_NOTE_PREF = "pref_lockscreen_slide_delay_note";

    private static final String LOCKSCREEN_SLIDE_DELAY_TOGGLE_PREF = "pref_lockscreen_slide_delay_toggle";

    private static final String LOCKSCREEN_SLIDE_TIMEOUT_DELAY_PREF = "pref_lockscreen_slide_timeout_delay";

    private static final String LOCKSCREEN_SLIDE_SCREENOFF_DELAY_PREF = "pref_lockscreen_slide_screenoff_delay";

    private LockPatternUtils mLockPatternUtils;

    private PreferenceCategory mSlideLockPreferenceCategory;

    private ListPreference mScreenLockSecurityTimeoutDelayPref;

    private ListPreference mScreenLockSecurityScreenOffDelayPref;

    private PreferenceCategory mSecurityLockPreferenceCategory;

    private CheckBoxPreference mScreenLockSlideDelayTogglePref;

    private ListPreference mScreenLockSlideTimeoutDelayPref;

    private ListPreference mScreenLockSlideScreenOffDelayPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLockPatternUtils = new LockPatternUtils(this);

        setTitle(R.string.lockscreen_settings_title_subhead);
        addPreferencesFromResource(R.xml.lockscreen_timeout_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        /* Screen Lock */
        mSecurityLockPreferenceCategory = (PreferenceCategory) prefSet
                .findPreference(LOCKSCREEN_SECURITY_CATEGORY_PREF);

        mScreenLockSecurityTimeoutDelayPref = (ListPreference) prefSet
                .findPreference(LOCKSCREEN_SECURITY_TIMEOUT_DELAY_PREF);
        int securityTimeoutDelay = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_LOCK_SECURITY_TIMEOUT_DELAY, 5000);
        mScreenLockSecurityTimeoutDelayPref.setValue(String.valueOf(securityTimeoutDelay));
        mScreenLockSecurityTimeoutDelayPref.setOnPreferenceChangeListener(this);

        mScreenLockSecurityScreenOffDelayPref = (ListPreference) prefSet
                .findPreference(LOCKSCREEN_SECURITY_SCREENOFF_DELAY_PREF);
        int securityScreenOffDelay = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_LOCK_SECURITY_SCREENOFF_DELAY, 0);
        mScreenLockSecurityScreenOffDelayPref.setValue(String.valueOf(securityScreenOffDelay));
        mScreenLockSecurityScreenOffDelayPref.setOnPreferenceChangeListener(this);

        mSlideLockPreferenceCategory = (PreferenceCategory) prefSet
                .findPreference(LOCKSCREEN_SLIDE_CATEGORY_PREF);

        mScreenLockSlideDelayTogglePref = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_SLIDE_DELAY_TOGGLE_PREF);
        mScreenLockSlideDelayTogglePref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_LOCK_SLIDE_DELAY_TOGGLE, 0) == 1);

        mScreenLockSlideTimeoutDelayPref = (ListPreference) prefSet
                .findPreference(LOCKSCREEN_SLIDE_TIMEOUT_DELAY_PREF);
        int slideTimeoutDelay = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_LOCK_SLIDE_TIMEOUT_DELAY, 5000);
        mScreenLockSlideTimeoutDelayPref.setValue(String.valueOf(slideTimeoutDelay));
        mScreenLockSlideTimeoutDelayPref.setOnPreferenceChangeListener(this);

        mScreenLockSlideScreenOffDelayPref = (ListPreference) prefSet
                .findPreference(LOCKSCREEN_SLIDE_SCREENOFF_DELAY_PREF);
        int slideScreenOffDelay = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_LOCK_SLIDE_SCREENOFF_DELAY, 0);
        mScreenLockSlideScreenOffDelayPref.setValue(String.valueOf(slideScreenOffDelay));
        mScreenLockSlideScreenOffDelayPref.setOnPreferenceChangeListener(this);

        if (mLockPatternUtils.isLockPatternEnabled() || mLockPatternUtils.isLockPasswordEnabled()) {
            // Remove the note explaining the semantic boundary case
            mSecurityLockPreferenceCategory.removePreference(
                    prefSet.findPreference(LOCKSCREEN_SECURITY_NOTE_PREF));
        } else {
            // Lock screen type is set to NONE, so disable slide lock components
            mSlideLockPreferenceCategory.setEnabled(false);
            // Also remove the inapplicable semantic note
            mSlideLockPreferenceCategory.removePreference(prefSet
                    .findPreference(LOCKSCREEN_SLIDE_NOTE_PREF));
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mScreenLockSlideDelayTogglePref) {
            value = mScreenLockSlideDelayTogglePref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_LOCK_SLIDE_DELAY_TOGGLE, value ? 1 : 0);
            return true;
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mScreenLockSecurityTimeoutDelayPref) {
            int securityTimeoutDelay = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_LOCK_SECURITY_TIMEOUT_DELAY,
                    securityTimeoutDelay);
            return true;
        } else if (preference == mScreenLockSecurityScreenOffDelayPref) {
            int securityScreenOffDelay = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_LOCK_SECURITY_SCREENOFF_DELAY, securityScreenOffDelay);
            return true;
        } else if (preference == mScreenLockSlideTimeoutDelayPref) {
            int slideTimeoutDelay = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_LOCK_SLIDE_TIMEOUT_DELAY,
                    slideTimeoutDelay);
            return true;
        } else if (preference == mScreenLockSlideScreenOffDelayPref) {
            int slideScreenOffDelay = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_LOCK_SLIDE_SCREENOFF_DELAY, slideScreenOffDelay);
            return true;
        }

        return false;
    }

}
