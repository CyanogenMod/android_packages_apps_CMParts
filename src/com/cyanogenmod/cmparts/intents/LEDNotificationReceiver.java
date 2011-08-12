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

package com.cyanogenmod.cmparts.intents;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class LEDNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        /* add package which sent out the notification to our own tracking list */
        String pkg = intent.getStringExtra(NotificationManager.EXTRA_PACKAGE);
        /* filter out our test notifications */
        if (!TextUtils.equals(ctx.getPackageName(), pkg)) {
            SharedPreferences pkgPrefs = ctx.getSharedPreferences("led_packages", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pkgPrefs.edit();
            editor.putString(pkg, "");
            editor.commit();
        }
    }
}
