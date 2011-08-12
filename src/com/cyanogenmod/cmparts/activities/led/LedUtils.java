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

import android.text.TextUtils;

public class LedUtils {
    public static String[] arrayFromString(String value, char delim) {
        if (TextUtils.isEmpty(value)) {
            return new String[0];
        }
        return value.split("\\" + delim);
    }

    public static String stringFromArray(String[] array, char delim) {
        StringBuilder builder = new StringBuilder();
        for (String string : array) {
            if (builder.length() > 0) {
                builder.append(delim);
            }
            builder.append(string);
        }
        return builder.toString();
    }
}
