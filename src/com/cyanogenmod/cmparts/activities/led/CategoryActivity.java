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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.cyanogenmod.cmparts.R;

public class CategoryActivity extends PreferenceActivity implements
            Preference.OnPreferenceChangeListener {

    public static final String KEY_CATEGORY_LIST = "categories";
    public static final String EXTRA_CATEGORIES = KEY_CATEGORY_LIST;

    private Set<String> mCategories;
    private SharedPreferences mPrefs;

    private Preference mCategoryAddPref;
    private ListPreference mCategoryRemovePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.led_categories);
        setResult(RESULT_CANCELED);

        mCategoryAddPref = findPreference("category_add");
        mCategoryRemovePref = (ListPreference) findPreference("category_remove");
        mCategoryRemovePref.setOnPreferenceChangeListener(this);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String[] catList = fetchCategories(mPrefs);
        mCategories = new HashSet<String>(Arrays.asList(catList));

        updateRemoveEntries();
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object objValue) {
        if (pref == mCategoryRemovePref) {
            String value = (String) objValue;
            mCategories.remove(value);
            saveCategories();
            updateRemoveEntries();
        }

        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference pref) {
        if (pref == mCategoryAddPref) {
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.add_cat, null);

            dialog.setTitle(R.string.trackball_category_add_title);
            dialog.setView(textEntryView);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    EditText textBox = (EditText) textEntryView.findViewById(R.id.cat_text);
                    String name = textBox.getText().toString();

                    if (name.contains("=")) {
                        showDisallowedNameError();
                        return;
                    }

                    mCategories.add(name);
                    saveCategories();
                    updateRemoveEntries();
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel),
                    (DialogInterface.OnClickListener) null);
            dialog.show();
            return false;
        }

        return super.onPreferenceTreeClick(screen, pref);
    }

    public static String[] fetchCategories(SharedPreferences prefs) {
        return LedUtils.arrayFromString(prefs.getString(KEY_CATEGORY_LIST, null), '|');
    }

    private String[] getCategoryArray() {
        return mCategories.toArray(new String[0]);
    }

    private void saveCategories() {
        String[] categories = getCategoryArray();
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(KEY_CATEGORY_LIST, LedUtils.stringFromArray(categories, '|'));
        editor.commit();

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_CATEGORIES, categories);
        setResult(RESULT_OK, resultIntent);
    }

    private void updateRemoveEntries() {
        String[] categories = getCategoryArray();
        mCategoryRemovePref.setEntries(categories);
        mCategoryRemovePref.setEntryValues(categories);
        mCategoryRemovePref.setEnabled(categories.length > 0);
    }

    private void showDisallowedNameError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.trackball_category_add_error_title))
               .setMessage(getString(R.string.trackball_category_add_error_summary))
               .setCancelable(false)
               .setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }
}
