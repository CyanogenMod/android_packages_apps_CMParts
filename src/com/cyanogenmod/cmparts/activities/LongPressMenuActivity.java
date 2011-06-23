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
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.cyanogenmod.cmparts.R;

public class LongPressMenuActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    private static final String USER_DEFINED_LONG_PRESS_MENU = "pref_user_defined_long_press_menu";

    private static final String INPUT_CUSTOM_LONG_MENU = "pref_long_press_menu";

    private Preference mUserDefinedLongPressMenu;

    private ListPreference long_menu;

    private static final int REQUEST_PICK_SHORTCUT = 1;
    private static final int REQUEST_PICK_APPLICATION = 2;
    private static final int REQUEST_CREATE_SHORTCUT = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.long_press_menu_title);
        addPreferencesFromResource(R.xml.long_press_menu);

        PreferenceScreen prefSet = getPreferenceScreen();

        long_menu = (ListPreference) prefSet.findPreference(INPUT_CUSTOM_LONG_MENU);

        long_menu.setOnPreferenceChangeListener(this);

        mUserDefinedLongPressMenu = (Preference) prefSet.findPreference(USER_DEFINED_LONG_PRESS_MENU);

        mUserDefinedLongPressMenu.setEnabled((Settings.System.getInt(getContentResolver(),Settings.System.USE_CUSTOM_LONG_MENU, 0))==3);
    }

    @Override
    public void onResume() {
        super.onResume();
        mUserDefinedLongPressMenu.setSummary(Settings.System.getString(getContentResolver(),
                Settings.System.USE_CUSTOM_LONG_MENU_APP_ACTIVITY));
    }

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
        String val = newValue.toString();
        int index = long_menu.findIndexOfValue(val);
        Settings.System.putString(getContentResolver(), Settings.System.USE_CUSTOM_LONG_MENU, val);
        mUserDefinedLongPressMenu.setEnabled((index==3) ? true : false);
        return true;
	}

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mUserDefinedLongPressMenu)
            pickShortcut();
        return true;
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

     private void pickShortcut() {
        Bundle bundle = new Bundle();

        ArrayList<String> shortcutNames = new ArrayList<String>();
        shortcutNames.add(getString(R.string.group_applications));
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);

        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
        shortcutIcons.add(ShortcutIconResource.fromContext(this, R.drawable.ic_launcher_application));
        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);

        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.select_custom_app_title));
        pickIntent.putExtras(bundle);

        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
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
        if (Settings.System.putString(getContentResolver(),
                    Settings.System.USE_CUSTOM_LONG_MENU_APP_ACTIVITY, appUri)) {
                        mUserDefinedLongPressMenu.setSummary(data.toUri(0));
        }
    }

    void completeSetCustomApp(Intent data) {
        String appUri = data.toUri(0);
        appUri = appUri.replaceAll("com.android.contacts.action.QUICK_CONTACT", "android.intent.action.VIEW");
        if (Settings.System.putString(getContentResolver(),
                    Settings.System.USE_CUSTOM_LONG_MENU_APP_ACTIVITY, appUri)) {
                        mUserDefinedLongPressMenu.setSummary(data.toUri(0));
        }
    }

}
