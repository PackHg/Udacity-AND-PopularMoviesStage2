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

import com.packheng.popularmoviesstage2.DetailActivity;
import com.packheng.popularmoviesstage2.db.AppDatabase;
import com.packheng.popularmoviesstage2.db.FavoriteEntry;
import com.packheng.popularmoviesstage2.db.MovieEntry;
import com.packheng.popularmoviesstage2.db.ReviewEntry;
import com.packheng.popularmoviesstage2.db.TrailerEntry;

import java.util.List;

/**
 * {@link ViewModel} for {@link DetailActivity}
 */
public class DetailViewModel extends ViewModel {
    private static final String LOG_TAG = DetailViewModel.class.getSimpleName();

    private LiveData<MovieEntry> mObservableMovie;
    private LiveData<List<ReviewEntry>> mObservableReviews;
    private LiveData<List<TrailerEntry>> mObservableTrailers;
    private LiveData<FavoriteEntry> mObservableFavorite;

    public DetailViewModel(AppDatabase appDatabase, int movieId) {
        Log.d(LOG_TAG, String.format("(PACK) Actively retrieving the movie %d from the Database", movieId));
        mObservableMovie = appDatabase.movieDao().loadObservableMovieWithMovieId(movieId);

        Log.d(LOG_TAG, "(PACK) Actively retrieving the reviews from the Database");
        mObservableReviews = appDatabase.reviewDao().loadReviewsWithMovieId(movieId);

        Log.d(LOG_TAG, "(PACK) Actively retrieving the trailers from the Database");
        mObservableTrailers = appDatabase.trailerDao().loadTrailersWithMovieId(movieId);

        Log.d(LOG_TAG, String.format("(PACK) Actively retrieving the favorite %d from the Database", movieId));
        mObservableFavorite = appDatabase.favoriteDao().loadObservableFavoriteWithMovieId(movieId);
    }

    public LiveData<MovieEntry> getObservableMovie() {
        return mObservableMovie;
    }

    public LiveData<List<ReviewEntry>> getObservableReviews() {
        return mObservableReviews;
    }

    public LiveData<List<TrailerEntry>> getObservableTrailers() {
        return mObservableTrailers;
    }

    public LiveData<FavoriteEntry> getObservableFavorite() {
        return mObservableFavorite;
    }
}
