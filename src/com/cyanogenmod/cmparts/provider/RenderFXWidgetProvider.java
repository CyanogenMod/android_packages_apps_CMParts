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

package com.cyanogenmod.cmparts.provider;

import com.cyanogenmod.cmparts.R;
import com.cyanogenmod.cmparts.services.RenderFXService;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class RenderFXWidgetProvider extends AppWidgetProvider {

    private static RenderFXWidgetProvider sInstance;
    public SharedPreferences mPrefs;

    static final ComponentName THIS_APPWIDGET = new ComponentName("com.cyanogenmod.cmparts",
            "com.cyanogenmod.cmparts.provider.RenderFXWidgetProvider");

    static synchronized RenderFXWidgetProvider getInstance() {
        if (sInstance == null) {
            sInstance = new RenderFXWidgetProvider();
        }
        return sInstance;
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds)
            updateState(context, appWidgetId);
    }

    private static PendingIntent getLaunchPendingIntent(Context context, int appWidgetId,
            int buttonId) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(context, RenderFXWidgetProvider.class);
        launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        launchIntent.setData(Uri.parse("custom:" + appWidgetId + "/" + buttonId));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, launchIntent, 0);
        return pi;
    }

    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
            Uri data = intent.getData();
            int buttonId;
            int widgetId;
            widgetId = Integer.parseInt(data.getSchemeSpecificPart().split("/")[0]);
            buttonId = Integer.parseInt(data.getSchemeSpecificPart().split("/")[1]);

            if (buttonId == 0) {
                Intent pendingIntent;

                if (this.RenderFXServiceRunning(context)) {
                    context.stopService(new Intent(context, RenderFXService.class));
                    this.updateAllStates(context);
                    return;
                }

                pendingIntent = new Intent(context, RenderFXService.class);
                pendingIntent.putExtra("widget_render_effect",
                        mPrefs.getInt("widget_render_effect_" + widgetId, 1));

                if (this.RenderFXServiceRunning(context)) {
                    context.stopService(pendingIntent);
                } else {
                    context.startService(pendingIntent);
                }
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.updateAllStates(context);
        }
    }

    private boolean RenderFXServiceRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> svcList = am.getRunningServices(100);

        if (!(svcList.size() > 0))
            return false;

        for (RunningServiceInfo serviceInfo : svcList) {
            if (serviceInfo.service.getClassName().endsWith(".RenderFXService"))
                return true;
        }
        return false;
    }

    public void updateAllStates(Context context) {
        final AppWidgetManager am = AppWidgetManager.getInstance(context);

        for (int appWidgetId : am.getAppWidgetIds(THIS_APPWIDGET))
            this.updateState(context, appWidgetId);
    }

    public void updateState(Context context, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        views.setOnClickPendingIntent(R.id.btn, getLaunchPendingIntent(context, appWidgetId, 0));

        if (this.RenderFXServiceRunning(context)) {
            views.setImageViewResource(R.id.img_torch, R.drawable.render_on);
        } else {
            views.setImageViewResource(R.id.img_torch, R.drawable.render_off);
        }

        int renderFx = prefs.getInt("widget_render_effect_" + appWidgetId, 1);

        switch (renderFx) {
        case 1:
            views.setTextViewText(R.id.ind, context.getResources().getString(R.string.widget_render_effect_night));
            break;
        case 2:
            views.setTextViewText(R.id.ind, context.getResources().getString(R.string.widget_render_effect_terminal));
            break;
        case 3:
            views.setTextViewText(R.id.ind, context.getResources().getString(R.string.widget_render_effect_blue));
            break;
        case 4:
            views.setTextViewText(R.id.ind, context.getResources().getString(R.string.widget_render_effect_amber));
            break;
        case 5:
            views.setTextViewText(R.id.ind, context.getResources().getString(R.string.widget_render_effect_salmon));
            break;
        case 6:
            views.setTextViewText(R.id.ind, context.getResources().getString(R.string.widget_render_effect_fuscia));
            break;
        case 7:
            views.setTextViewText(R.id.ind, context.getResources().getString(R.string.widget_render_effect_calibrated));
            break;
        case 8:
            views.setTextViewText(R.id.ind, context.getResources().getString(R.string.widget_render_effect_calibrated_red));
            break;
        case 9:
            views.setTextViewText(R.id.ind, context.getResources().getString(R.string.widget_render_effect_calibrated_cool));
            break;
        case 10:
            views.setTextViewText(R.id.ind, context.getResources().getString(R.string.widget_render_effect_red));
            break;
        default:
            views.setTextViewText(R.id.ind, context.getResources()
                    .getString(R.string.renderfx_temp));
        }

        final AppWidgetManager gm = AppWidgetManager.getInstance(context);
        gm.updateAppWidget(appWidgetId, views);
    }
}
