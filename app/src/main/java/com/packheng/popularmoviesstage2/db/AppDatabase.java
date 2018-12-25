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

package com.packheng.popularmoviesstage2.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.util.Log;

@Database(entities = {MovieEntry.class, ReviewEntry.class, TrailerEntry.class, FavoriteEntry.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "popular-movies-db";
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {

        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "(PACK) Creating new database instance, " + DATABASE_NAME);
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, DATABASE_NAME)
                        .build();
            }
        }

        Log.d(LOG_TAG, "(PACK) Getting the database instance, " + DATABASE_NAME);
        return sInstance;
    }

    public abstract MovieDao movieDao();

    public abstract ReviewDao reviewDao();

    public abstract TrailerDao trailerDao();

    public abstract FavoriteDao favoriteDao();
}
