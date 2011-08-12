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

package com.cyanogenmod.cmparts.activities.led;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;

import com.cyanogenmod.cmparts.R;
import com.cyanogenmod.cmparts.activities.ColorPickerDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PackageSettingsActivity extends PreferenceActivity implements
            Preference.OnPreferenceChangeListener {

    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_BLINK = "blink";
    public static final String EXTRA_COLOR = "color";
    public static final String EXTRA_FORCE_MODE = "forcemode";
    public static final String EXTRA_PACKAGE = "package";
    public static final String EXTRA_TITLE = "title";

    private static final String COLOR_RANDOM = "random";
    private static final String VALUE_DEFAULT = "default";

    private Set<String> mCategories;
    private SharedPreferences mPrefs;

    private int[] mColorList;

    private ListPreference mCategoryPref;
    private ListPreference mForceModePref;
    private ListPreference mColorPref;
    private ListPreference mBlinkPref;
    private Preference mCustomPref;
    private Preference mTestPref;
    private Preference mResetPref;

    private Intent mResultIntent = new Intent(Intent.ACTION_EDIT);
    private static final int NOTIFICATION_ID = 400;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.led_package);
        setResult(RESULT_CANCELED);

        mCategoryPref = (ListPreference) findPreference("category");
        mCategoryPref.setOnPreferenceChangeListener(this);
        populateCategories();

        mForceModePref = (ListPreference) findPreference("force_mode");
        mForceModePref.setOnPreferenceChangeListener(this);

        mColorPref = (ListPreference) findPreference("color");
        mColorPref.setOnPreferenceChangeListener(this);
        populateColors();

        mBlinkPref = (ListPreference) findPreference("blink");
        mCustomPref = findPreference("custom_color");
        mTestPref = findPreference("test_color");
        mResetPref = findPreference("reset");

        if (getResources().getBoolean(R.bool.has_rgb_notification_led)) {
            mBlinkPref.setOnPreferenceChangeListener(this);
        } else {
            PreferenceScreen screen = getPreferenceScreen();
            screen.removePreference(mBlinkPref);
            screen.removePreference(mCustomPref);
        }

        String[] colorList = getResources().getStringArray(
                com.android.internal.R.array.notification_led_random_color_set);
        mColorList = new int[colorList.length];
        for (int i = 0; i < colorList.length; i++) {
            mColorList[i] = Color.parseColor(colorList[i]);
        }

        loadInitialData();
    }

    private void loadInitialData() {
        Intent intent = getIntent();

        setTitle(intent.getStringExtra(EXTRA_TITLE));
        mResultIntent.putExtra(EXTRA_PACKAGE, intent.getStringExtra(EXTRA_PACKAGE));

        setPrefWithDefault(mCategoryPref, intent, EXTRA_CATEGORY);
        setPrefWithDefault(mBlinkPref, intent, EXTRA_BLINK);
        setPrefWithDefault(mForceModePref, intent, EXTRA_FORCE_MODE);
        setPrefWithDefault(mColorPref, intent, EXTRA_COLOR);
        updateEnabledStates(mForceModePref.getValue());
    }

    private void setPrefWithDefault(ListPreference pref, Intent data, String field) {
        String value = data.getStringExtra(field);
        if (value != null) {
            pref.setValue(value);
        } else {
            pref.setValueIndex(0);
            mResultIntent.putExtra(field, pref.getValue());
        }
    }

    private void updateEnabledStates(String forceModeValue) {
        boolean isOn = !TextUtils.equals(forceModeValue, "forceoff");
        mCustomPref.setEnabled(isOn);
        mTestPref.setEnabled(isOn);
        mColorPref.setEnabled(isOn);
        mBlinkPref.setEnabled(isOn);
    }

    private void populateCategories() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String[] catList = CategoryActivity.fetchCategories(prefs);
        List<String> categories = new ArrayList<String>(Arrays.asList(catList));

        categories.add(0, ""); /* empty string = default category */
        mCategoryPref.setEntryValues(categories.toArray(new String[0]));

        categories.set(0, getResources().getString(R.string.trackball_category_misc));
        mCategoryPref.setEntries(categories.toArray(new String[0]));
    }

    private void populateColors() {
        if (getResources().getBoolean(R.bool.has_mixable_dual_notification_led)) {
            mColorPref.setEntries(R.array.entries_mixable_dual_led_colors);
            mColorPref.setEntryValues(R.array.values_mixable_dual_led_colors);
        } else if (getResources().getBoolean(R.bool.has_dual_notification_led)) {
            mColorPref.setEntries(R.array.entries_dual_led_colors);
            mColorPref.setEntryValues(R.array.values_dual_led_colors);
        } else {
            mColorPref.setEntries(R.array.entries_trackball_colors);
            mColorPref.setEntryValues(R.array.pref_trackball_colors_values);
        }
    }

    private String findSetting(ListPreference pref, String name) {
        String value = mResultIntent.getStringExtra(name);
        if (value != null) {
            return value;
        }
        return pref.getValue();
    }

    private void doTest() {
        final int alwaysPulse = Settings.System.getInt(
                getContentResolver(), Settings.System.TRACKBALL_SCREEN_ON, 0);
        String color = findSetting(mColorPref, EXTRA_COLOR);
        String blink = findSetting(mBlinkPref, EXTRA_BLINK);

        if (color == null || blink == null) {
            return;
        }

        final Notification notification = new Notification();
        final NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notification.flags |= Notification.FLAG_SHOW_LIGHTS;

        if (blink.equals(VALUE_DEFAULT)) {
            notification.ledOnMS =
                    getResources().getInteger(com.android.internal.R.integer.config_defaultNotificationLedOn);
            notification.ledOffMS =
                    getResources().getInteger(com.android.internal.R.integer.config_defaultNotificationLedOff);
        } else {
            notification.ledOnMS = 500;
            notification.ledOffMS = Integer.parseInt(blink) * 1000;
        }

        if (color.equals(VALUE_DEFAULT)) {
            notification.ledARGB =
                getResources().getColor(com.android.internal.R.color.config_defaultNotificationColor);
        } else if (color.equals(COLOR_RANDOM)) {
            Random generator = new Random();
            int x = generator.nextInt(mColorList.length - 1);
            notification.ledARGB = mColorList[x];
        } else {
            notification.ledARGB = Color.parseColor(color);
        }

        if (alwaysPulse != 1) {
            Settings.System.putInt(getContentResolver(), Settings.System.TRACKBALL_SCREEN_ON, 1);
        }
        nm.notify(NOTIFICATION_ID, notification);

        AlertDialog.Builder endFlash = new AlertDialog.Builder(this);
        endFlash.setMessage(R.string.dialog_clear_flash);
        endFlash.setCancelable(false);
        endFlash.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                nm.cancel(NOTIFICATION_ID);
                if (alwaysPulse != 1) {
                    Settings.System.putInt(getContentResolver(), Settings.System.TRACKBALL_SCREEN_ON, 0);
                }
            }
        });
        endFlash.show();
    }

    private void doReset() {
        mResultIntent.setAction(Intent.ACTION_DELETE);
        setResult(RESULT_OK, mResultIntent);
        finish();
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object objValue) {
        if (pref == mBlinkPref) {
            updateResult(EXTRA_BLINK, (String) objValue);
        } else if (pref == mCategoryPref) {
            updateResult(EXTRA_CATEGORY, (String) objValue);
        } else if (pref == mColorPref) {
            updateResult(EXTRA_COLOR, (String) objValue);
        } else if (pref == mForceModePref) {
            String value = (String) objValue;
            updateResult(EXTRA_FORCE_MODE, value);
            updateEnabledStates(value);
        }

        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference pref) {
        if (pref == mCustomPref) {
            String colorValue = findSetting(mColorPref, EXTRA_COLOR);
            int color = -1;

            if (colorValue != null && !colorValue.equals(COLOR_RANDOM)) {
                try {
                    color = Color.parseColor(colorValue);
                } catch (IllegalArgumentException e) {
                }
            }

            ColorPickerDialog cp = new ColorPickerDialog(this, mPackageColorListener, color);
            cp.show();
        } else if (pref == mTestPref) {
            doTest();
        } else if (pref == mResetPref) {
            doReset();
        }

        return false;
    }

    private void updateResult(String field, String value) {
        mResultIntent.putExtra(field, value);
        setResult(RESULT_OK, mResultIntent);
    }

    private ColorPickerDialog.OnColorChangedListener mPackageColorListener =
            new ColorPickerDialog.OnColorChangedListener() {
        @Override
        public void colorUpdate(int color) {
        }

        @Override
        public void colorChanged(int color) {
            String colorString = String.format("#%02x%02x%02x",
                    Color.red(color), Color.green(color), Color.blue(color));
            updateResult(EXTRA_COLOR, colorString);
        }
    };
}
