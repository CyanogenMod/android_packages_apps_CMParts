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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.cyanogenmod.cmparts.R;

public class NotificationActivity extends PreferenceActivity {
    public Handler mHandler = new Handler();
    public String mGlobalPackage;

    private static final int REQ_CATEGORY_LIST = 100;
    private static final int REQ_APPLICATION = 101;
    private static final int REQ_ADVANCED = 102;

    private Set<String> mCategories;
    private Preference mCategoryListPref;
    private Preference mAdvancedPref;

    private static class PackageSettings {
        public String title;
        public String packageName;
        public String color;
        public String blink;
        public String forceMode;
        public String category;

        private PackageSettings() {
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(packageName);
            builder.append("=");
            builder.append(color);
            builder.append("=");
            builder.append(blink);
            builder.append("=");
            builder.append(forceMode);
            builder.append("=");
            builder.append(category);
            return builder.toString();
        }
        public static PackageSettings fromString(String value) {
            if (TextUtils.isEmpty(value)) {
                return null;
            }
            String[] items = value.split("=");
            if (items.length < 4) {
                return null;
            }
            PackageSettings item = new PackageSettings();
            item.packageName = items[0];
            item.color = items[1];
            item.blink = items[2];
            item.forceMode = items[3];

            if (items.length == 4) {
                item.category = "";
            } else {
                /* Category names might include the character '=',
                   try to handle that case as well */
                StringBuilder builder = new StringBuilder();
                for (int i = 4; i < items.length; i++) {
                    if (i > 4) {
                        builder.append("=");
                    }
                    builder.append(items[i]);
                }
                item.category = builder.toString();
            }

            return item;
        }
    };

