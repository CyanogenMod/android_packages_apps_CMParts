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

public class LockscreenStyleActivity extends PreferenceActivity implements
        OnPreferenceChangeListener {

    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";

    private static final String IN_CALL_STYLE_PREF = "pref_in_call_style";

    private static final String LOCKSCREEN_CUSTOM_APP_TOGGLE = "pref_lockscreen_custom_app_toggle";

    private static final String LOCKSCREEN_CUSTOM_APP_ACTIVITY = "pref_lockscreen_custom_app_activity";

    private static final String LOCKSCREEN_ROTARY_UNLOCK_DOWN_TOGGLE = "pref_lockscreen_rotary_unlock_down_toggle";

    private static final String LOCKSCREEN_ROTARY_HIDE_ARROWS_TOGGLE = "pref_lockscreen_rotary_hide_arrows_toggle";

    private static final String LOCKSCREEN_CUSTOM_ICON_STYLE = "pref_lockscreen_custom_icon_style";

    private CheckBoxPreference mCustomAppTogglePref;

    private CheckBoxPreference mRotaryUnlockDownToggle;

    private CheckBoxPreference mRotaryHideArrowsToggle;

    private CheckBoxPreference mCustomIconStyle;

    private ListPreference mLockscreenStylePref;

    private ListPreference mInCallStylePref;

    private Preference mCustomAppActivityPref;

    private int mKeyNumber = 1;

    private static final int REQUEST_PICK_SHORTCUT = 1;

    private static final int REQUEST_PICK_APPLICATION = 2;

    private static final int REQUEST_CREATE_SHORTCUT = 3;

    enum LockscreenStyle{
        Slider,
        Rotary,
        RotaryRevamped,
        Lense;

        static public LockscreenStyle getStyleById(int id){
            switch (id){
                case 1:
                    return Slider;
                case 2:
                    return Rotary;
                case 3:
                    return RotaryRevamped;
                case 4:
                    return Lense;
                default:
                    return RotaryRevamped;
            }
        }

        static public LockscreenStyle getStyleById(String id){
            return getStyleById(Integer.valueOf(id));
        }

        static public int getIdByStyle(LockscreenStyle lockscreenstyle){
            switch (lockscreenstyle){
                case Slider:
                    return 1;
                case Rotary:
                    return 2;
                case RotaryRevamped:
                    return 3;
                case Lense:
                    return 4;
                default:
                    return 3;
            }
        }
    }

    enum InCallStyle {
        Slider,
        Rotary,
        RotaryRevamped;

        static public InCallStyle getStyleById(int id){
            switch (id){
                case 1:
                    return Slider;
                case 2:
                    return Rotary;
                case 3:
                    return RotaryRevamped;
                default:
                    return RotaryRevamped;
            }
        }

        static public InCallStyle getStyleById(String id){
            return getStyleById(Integer.valueOf(id));
        }

        static public int getIdByStyle(InCallStyle inCallStyle){
            switch (inCallStyle){
                case Slider:
                    return 1;
                case Rotary:
                    return 2;
                case RotaryRevamped:
                    return 3;
                default:
                    return 3;
            }
        }
    }

    private LockscreenStyle mLockscreenStyle;
    private InCallStyle mInCallStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.lockscreen_settings_title_subhead);
        addPreferencesFromResource(R.xml.lockscreen_style_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        /* Lockscreen Style and related related settings */
        mLockscreenStylePref = (ListPreference) prefSet.findPreference(LOCKSCREEN_STYLE_PREF);
        mLockscreenStyle = LockscreenStyle.getStyleById(
                Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_STYLE_PREF, 3));
        mLockscreenStylePref.setValue(String.valueOf(LockscreenStyle.getIdByStyle(mLockscreenStyle)));
        mLockscreenStylePref.setOnPreferenceChangeListener(this);

        mInCallStylePref = (ListPreference) prefSet.findPreference(IN_CALL_STYLE_PREF);
        mInCallStyle = InCallStyle.getStyleById(
                Settings.System.getInt(getContentResolver(),
                Settings.System.IN_CALL_STYLE_PREF, 3));
        mInCallStylePref.setValue(String.valueOf(InCallStyle.getIdByStyle(mInCallStyle)));
        mInCallStylePref.setOnPreferenceChangeListener(this);

        mRotaryUnlockDownToggle = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_ROTARY_UNLOCK_DOWN_TOGGLE);
        mRotaryUnlockDownToggle.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN, 0) == 1);

        mRotaryHideArrowsToggle = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_ROTARY_HIDE_ARROWS_TOGGLE);
        mRotaryHideArrowsToggle.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_ROTARY_HIDE_ARROWS, 0) == 1);

        mCustomAppTogglePref = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_CUSTOM_APP_TOGGLE);
        mCustomAppTogglePref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE, 0) == 1);

        mCustomIconStyle = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_CUSTOM_ICON_STYLE);
        mCustomIconStyle.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_ICON_STYLE, 1) == 2);

        updateStylePrefs(mLockscreenStyle, mInCallStyle);

        mCustomAppActivityPref = (Preference) prefSet
                .findPreference(LOCKSCREEN_CUSTOM_APP_ACTIVITY);
    }

    @Override
    public void onResume() {
        super.onResume();

        mCustomAppActivityPref.setSummary(Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY));
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mCustomAppTogglePref) {
            value = mCustomAppTogglePref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE, value ? 1 : 0);
            updateStylePrefs(mLockscreenStyle, mInCallStyle);
            return true;
        } else if (preference == mRotaryUnlockDownToggle) {
            value = mRotaryUnlockDownToggle.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN, value ? 1 : 0);
            return true;
        } else if (preference == mRotaryHideArrowsToggle) {
            value = mRotaryHideArrowsToggle.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_ROTARY_HIDE_ARROWS, value ? 1 : 0);
            return true;
        } else if (preference == mCustomIconStyle) {
            value = mCustomIconStyle.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_ICON_STYLE, value ? 2 : 1);
            return true;
        } else if (preference == mCustomAppActivityPref) {
            pickShortcut(4);
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockscreenStylePref) {
            mLockscreenStyle = LockscreenStyle.getStyleById((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_STYLE_PREF,
                    LockscreenStyle.getIdByStyle(mLockscreenStyle));
            updateStylePrefs(mLockscreenStyle, mInCallStyle);
            return true;
        }
        if (preference == mInCallStylePref) {
            mInCallStyle = InCallStyle.getStyleById((String) newValue);
            Settings.System.putInt(getContentResolver(), Settings.System.IN_CALL_STYLE_PREF,
                    InCallStyle.getIdByStyle(mInCallStyle));
            updateStylePrefs(mLockscreenStyle, mInCallStyle);
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
        int keyNumber = mKeyNumber;
        if (keyNumber == 4) {
            if (Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY, intent.toUri(0))) {
                mCustomAppActivityPref.setSummary(intent.toUri(0));
            }
        }
    }

    void completeSetCustomApp(Intent data) {
        int keyNumber = mKeyNumber;
        if (keyNumber == 4) {
            if (Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY, data.toUri(0))) {
                mCustomAppActivityPref.setSummary(data.toUri(0));
            }
        }
    }

    private void updateStylePrefs(LockscreenStyle lockscreenStyle, InCallStyle inCallStyle) {
        // slider style & lense style
        if (lockscreenStyle == LockscreenStyle.Slider
                || lockscreenStyle == LockscreenStyle.Lense) {
            if(inCallStyle==InCallStyle.Slider){
                mRotaryHideArrowsToggle.setChecked(false);
                mRotaryHideArrowsToggle.setEnabled(false);
            }else{
                mRotaryHideArrowsToggle.setEnabled(true);
            }
            mRotaryUnlockDownToggle.setChecked(false);
            mRotaryUnlockDownToggle.setEnabled(false);
        // rotary and rotary revamped style
        } else if (lockscreenStyle == LockscreenStyle.Rotary
                || lockscreenStyle == LockscreenStyle.RotaryRevamped) {
            mRotaryHideArrowsToggle.setEnabled(true);
            if (mCustomAppTogglePref.isChecked() == true) {
                mRotaryUnlockDownToggle.setEnabled(true);
            } else {
                mRotaryUnlockDownToggle.setChecked(false);
                mRotaryUnlockDownToggle.setEnabled(false);
            }
        }
        // disable custom app starter for lense - would be ugly in above if
        // statement
        if (lockscreenStyle == LockscreenStyle.Lense) {
            mCustomIconStyle.setChecked(false);
            mCustomAppTogglePref.setChecked(false);
            mCustomAppTogglePref.setEnabled(false);
        } else {
            mCustomAppTogglePref.setEnabled(true);
        }

        // make sure toggled settings are saved to system settings
        boolean value = mRotaryUnlockDownToggle.isChecked();
        Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_ROTARY_UNLOCK_DOWN,
                value ? 1 : 0);
        value = mRotaryHideArrowsToggle.isChecked();
        Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_ROTARY_HIDE_ARROWS,
                value ? 1 : 0);
        value = mCustomAppTogglePref.isChecked();
        Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_CUSTOM_APP_TOGGLE,
                value ? 1 : 0);
        value = mCustomIconStyle.isChecked();
        Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_CUSTOM_ICON_STYLE,
                value ? 2 : 1);
    }
}
