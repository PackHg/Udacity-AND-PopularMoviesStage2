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

package com.packheng.popularmoviesstage2.TMDB;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import com.packheng.popularmoviesstage2.AppExecutors;
import com.packheng.popularmoviesstage2.BuildConfig;
import com.packheng.popularmoviesstage2.db.MovieEntry;

import java.util.List;

import static com.packheng.popularmoviesstage2.utils.NetworkUtils.isNetworkConnected;

public class MovieDataSource {
    private static final String LOG_TAG = MovieDataSource.class.getSimpleName();

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static MovieDataSource sInstance;
    private final Context mContext;

    // LiveData storing the latest downloaded movies data
    private final MutableLiveData<List<MovieEntry>> mDownloadedMovies;
    private final AppExecutors mAppExecutors;

    private MovieDataSource(Context context, AppExecutors appExecutors) {
        mContext = context;
        mAppExecutors = appExecutors;
        mDownloadedMovies = new MutableLiveData<List<MovieEntry>>();
    }

    /**
     * Get the singleton for this class
     */
    public static MovieDataSource getInstance(Context context, AppExecutors appExecutors) {
        Log.d(LOG_TAG, "Getting the movies data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new MovieDataSource(context.getApplicationContext(), appExecutors);
                Log.d(LOG_TAG, "Made new movies data source");
            }
        }
        return sInstance;
    }

    public LiveData<List<MovieEntry>> getMoviesData() {
        return mDownloadedMovies;
    }

    /**
     * Downloads movies.
     *
     * @param sortBy "Most Popular Movies" or "Top Rated Movies"
     */
    public void fetchMovies(String sortBy) {
        Log.d(LOG_TAG, "Fetch movies started");
        mAppExecutors.networkIO().execute(() -> {
            try {
                final String API_KEY_VALUE = BuildConfig.ApiKey;
                final String BASE_URL = "https://image.tmdb.org/t/p";
                final String IMAGE_SIZE = "/w185";
                final String EMPTY_STRING = "";

                if (isNetworkConnected(mContext)) {

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }
}