    private Map<String, PackageSettings> mPackages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.led_settings);
        initApplicationList();

        mCategoryListPref = findPreference("categories");
        mAdvancedPref = findPreference("advanced");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference pref) {
        String key = pref.getKey();

        if (pref == mCategoryListPref) {
            Intent intent = new Intent(this, CategoryActivity.class);
            startActivityForResult(intent, REQ_CATEGORY_LIST);
        } else if (pref == mAdvancedPref) {
            Intent intent = new Intent(this, AdvancedActivity.class);
            startActivityForResult(intent, REQ_ADVANCED);
        } else if (key != null && key.startsWith("app_")) {
            String pkg = key.substring(4);
            PackageSettings settings = mPackages.get(pkg);
            Intent intent = new Intent(this, PackageSettingsActivity.class);

            intent.putExtra(PackageSettingsActivity.EXTRA_PACKAGE, pkg);
            intent.putExtra(PackageSettingsActivity.EXTRA_TITLE, pref.getTitle().toString());
            if (settings != null) {
                intent.putExtra(PackageSettingsActivity.EXTRA_COLOR, settings.color);
                intent.putExtra(PackageSettingsActivity.EXTRA_BLINK, settings.blink);
                intent.putExtra(PackageSettingsActivity.EXTRA_FORCE_MODE, settings.forceMode);
                intent.putExtra(PackageSettingsActivity.EXTRA_CATEGORY, settings.category);
            }

            startActivityForResult(intent, REQ_APPLICATION);
        }

        return super.onPreferenceTreeClick(screen, pref);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_APPLICATION) {
                String pkg = data.getStringExtra(PackageSettingsActivity.EXTRA_PACKAGE);
                if (TextUtils.equals(data.getAction(), Intent.ACTION_DELETE)) {
                    mPackages.remove(pkg);
                } else {
                    PackageSettings settings = mPackages.get(pkg);
                    if (settings == null) {
                        settings = new PackageSettings();
                        settings.packageName = pkg;
                        mPackages.put(pkg, settings);
                    }
                    if (data.hasExtra(PackageSettingsActivity.EXTRA_COLOR)) {
                        settings.color = data.getStringExtra(PackageSettingsActivity.EXTRA_COLOR);
                    }
                    if (data.hasExtra(PackageSettingsActivity.EXTRA_BLINK)) {
                        settings.blink = data.getStringExtra(PackageSettingsActivity.EXTRA_BLINK);
                    }
                    if (data.hasExtra(PackageSettingsActivity.EXTRA_FORCE_MODE)) {
                        settings.forceMode = data.getStringExtra(PackageSettingsActivity.EXTRA_FORCE_MODE);
                    }
                    if (data.hasExtra(PackageSettingsActivity.EXTRA_CATEGORY)) {
                        settings.category = data.getStringExtra(PackageSettingsActivity.EXTRA_CATEGORY);
                    }
                }
                savePackageList();
            } else if (requestCode == REQ_CATEGORY_LIST) {
                List<String> categories =
                        Arrays.asList(data.getStringArrayExtra(CategoryActivity.EXTRA_CATEGORIES));
                boolean packageChanged = false;

                /* make sure to clear out all references to deleted categories */
                for (PackageSettings pkg : mPackages.values()) {
                    if (TextUtils.isEmpty(pkg.category)) {
                        continue;
                    }
                    if (!categories.contains(pkg.category)) {
                        pkg.category = "";
                        packageChanged = true;
                    }
                }
                if (packageChanged) {
                    savePackageList();
                }
            }

            initApplicationList();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initApplicationList() {
        final ProgressDialog pbarDialog =
                ProgressDialog.show(this, getString(R.string.dialog_trackball_loading),
                                    getString(R.string.dialog_trackball_packagelist), true, false);

        Thread t = new Thread() {
            public void run() {
                /* pretend an intent to close any open category preference screen */
                onNewIntent(new Intent());
                parsePackageList();
                populateApplicationList();

                mHandler.post(new Runnable() {
                    public void run() {
                        pbarDialog.dismiss();
                    }
                });
            }
        };
        t.start();
    }

    private void parsePackageList() {
        String baseString = Settings.System.getString(getContentResolver(),
                            Settings.System.NOTIFICATION_PACKAGE_COLORS);
        String[] array = LedUtils.arrayFromString(baseString, '|');

        mPackages = new HashMap<String, PackageSettings>();
        if (array != null) {
            for (String item : array) {
                if (TextUtils.isEmpty(item)) {
                    continue;
                }
                PackageSettings settings = PackageSettings.fromString(item);
                if (settings != null) {
                    mPackages.put(settings.packageName, settings);
                }
            }
        }
    }

    private void savePackageList() {
        List<String> settings = new ArrayList<String>();
        for (PackageSettings setting : mPackages.values()) {
            settings.add(setting.toString());
        }
        String value = LedUtils.stringFromArray(settings.toArray(new String[0]), '|');
        Settings.System.putString(getContentResolver(),
                                  Settings.System.NOTIFICATION_PACKAGE_COLORS, value);
    }

    private String knownPackage(String pkg) {
        int resId = -1;
        if (pkg.equals("com.android.email")) {
            resId = R.string.trackball_app_email;
        } else if (pkg.equals("com.android.mms")) {
            resId = R.string.trackball_app_mms;
        } else if (pkg.equals("com.google.android.apps.googlevoice")) {
            resId = R.string.trackball_app_gvoice;
        } else if (pkg.equals("com.google.android.gm")) {
            resId = R.string.trackball_app_gmail;
        } else if (pkg.equals("com.google.android.gsf")) {
            resId = R.string.trackball_app_gtalk;
        } else if (pkg.equals("com.twitter.android")) {
            resId = R.string.trackball_app_twitter;
        } else if (pkg.equals("jp.r246.twicca")) {
            resId = R.string.trackball_app_twicca;
        } else if (pkg.equals("com.android.phone")) {
            resId = R.string.trackball_app_dialer;
        }
        if (resId >= 0) {
            return getResources().getString(resId);
        }

        return null;
    }

    private String getPackageName(PackageInfo p) {
        String knownPackage = knownPackage(p.packageName);
        return knownPackage == null ?
               p.applicationInfo.loadLabel(getPackageManager()).toString() : knownPackage;
    }

    private List<PackageInfo> getPackageList() {
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        List<PackageInfo> list = new ArrayList<PackageInfo>();
        SharedPreferences pkgPrefs = getSharedPreferences("led_packages", Context.MODE_PRIVATE);
        for (PackageInfo p : packs) {
            try {
                String pkg = p.packageName;
                if (pkgPrefs.getString(pkg, null) != null || knownPackage(pkg) != null) {
                    list.add(p);
                }
            } catch (Exception e) {
                Log.d("GetPackageList", e.toString());
            }
        }
        return list;
    }

    private Set<String> getCategoryList() {
        Set<String> categories = new TreeSet<String>();

        for (PackageSettings settings : mPackages.values()) {
            if (settings.category != null) {
                categories.add(settings.category);
            }
        }

        return categories;
    }

    private void populateApplicationList() {
        final PreferenceCategory parent = (PreferenceCategory) findPreference("applications");
        Map<String, PackageInfo> sortedPackages = new TreeMap<String, PackageInfo>();
        Map<String, PreferenceScreen> categories = new HashMap<String, PreferenceScreen>();

        for (PackageInfo pkgInfo : getPackageList()) {
            sortedPackages.put(getPackageName(pkgInfo), pkgInfo);
        }

        parent.removeAll();

        PreferenceScreen unconfGroup = getPreferenceManager().createPreferenceScreen(this);
        unconfGroup.setKey("applications_unconf");
        unconfGroup.setTitle(getResources().getString(R.string.trackball_category_unconfigured));
        parent.addPreference(unconfGroup);

        for (String category : getCategoryList()) {
            PreferenceScreen categoryGroup = getPreferenceManager().createPreferenceScreen(this);
            categoryGroup.setKey("applications_" + category);
            categories.put(category, categoryGroup);
            if (category.isEmpty()) {
                category = getResources().getString(R.string.trackball_category_misc);
            }
            categoryGroup.setTitle(category);
            parent.addPreference(categoryGroup);
        }

        for (Map.Entry<String, PackageInfo> pkgEntry : sortedPackages.entrySet()) {
            String pkg = pkgEntry.getValue().packageName;

            if (TextUtils.isEmpty(pkg)) {
                continue;
            }

            PackageSettings settings = mPackages.get(pkg);
            PreferenceScreen catScreen = unconfGroup;

            if (settings != null) {
                catScreen = categories.get(settings.category);
            }

            if (catScreen != null) {
                Preference appName = getPreferenceManager().createPreferenceScreen(this);
                String shortPackageName = pkgEntry.getKey();

                appName.setKey("app_" + pkg);
                appName.setTitle(shortPackageName);
                catScreen.addPreference(appName);
            }
        }

        if (unconfGroup.getPreferenceCount() == 0) {
            parent.removePreference(unconfGroup);
        }
    }
}
