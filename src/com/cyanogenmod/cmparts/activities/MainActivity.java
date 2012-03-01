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
import com.cyanogenmod.cmparts.activities.DisplayActivity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainActivity extends PreferenceActivity {
    private static final String DISPLAY_SETTINGS = "display_settings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.cmparts);

        /* Show a different summary for display settings if no backlight settings
         * are supported. */
        if (!DisplayActivity.supportsBacklightSettings(this)) {
            getPreferenceScreen().findPreference(DISPLAY_SETTINGS)
                .setSummary(R.string.display_settings_summary_head_nobacklight);
        }
    }
}
