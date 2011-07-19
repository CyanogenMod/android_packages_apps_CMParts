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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import com.cyanogenmod.cmparts.R;
import com.cyanogenmod.cmparts.utils.ShortcutPickHelper;

public class LockscreenStyleActivity extends PreferenceActivity implements
        OnPreferenceChangeListener, ShortcutPickHelper.OnPickListener {

    private static final int LOCKSCREEN_BACKGROUND = 1024;

    private static final String LOCKSCREEN_STYLE_PREF = "pref_lockscreen_style";

    private static final String IN_CALL_STYLE_PREF = "pref_in_call_style";

    private static final String LOCKSCREEN_CUSTOM_APP_TOGGLE = "pref_lockscreen_custom_app_toggle";

    private static final String LOCKSCREEN_CUSTOM_APP_ACTIVITY = "pref_lockscreen_custom_app_activity";

    private static final String LOCKSCREEN_ROTARY_UNLOCK_DOWN_TOGGLE = "pref_lockscreen_rotary_unlock_down_toggle";

    private static final String LOCKSCREEN_ROTARY_HIDE_ARROWS_TOGGLE = "pref_lockscreen_rotary_hide_arrows_toggle";

    private static final String LOCKSCREEN_CUSTOM_ICON_STYLE = "pref_lockscreen_custom_icon_style";

    private static final String LOCKSCREEN_CUSTOM_BACKGROUND = "pref_lockscreen_background";

    private CheckBoxPreference mCustomAppTogglePref;

    private CheckBoxPreference mRotaryUnlockDownToggle;

    private CheckBoxPreference mRotaryHideArrowsToggle;

    private CheckBoxPreference mCustomIconStyle;

    private ListPreference mLockscreenStylePref;

    private ListPreference mInCallStylePref;

    private Preference mCustomAppActivityPref;

    private ListPreference mCustomBackground;

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
    private ShortcutPickHelper mPicker;

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

        mCustomBackground = (ListPreference) prefSet
        .findPreference(LOCKSCREEN_CUSTOM_BACKGROUND);
        mCustomBackground.setOnPreferenceChangeListener(this);
        mPicker = new ShortcutPickHelper(this, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        String value = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY);
        mCustomAppActivityPref.setSummary(mPicker.getFriendlyNameForUri(value));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == LOCKSCREEN_BACKGROUND)&&(resultCode == RESULT_OK)){
            File lockWall = new File(getApplicationContext().getFilesDir()+"/lockwallpaper");
            lockWall.setReadOnly();
            lockWall.setWritable(true, true);
            Settings.System.putString(getContentResolver(), Settings.System.LOCKSCREEN_BACKGROUND,"");
        }
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
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
            mPicker.pickShortcut();
        }
        return false;
    }

    ColorPickerDialog.OnColorChangedListener mPackageColorListener = new ColorPickerDialog.OnColorChangedListener() {
        public void colorChanged(int color) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_BACKGROUND,color);
        }
        @Override
        public void colorUpdate(int color) {
        }
    };

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String val = newValue.toString();
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
        if (preference == mCustomBackground){
            if (mCustomBackground.findIndexOfValue(val) == 0){
                ColorPickerDialog cp = new ColorPickerDialog(this,mPackageColorListener,
                        Settings.System.getInt(getContentResolver(),Settings.System.LOCKSCREEN_BACKGROUND, 0));
                cp.show();
            }else if (mCustomBackground.findIndexOfValue(val) == 1){
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setClassName("com.cooliris.media", "com.cooliris.media.Gallery");
                PackageManager pm = getPackageManager();
                List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
                if (activities != null && activities.size() > 0) {
                    intent.setType("image/*");
                    intent.putExtra("crop", "true");
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("outputX", getWindowManager().getDefaultDisplay().getWidth());
                    intent.putExtra("outputY", getWindowManager().getDefaultDisplay().getHeight());
                    File lockWall = new File(getApplicationContext().getFilesDir()+"/lockwallpaper");
                    if (!lockWall.exists()){
                        try {
                            lockWall.createNewFile();
                        } catch (IOException e) {
                            return true;
                        }
                    }
                    lockWall.setWritable(true, false);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(lockWall));
                    intent.putExtra("return-data", false);
                    startActivityForResult(intent,LOCKSCREEN_BACKGROUND);
                }else{
                    Toast.makeText(this, "Gallery is not installed", Toast.LENGTH_LONG).show();
                }
            }else if (mCustomBackground.findIndexOfValue(val) == 2){
                Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_BACKGROUND,Color.parseColor("#70000000"));
            }
            return true;
        }
        return false;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        if (Settings.System.putString(getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITY, uri)) {
            mCustomAppActivityPref.setSummary(friendlyName);
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
