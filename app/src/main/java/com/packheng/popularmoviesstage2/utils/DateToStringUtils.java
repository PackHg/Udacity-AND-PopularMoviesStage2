/*
 * Copyright (c) 2018 Pack Heng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.packheng.popularmoviesstage2.utils;

import android.util.Log;

import java.text.DateFormat;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper class with methods for converting {@link Date} to {@link String} and vice versa.
 */
public class DateToStringUtils {
    private static final String LOG_TAG = DateToStringUtils.class.getSimpleName();

    private DateToStringUtils() {}

    /**
     * Converts a date string to a {@link java.util.Date} object with UTC time zone.
     * Returns null if the {@param s} is empty.
     *
     * @param s a date String.
     * @return a {@link java.util.Date} object.
     */
    public static Date stringToDate(String s) {
        SimpleDateFormat dateFormatUTC = new SimpleDateFormat(
                "yyyy-MM-dd" /* ISO-8601 format */,
                Locale.ENGLISH);
        dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (s.isEmpty()) {
            return null;
        }

        java.util.Date date = new java.util.Date();
        try {
            date = dateFormatUTC.parse(s);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Unable to parse the string parameter to a Date.");
            e.printStackTrace();
        }
        return date;
    }

    /**
     * Returns a date string from a {@link Date} using the local {@link DateFormat} with the format
     * "MMM dd, yyyy".
     *
     * @param date a {@link Date} object.
     * @return a String.
     */
    public static String formatDateToString(Date date) {
        DateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);;
        return date != null ? df.format(date) : "";
    }
}
