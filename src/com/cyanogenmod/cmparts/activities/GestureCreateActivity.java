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
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.cyanogenmod.cmparts.R;
import com.cyanogenmod.cmparts.utils.ShortcutPickHelper;

public class GestureCreateActivity extends Activity implements ShortcutPickHelper.OnPickListener {
    private static final float LENGTH_THRESHOLD = 120.0f;

    // must correspond with the @array/pref_lockscreen_gesture_action_entries
    private static final int ACTION_POSITION_UNLOCK = 0;
    private static final int ACTION_POSITION_SOUND = 1;
    private static final int ACTION_POSITION_SHORTCUT = 2;
    private static final int ACTION_POSITION_FLASHLIGHT = 3;

    private Gesture mGesture;

    private View mDoneButton;

    private Spinner mActionPicker;

    private TextView mDrawLabel;

    private CheckBox mRunInBackground;

    private String mUri;

    private String mFriendlyName;

    private double mGestureSensitivity;

    private ShortcutPickHelper mPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_create);

        mPicker = new ShortcutPickHelper(this, this);

        mDoneButton = findViewById(R.id.done);
        mDrawLabel = (TextView) findViewById(R.id.gestures_draw_label);
        mRunInBackground = (CheckBox) findViewById(R.id.gestures_run_in_background);
        mActionPicker = (Spinner) findViewById(R.id.action_picker);
        mActionPicker.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case ACTION_POSITION_UNLOCK:
                        pickUnlockOnly();
                        break;
                    case ACTION_POSITION_SOUND:
                        pickSoundOnly();
                        break;
                    case ACTION_POSITION_SHORTCUT:
                        mPicker.pickShortcut();
                        break;
                    case ACTION_POSITION_FLASHLIGHT:
                        pickFlashlight();
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        GestureOverlayView overlay = (GestureOverlayView) findViewById(R.id.gestures_overlay);
        overlay.addOnGestureListener(new GesturesProcessor());
        // Remove flashlight button if Torch app isn't on the phone
        PackageManager pm = this.getBaseContext().getPackageManager();
        List<ResolveInfo> l = pm.queryBroadcastReceivers(new Intent(
                "net.cactii.flash2.TOGGLE_FLASHLIGHT"), 0);
        if (l.isEmpty()) {
            // Get the original array
            CharSequence entries[] = getResources().getStringArray(R.array.pref_lockscreen_gesture_action_entries);
            // Create new array without the last item (without the flashlight)
            CharSequence entriesShort[] = new String[entries.length-1];
            for (int i = 0; i < entries.length-1; i++) {
                entriesShort[i] = entries[i];
            }
            ArrayAdapter<CharSequence> spinnerArrayAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, entriesShort);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Set new adapter
            mActionPicker.setAdapter(spinnerArrayAdapter);
        }

        mGestureSensitivity = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_GESTURES_SENSITIVITY, 3);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mGesture != null) {
            outState.putParcelable("gesture", mGesture);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mGesture = savedInstanceState.getParcelable("gesture");
        if (mGesture != null) {
            final GestureOverlayView overlay = (GestureOverlayView) findViewById(R.id.gestures_overlay);
            overlay.post(new Runnable() {
                public void run() {
                    overlay.setGesture(mGesture);
                }
            });

            mDoneButton.setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

    public void addGesture(View v) {
        if (mGesture != null) {
            if (mUri == null) {
                Toast.makeText(this, R.string.gestures_error_missing_shortcut, Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            if (mRunInBackground.isChecked()) {
                mUri += "___BACKGROUND";
            }

            final GestureLibrary store = GestureListActivity.getStore();
            store.addGesture(mUri, mGesture);
            store.save();
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    public void cancelGesture(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }

    private class GesturesProcessor implements GestureOverlayView.OnGestureListener {
        public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
            mDoneButton.setEnabled(false);
            mGesture = null;
        }

        public void onGesture(GestureOverlayView overlay, MotionEvent event) {
        }

        public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
            mGesture = overlay.getGesture();
            if (mGesture.getLength() < LENGTH_THRESHOLD) {
                overlay.clear(false);
            }

            if (isThereASimilarGesture(mGesture)) {
                Toast.makeText(GestureCreateActivity.this, R.string.gestures_already_present,
                        Toast.LENGTH_SHORT).show();
            }

            mDoneButton.setEnabled(true);
        }

        public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
        }
    }

    public boolean isThereASimilarGesture(Gesture gesture) {
        final GestureLibrary store = GestureListActivity.getStore();
        ArrayList<Prediction> predictions = store.recognize(gesture);

        for (Prediction prediction : predictions) {
            if (prediction.score > mGestureSensitivity) {
                return true;
            }
        }

        return false;
    }

    public void pickUnlockOnly() {
        mFriendlyName = getString(R.string.gestures_unlock_only);
        mDrawLabel.setText(getString(R.string.gestures_draw_for_label, mFriendlyName));
        mUri = mFriendlyName + "___UNLOCK";
        disableCheckbox();
    }

    public void pickSoundOnly() {
        mFriendlyName = getString(R.string.gestures_toggle_sound);
        mDrawLabel.setText(getString(R.string.gestures_draw_for_label, mFriendlyName));
        mUri = mFriendlyName + "___SOUND";
        disableCheckbox();
    }

    public void pickFlashlight() {
        mFriendlyName = getString(R.string.gestures_flashlight);
        mDrawLabel.setText(getString(R.string.gestures_draw_for_label, mFriendlyName));
        mUri = mFriendlyName + "___FLASHLIGHT";
        disableCheckbox();
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        mFriendlyName = (friendlyName != null) ? friendlyName : "null";

        mDrawLabel.setText(getString(R.string.gestures_draw_for_label, mFriendlyName));
        mUri = mFriendlyName + "___" + uri;

        if (isApplication) {
            mRunInBackground.setVisibility(View.VISIBLE);
        } else {
            disableCheckbox();
        }
    }

    void disableCheckbox() {
        mRunInBackground.setChecked(false);
        mRunInBackground.setVisibility(View.GONE);
    }
}
