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

package com.packheng.popularmoviesstage2.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.packheng.popularmoviesstage2.MainActivity;
import com.packheng.popularmoviesstage2.data.database.AppDatabase;
import com.packheng.popularmoviesstage2.data.database.FavoriteEntry;
import com.packheng.popularmoviesstage2.data.database.MovieEntry;

import java.util.List;

/**
 * {@link ViewModel} for {@link MainActivity}
 */
public class MainViewModel extends ViewModel {
    private static final String LOG_TAG = MainViewModel.class.getSimpleName();

    private final AppDatabase mAppDatabase;

    private final LiveData<List<MovieEntry>> mObservableMovies;
    private final LiveData<List<FavoriteEntry>> mObservableFavorites;

    public MainViewModel(AppDatabase appDatabase) {
        mAppDatabase = appDatabase;
        Log.d(LOG_TAG, "(PACK) Actively retrieving the movies from the Database");
        mObservableMovies = mAppDatabase.movieDao().loadAllObservableMovies();
        Log.d(LOG_TAG, "(PACK) Actively retrieving the favorites from the Database");
        mObservableFavorites = mAppDatabase.favoriteDao().loadAllObservableFavorites();
    }

    public LiveData<List<MovieEntry>> getObservableMovies() {
        return mObservableMovies;
    }

    public LiveData<List<FavoriteEntry>> getObservableFavorites() {
        return mObservableFavorites;
    }
}
