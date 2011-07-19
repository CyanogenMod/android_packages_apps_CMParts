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

import android.app.admin.DevicePolicyManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.cyanogenmod.cmparts.R;

public class LockscreenActivity extends PreferenceActivity {

    private final static String LOCKSCREEN_DISABLE_ON_SECURITY = "pref_lockscreen_disable_on_security";

    private final static String PASSWORD_TYPE_KEY = "lockscreen.password_type";

    private CheckBoxPreference mLockscreenDisableOnSecurity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.lockscreen_settings_title_subhead);
        addPreferencesFromResource(R.xml.lockscreen_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        mLockscreenDisableOnSecurity = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_DISABLE_ON_SECURITY);

        int LockscreenDisableOnSecurityValue = Settings.System.getInt(
                getContentResolver(), Settings.System.LOCKSCREEN_DISABLE_ON_SECURITY, 3);

        if (LockscreenDisableOnSecurityValue == 3) {
            // We don't have the option set, check if pattern security is
            // enabled and set the option accordingly
            final boolean usingLockPattern = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCK_PATTERN_ENABLED, 0) == 1
                    && Settings.Secure.getLong(getContentResolver(), PASSWORD_TYPE_KEY,
                            DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING;

            if (usingLockPattern) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.LOCKSCREEN_DISABLE_ON_SECURITY, 1);
                LockscreenDisableOnSecurityValue = 1;
            } else {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.LOCKSCREEN_DISABLE_ON_SECURITY, 0);
                LockscreenDisableOnSecurityValue = 0;
            }
        }
        mLockscreenDisableOnSecurity.setChecked(LockscreenDisableOnSecurityValue == 1);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mLockscreenDisableOnSecurity) {
            value = mLockscreenDisableOnSecurity.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_DISABLE_ON_SECURITY, value ? 1 : 0);
            return true;
        }
        return false;
    }
}
