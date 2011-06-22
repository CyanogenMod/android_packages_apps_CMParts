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
import com.cyanogenmod.cmparts.utils.ShortcutPickHelper;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import java.util.ArrayList;

public class LongPressHomeActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener, ShortcutPickHelper.OnPickListener {

    private static final String RECENT_APPS_SHOW_TITLE_PREF = "pref_show_recent_apps_title";
    private static final String RECENT_APPS_NUM_PREF= "pref_recent_apps_num";
    private static final String USE_CUSTOM_APP_PREF = "pref_use_custom_app";
    private static final String SELECT_CUSTOM_APP_PREF = "pref_select_custom_app";    
    
    private CheckBoxPreference mShowRecentAppsTitlePref;
    private ListPreference mRecentAppsNumPref;
    private CheckBoxPreference mUseCustomAppPref;
    private Preference mSelectCustomAppPref;
    private ShortcutPickHelper mPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.long_press_home_title);
        addPreferencesFromResource(R.xml.long_press_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        
        mShowRecentAppsTitlePref = (CheckBoxPreference) prefSet.findPreference(RECENT_APPS_SHOW_TITLE_PREF);        

        mRecentAppsNumPref = (ListPreference) prefSet.findPreference(RECENT_APPS_NUM_PREF);
        mRecentAppsNumPref.setOnPreferenceChangeListener(this);
        
        mUseCustomAppPref = (CheckBoxPreference) prefSet.findPreference(USE_CUSTOM_APP_PREF);        
        mSelectCustomAppPref = (Preference) prefSet.findPreference(SELECT_CUSTOM_APP_PREF);

        //final PreferenceGroup parentPreference = getPreferenceScreen();
        //parentPreference.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        mPicker = new ShortcutPickHelper(this, this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mUseCustomAppPref.setChecked(Settings.System.getInt(getContentResolver(), Settings.System.USE_CUSTOM_APP, 0) == 1);

        String value = Settings.System.getString(getContentResolver(), Settings.System.SELECTED_CUSTOM_APP);
        mSelectCustomAppPref.setSummary(mPicker.getFriendlyNameForUri(value));

        readRecentAppsNumPreference();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mRecentAppsNumPref) {
            writeRecentAppsNumPreference(objValue);
        }
        
        // always let the preference setting proceed.
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mUseCustomAppPref) {
            Settings.System.putInt(getContentResolver(), Settings.System.USE_CUSTOM_APP, mUseCustomAppPref.isChecked() ? 1 : 0);
            if (mUseCustomAppPref.isChecked()){
                mShowRecentAppsTitlePref.setChecked(false);
                Settings.System.putInt(getContentResolver(), Settings.System.RECENT_APPS_SHOW_TITLE, 0);
            }
        }
        else if (preference == mShowRecentAppsTitlePref) {
            Settings.System.putInt(getContentResolver(), Settings.System.RECENT_APPS_SHOW_TITLE , mShowRecentAppsTitlePref.isChecked() ? 1 : 0);
            if (mShowRecentAppsTitlePref.isChecked()){
                mUseCustomAppPref.setChecked(false);
                Settings.System.putInt(getContentResolver(), Settings.System.USE_CUSTOM_APP, 0);
            }
        }
        else if (preference == mSelectCustomAppPref) {
            mPicker.pickShortcut();
        }
        return true;
    }    

    public void readRecentAppsNumPreference() {
        try {
            int value = Settings.System.getInt(getContentResolver(), Settings.System.RECENT_APPS_NUMBER);
            mRecentAppsNumPref.setValue(Integer.toString(value));
        } catch (SettingNotFoundException e) {
            mRecentAppsNumPref.setValue("8");
        }
    }
    
    private void writeRecentAppsNumPreference(Object objValue) {
        try {
            int val = Integer.parseInt(objValue.toString());
            Settings.System.putInt(getContentResolver(), Settings.System.RECENT_APPS_NUMBER, val);
        } catch (NumberFormatException e) {
        }
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        if (Settings.System.putString(getContentResolver(), Settings.System.SELECTED_CUSTOM_APP, uri)) {
            mSelectCustomAppPref.setSummary(friendlyName);
        }
    }
}
