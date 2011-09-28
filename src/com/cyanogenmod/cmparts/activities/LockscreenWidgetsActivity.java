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

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.BaseColumns;
import android.provider.Calendar;
import android.provider.Settings;

import com.cyanogenmod.cmparts.R;

import java.util.ArrayList;
import java.util.List;

public class LockscreenWidgetsActivity extends PreferenceActivity implements
        OnPreferenceChangeListener {

    private static final String LOCKSCREEN_MUSIC_CONTROLS = "lockscreen_music_controls";

    private static final String LOCKSCREEN_NOW_PLAYING = "pref_lockscreen_now_playing";

    private static final String LOCKSCREEN_ALBUM_ART = "pref_lockscreen_album_art";

    private static final String LOCKSCREEN_MUSIC_CONTROLS_HEADSET = "pref_lockscreen_music_headset";

    private static final String LOCKSCREEN_ALWAYS_MUSIC_CONTROLS = "lockscreen_always_music_controls";

    private static final String LOCKSCREEN_ALWAYS_BATTERY = "lockscreen_always_battery";

    private static final String LOCKSCREEN_CALENDARS = "lockscreen_calendars";

    private static final String LOCKSCREEN_CALENDAR_ALARM = "lockscreen_calendar_alarm";

    private static final String LOCKSCREEN_CALENDAR_REMINDERS_ONLY = "lockscreen_calendar_reminders_only";

    private static final String LOCKSCREEN_CALENDAR_LOOKAHEAD = "lockscreen_calendar_lookahead";

    private static final String LOCKSCREEN_WIDGETS_LAYOUT = "pref_lockscreen_widgets_layout";

    private CheckBoxPreference mMusicControlPref;

    private CheckBoxPreference mNowPlayingPref;

    private CheckBoxPreference mAlbumArtPref;

    private CheckBoxPreference mAlwaysMusicControlPref;

    private CheckBoxPreference mAlwaysBatteryPref;

    private CheckBoxPreference mCalendarAlarmPref;

    private CheckBoxPreference mCalendarRemindersOnlyPref;

    private ListPreference mLockscreenMusicHeadsetPref;

    private MultiSelectListPreference mCalendarsPref;

    private ListPreference mCalendarAlarmLookaheadPref;

    private ListPreference mLockscreenWidgetLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.lockscreen_settings_title_subhead);
        addPreferencesFromResource(R.xml.lockscreen_widgets_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

        /* Music Controls */
        mMusicControlPref = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_MUSIC_CONTROLS);
        mMusicControlPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_MUSIC_CONTROLS, 1) == 1);

        /* Now Playing / Song title */
        mNowPlayingPref = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_NOW_PLAYING);
        mNowPlayingPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_NOW_PLAYING, 1) == 1);

        /* Album Art */
        mAlbumArtPref = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_ALBUM_ART);
        mAlbumArtPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_ALBUM_ART, 1) == 1);

        /* Show Music Controls with Headset */
        mLockscreenMusicHeadsetPref = (ListPreference) prefSet
                .findPreference(LOCKSCREEN_MUSIC_CONTROLS_HEADSET);
        int lockscreenMusicHeadsetPref = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_MUSIC_CONTROLS_HEADSET, 0);
        mLockscreenMusicHeadsetPref.setValue(String.valueOf(lockscreenMusicHeadsetPref));
        mLockscreenMusicHeadsetPref.setOnPreferenceChangeListener(this);

        /* Always Display Music Controls */
        mAlwaysMusicControlPref = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_ALWAYS_MUSIC_CONTROLS);
        boolean alwaysMusicControlPref = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_ALWAYS_MUSIC_CONTROLS, 0) == 1;
        mAlwaysMusicControlPref.setChecked(alwaysMusicControlPref);
        mLockscreenMusicHeadsetPref.setEnabled(!alwaysMusicControlPref);

        mLockscreenWidgetLayout = (ListPreference) prefSet
                .findPreference(LOCKSCREEN_WIDGETS_LAYOUT);
        mLockscreenWidgetLayout.setOnPreferenceChangeListener(this);

        /* Always Display Battery Status */
        mAlwaysBatteryPref = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_ALWAYS_BATTERY);
        mAlwaysBatteryPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_ALWAYS_BATTERY, 0) == 1);

        /* Calendars */
        mCalendarsPref = (MultiSelectListPreference) prefSet.findPreference(LOCKSCREEN_CALENDARS);
        mCalendarsPref.setValue(Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_CALENDARS));
        mCalendarsPref.setOnPreferenceChangeListener(this);
        CalendarEntries calEntries = CalendarEntries.findCalendars(getContentResolver());
        mCalendarsPref.setEntries(calEntries.getEntries());
        mCalendarsPref.setEntryValues(calEntries.getEntryValues());

        /* Calendar Reminders Only */
        mCalendarRemindersOnlyPref = (CheckBoxPreference) prefSet
                .findPreference(LOCKSCREEN_CALENDAR_REMINDERS_ONLY);
        mCalendarRemindersOnlyPref.setChecked(Settings.System.getInt(getContentResolver(),
                LOCKSCREEN_CALENDAR_REMINDERS_ONLY, 0) == 1);

        /* Calendar Alarm Lookahead */
        mCalendarAlarmLookaheadPref = (ListPreference) prefSet
                .findPreference(LOCKSCREEN_CALENDAR_LOOKAHEAD);
        long calendarAlarmLookaheadPref = Settings.System.getLong(getContentResolver(),
                Settings.System.LOCKSCREEN_CALENDAR_LOOKAHEAD, 10800000);
        mCalendarAlarmLookaheadPref.setValue(String.valueOf(calendarAlarmLookaheadPref));
        mCalendarAlarmLookaheadPref.setOnPreferenceChangeListener(this);

        /* Show next Calendar Alarm */
        mCalendarAlarmPref = (CheckBoxPreference) prefSet.findPreference(LOCKSCREEN_CALENDAR_ALARM);
        mCalendarAlarmPref.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_CALENDAR_ALARM, 0) == 1);

        boolean enableShowWidgetsPref = !mLockscreenWidgetLayout.getEntry().equals(getResources()
                .getStringArray(R.array.pref_lockscreen_widget_layout_entries)[1]);
        mAlwaysBatteryPref.setEnabled(enableShowWidgetsPref);
        mCalendarAlarmPref.setEnabled(enableShowWidgetsPref);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
        if (preference == mMusicControlPref) {
            value = mMusicControlPref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_MUSIC_CONTROLS,
                    value ? 1 : 0);
            return true;
        } else if (preference == mNowPlayingPref) {
            value = mNowPlayingPref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_NOW_PLAYING,
                    value ? 1 : 0);
            return true;
        } else if (preference == mAlbumArtPref) {
            value = mAlbumArtPref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_ALBUM_ART,
                    value ? 1 : 0);
            return true;
        } else if (preference == mAlwaysMusicControlPref) {
            value = mAlwaysMusicControlPref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_ALWAYS_MUSIC_CONTROLS, value ? 1 : 0);
            mLockscreenMusicHeadsetPref.setEnabled(!value);
            return true;
        } else if (preference == mAlwaysBatteryPref) {
            value = mAlwaysBatteryPref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_ALWAYS_BATTERY,
                    value ? 1 : 0);
            return true;
        } else if (preference == mCalendarAlarmPref) {
            value = mCalendarAlarmPref.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_CALENDAR_ALARM,
                    value ? 1 : 0);
            return true;
        } else if (preference == mCalendarRemindersOnlyPref) {
            value = mCalendarRemindersOnlyPref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_CALENDAR_REMINDERS_ONLY, value ? 1 : 0);
            return true;
        }
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockscreenMusicHeadsetPref) {
            int lockscreenMusicHeadsetPref = Integer.valueOf((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_MUSIC_CONTROLS_HEADSET, lockscreenMusicHeadsetPref);
            return true;
        } else if (preference == mCalendarAlarmLookaheadPref) {
            long calendarAlarmLookaheadPref = Long.valueOf((String) newValue);
            Settings.System.putLong(getContentResolver(),
                    Settings.System.LOCKSCREEN_CALENDAR_LOOKAHEAD, calendarAlarmLookaheadPref);
            return true;
        } else if (preference == mCalendarsPref) {
            String calendarsPref = (String) newValue;
            Settings.System.putString(getContentResolver(), Settings.System.LOCKSCREEN_CALENDARS,
                    calendarsPref);
            return true;
        } else if (preference == mLockscreenWidgetLayout) {
            Integer val = Integer.valueOf(newValue.toString());
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_WIDGETS_LAYOUT,val);
            mAlwaysBatteryPref.setEnabled(val != 1);
            mCalendarAlarmPref.setEnabled(val != 1);
            return true;
        }
        return false;
    }

    private static class CalendarEntries {

        private final static String CALENDARS_WHERE = Calendar.CalendarsColumns.SELECTED + "=1 AND "
                + Calendar.CalendarsColumns.ACCESS_LEVEL + ">=200";

        private final CharSequence[] mEntries;

        private final CharSequence[] mEntryValues;

        static CalendarEntries findCalendars(ContentResolver contentResolver) {
            List<CharSequence> entries = new ArrayList<CharSequence>();
            List<CharSequence> entryValues = new ArrayList<CharSequence>();
            Cursor cursor = null;
            try {
                cursor = Calendar.Calendars.query(contentResolver, new String[] {
                    Calendar.Calendars.DISPLAY_NAME, BaseColumns._ID
                }, CALENDARS_WHERE, null);
                while (cursor.moveToNext()) {
                    String entry = cursor.getString(0);
                    entries.add(entry);
                    String entryValue = cursor.getString(1);
                    entryValues.add(entryValue);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return new CalendarEntries(entries, entryValues);
        }

        private CalendarEntries(List<CharSequence> mEntries, List<CharSequence> mEntryValues) {
            this.mEntries = mEntries.toArray(new CharSequence[mEntries.size()]);
            this.mEntryValues = mEntryValues.toArray(new CharSequence[mEntryValues.size()]);
        }

        CharSequence[] getEntries() {
            return mEntries;
        }

        CharSequence[] getEntryValues() {
            return mEntryValues;
        }
    }
}
