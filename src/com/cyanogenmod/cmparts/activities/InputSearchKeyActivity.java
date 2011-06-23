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
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.cyanogenmod.cmparts.R;

public class InputSearchKeyActivity extends PreferenceActivity {

    private static final String INPUT_CUSTOM_SEARCH_APP_TOGGLE = "pref_input_custom_search_app_toggle";

    private static final String INPUT_CUSTOM_SEARCH_APP_ACTIVITY = "pref_input_custom_search_app_activity";

    private static final String INPUT_CUSTOM_LONG_SEARCH_APP_TOGGLE = "pref_input_custom_long_search_app_toggle";

    private static final String INPUT_CUSTOM_LONG_SEARCH_APP_ACTIVITY = "pref_input_custom_long_search_app_activity";

    private CheckBoxPreference mCustomSearchAppTogglePref;

    private CheckBoxPreference mCustomLongSearchAppTogglePref;

    private Preference mCustomSearchAppActivityPref;

    private Preference mCustomLongSearchAppActivityPref;

    private int mKeyNumber = 1;

    private static final int REQUEST_PICK_SHORTCUT = 1;

    private static final int REQUEST_PICK_APPLICATION = 2;

    private static final int REQUEST_CREATE_SHORTCUT = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.input_search_key_title);
        addPreferencesFromResource(R.xml.input_search_key_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        /* Search key custom app */
        mCustomSearchAppTogglePref = (CheckBoxPreference) prefSet
                .findPreference(INPUT_CUSTOM_SEARCH_APP_TOGGLE);
        mCustomSearchAppTogglePref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.USE_CUSTOM_SEARCH_APP_TOGGLE, 0) == 1);
        mCustomSearchAppActivityPref = (Preference) prefSet
                .findPreference(INPUT_CUSTOM_SEARCH_APP_ACTIVITY);

        mCustomLongSearchAppTogglePref = (CheckBoxPreference) prefSet
                .findPreference(INPUT_CUSTOM_LONG_SEARCH_APP_TOGGLE);
        mCustomLongSearchAppTogglePref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.USE_CUSTOM_LONG_SEARCH_APP_TOGGLE, 0) == 1);
        mCustomLongSearchAppActivityPref = (Preference) prefSet
                .findPreference(INPUT_CUSTOM_LONG_SEARCH_APP_ACTIVITY);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCustomSearchAppActivityPref.setSummary(Settings.System.getString(getContentResolver(),
                Settings.System.USE_CUSTOM_SEARCH_APP_ACTIVITY));
        mCustomLongSearchAppActivityPref.setSummary(Settings.System.getString(getContentResolver(),
                Settings.System.USE_CUSTOM_LONG_SEARCH_APP_ACTIVITY));
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mCustomSearchAppTogglePref) {
            value = mCustomSearchAppTogglePref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.USE_CUSTOM_SEARCH_APP_TOGGLE, value ? 1 : 0);
            if (mCustomSearchAppTogglePref.isChecked()){
                mCustomLongSearchAppTogglePref.setChecked(false);
                Settings.System.putInt(getContentResolver(), Settings.System.USE_CUSTOM_LONG_SEARCH_APP_TOGGLE, 0);
            }
            return true;
        } else if (preference == mCustomLongSearchAppTogglePref) {
            value = mCustomLongSearchAppTogglePref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.USE_CUSTOM_LONG_SEARCH_APP_TOGGLE, value ? 1 : 0);
            if (mCustomLongSearchAppTogglePref.isChecked()){
                mCustomSearchAppTogglePref.setChecked(false);
                Settings.System.putInt(getContentResolver(), Settings.System.USE_CUSTOM_SEARCH_APP_TOGGLE, 0);
            }
            return true;
        } else if (preference == mCustomSearchAppActivityPref) {
            pickShortcut(1);
            return true;
        } else if (preference == mCustomLongSearchAppActivityPref) {
            pickShortcut(2);
            return true;
        }

        return false;
    }

    private void pickShortcut(int keyNumber) {
        mKeyNumber = keyNumber;
        Bundle bundle = new Bundle();
        ArrayList<String> shortcutNames = new ArrayList<String>();
        shortcutNames.add(getString(R.string.group_applications));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);
        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
        shortcutIcons.add(ShortcutIconResource
                .fromContext(this, R.drawable.ic_launcher_application));
        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);
        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.select_custom_app_title));
        pickIntent.putExtras(bundle);
        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_APPLICATION:
                    completeSetCustomApp(data);
                    break;
                case REQUEST_CREATE_SHORTCUT:
                    completeSetCustomShortcut(data);
                    break;
                case REQUEST_PICK_SHORTCUT:
                    processShortcut(data, REQUEST_PICK_APPLICATION, REQUEST_CREATE_SHORTCUT);
                    break;
            }
        }
    }

    void processShortcut(Intent intent, int requestCodeApplication, int requestCodeShortcut) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            startActivityForResult(pickIntent, requestCodeApplication);
        } else {
            startActivityForResult(intent, requestCodeShortcut);
        }
    }

    void completeSetCustomShortcut(Intent data) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String appUri = intent.toUri(0);
        appUri = appUri.replaceAll("com.android.contacts.action.QUICK_CONTACT", "android.intent.action.VIEW");
        int keyNumber = mKeyNumber;
        if (keyNumber == 1) {
            if (Settings.System.putString(getContentResolver(),
                    Settings.System.USE_CUSTOM_SEARCH_APP_ACTIVITY, appUri)) {
                mCustomSearchAppActivityPref.setSummary(intent.toUri(0));
            }
        } else if (keyNumber == 2) {
            if (Settings.System.putString(getContentResolver(),
                    Settings.System.USE_CUSTOM_LONG_SEARCH_APP_ACTIVITY, appUri)) {
                mCustomLongSearchAppActivityPref.setSummary(intent.toUri(0));
            }
        }

    }

    void completeSetCustomApp(Intent data) {
        String appUri = data.toUri(0);
        appUri = appUri.replaceAll("com.android.contacts.action.QUICK_CONTACT", "android.intent.action.VIEW");
        int keyNumber = mKeyNumber;
        if (keyNumber == 1) {
            if (Settings.System.putString(getContentResolver(),
                    Settings.System.USE_CUSTOM_SEARCH_APP_ACTIVITY, appUri)) {
                mCustomSearchAppActivityPref.setSummary(data.toUri(0));
            }
        } else if (keyNumber == 2) {
            if (Settings.System.putString(getContentResolver(),
                    Settings.System.USE_CUSTOM_LONG_SEARCH_APP_ACTIVITY, appUri)) {
                mCustomLongSearchAppActivityPref.setSummary(data.toUri(0));
            }
        }

    }
}
