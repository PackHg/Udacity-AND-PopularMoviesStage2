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

package com.packheng.popularmoviesstage2;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.packheng.popularmoviesstage2.db.AppDatabase;
import com.packheng.popularmoviesstage2.db.MovieEntry;

/**
 * {@link ViewModel} for {@link DetailActivity}
 */
public class DetailViewModel extends ViewModel {
    private static final String LOG_TAG = DetailViewModel.class.getSimpleName();

    private LiveData<MovieEntry> mMovie;

    public DetailViewModel(AppDatabase appDatabase, int movideId) {
        Log.d(LOG_TAG, String.format("Actively retrieving the movie %d from the Database", movideId));
        mMovie = appDatabase.movieDao().loadById(movideId);
    }

    public LiveData<MovieEntry> getMovie() {
        return mMovie;
    }
}
