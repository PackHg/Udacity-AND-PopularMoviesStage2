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

package com.packheng.popularmoviesstage2.data;

import android.util.Log;

import com.packheng.popularmoviesstage2.BuildConfig;
import com.packheng.popularmoviesstage2.MainActivity;
import com.packheng.popularmoviesstage2.data.api.TMDBEndpointInterface;
import com.packheng.popularmoviesstage2.data.api.TMDBMovie;
import com.packheng.popularmoviesstage2.data.api.TMDBMovies;
import com.packheng.popularmoviesstage2.data.api.TMDBReview;
import com.packheng.popularmoviesstage2.data.api.TMDBReviews;
import com.packheng.popularmoviesstage2.data.api.TMDBTrailer;
import com.packheng.popularmoviesstage2.data.api.TMDBTrailers;
import com.packheng.popularmoviesstage2.data.database.AppDatabase;
import com.packheng.popularmoviesstage2.data.database.MovieEntry;
import com.packheng.popularmoviesstage2.data.database.ReviewEntry;
import com.packheng.popularmoviesstage2.data.database.TrailerEntry;
import com.packheng.popularmoviesstage2.utils.AppExecutors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.packheng.popularmoviesstage2.utils.DateToStringUtils.stringToDate;

/**
 * Handles data operations in this app. Acts as a mediator between the sourcing of remote
 * movies data and the {@link AppDatabase}.
 */
public class DataRepository {
    private static final String LOG_TAG = DataRepository.class.getSimpleName();

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static DataRepository sInstance;

    private final AppDatabase mAppDatabase;
    private final TMDBEndpointInterface mApiService;
    private final AppExecutors mAppExecutors;

    private static final String API_KEY_VALUE = BuildConfig.ApiKey;

    private ArrayList<MovieEntry> mMovieEntries;

    private OnDownloadOfDataListener mCallback;

    private boolean mIsDownloadOfMoviesFinished = false;
    private boolean mIsDownloadOfReviewsFinished = false;
    private boolean mIsDownloadOfTrailersFinished = false;

    public interface OnDownloadOfDataListener {
        void OnDownloadOfDataFinished();
        void OnDownLoadOfDataFailed();
    }

    private DataRepository(AppDatabase appDatabase, TMDBEndpointInterface apiService,
           AppExecutors appExecutors, OnDownloadOfDataListener listener) {
        mAppDatabase = appDatabase;
        mApiService =  apiService;
        mAppExecutors = appExecutors;
        mCallback = listener;

        mMovieEntries = new ArrayList<>();
    }

    public synchronized static DataRepository getInstance(AppDatabase appDatabase,
          TMDBEndpointInterface apiService, AppExecutors appExecutors, OnDownloadOfDataListener listener) {
        Log.d(LOG_TAG, "(PACK) Getting the data repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new DataRepository(appDatabase, apiService, appExecutors, listener);
                Log.d(LOG_TAG, "(PACK) Made new data repository");
            }
        }
        return sInstance;
    }

    /**
     * If all downloads are finished calls back {@link MainActivity} to
     * notify.
     */
    private void checkAllDownloadsAreFinished() {
        Log.d(LOG_TAG, "(PACK) checkAllDownloadsAreFinished");
        if (mIsDownloadOfMoviesFinished && mIsDownloadOfReviewsFinished
                && mIsDownloadOfTrailersFinished) {
            mCallback.OnDownloadOfDataFinished();
        }
    }

    /**
     * Downloads movies data.
     * 
     * @param sortBy sort by chosen by the user.
     */
    public synchronized void downloadMovies(String sortBy) {
        final String BASE_URL = "https://image.tmdb.org/t/p";
        final String IMAGE_SIZE = "/w185";
        final String EMPTY_STRING = "";
        final String MOST_POPULAR = "Most Popular Movies";
        final String TOP_RATED = "Top Rated Movies";

        // Accessing the API
        Log.d(LOG_TAG, "(PACK) downloadMovies() - Starts loading movies from API.");

        Call<TMDBMovies> call;

        if (sortBy.equals(MOST_POPULAR)) {
            call = mApiService.popularMovies(API_KEY_VALUE);
        } else if (sortBy.equals(TOP_RATED)) {
            call = mApiService.topRatedMovies(API_KEY_VALUE);
        } else {
            Log.w(LOG_TAG, "downloadMovies(), the sortBy argument is unknown: " + sortBy);
            return;
        }

        mIsDownloadOfMoviesFinished = false;
        mIsDownloadOfReviewsFinished = false;
        mIsDownloadOfTrailersFinished = false;

        call.enqueue(new Callback<TMDBMovies>() {

            @Override
            public void onResponse(Call<TMDBMovies> call, Response<TMDBMovies> response) {

                if (response.body() != null) {
                        List<TMDBMovie> results = response.body().getResults();
                        mMovieEntries.clear();
                        for (TMDBMovie result: results) {
                            if (result != null) {
                                int movieId = result.getId();
                                String title = result.getOriginalTitle();
                                Log.d(LOG_TAG, "(PACK) downloadMovies() - movie title : " + title + ".");
                                String posterUrl;
                                if (!result.getPosterPath().isEmpty()) {
                                    posterUrl = BASE_URL + IMAGE_SIZE + result.getPosterPath();
                                } else {
                                    posterUrl = EMPTY_STRING;
                                }
                                String plotSynopsis = result.getOverview();
                                double userRating = result.getVoteAverage();
                                Date releaseDate = stringToDate(result.getReleaseDate());
                                MovieEntry movie = new MovieEntry(movieId, title, posterUrl, plotSynopsis,
                                        userRating, releaseDate);
                                mMovieEntries.add(movie);
                            }
                        }

                        mAppExecutors.diskIO().execute(() -> {
                            mAppDatabase.movieDao().deleteAllMovies();
                            Log.d(LOG_TAG, "(PACK) downloadMovies() - deleted all movies in database.");
                            mAppDatabase.movieDao().insertMovies(mMovieEntries);
                            Log.d(LOG_TAG, "(PACK) downloadMovies() - inserted downloaded movies into database.");
                        });

                        mIsDownloadOfMoviesFinished = true;

                        downloadReviews();
                        downloadTrailers();
                } else {
                    mCallback.OnDownLoadOfDataFailed();
                }
            }

            @Override
            public void onFailure(Call<TMDBMovies> call, Throwable t) {
                mCallback.OnDownLoadOfDataFailed();
            }
        });
    }

    /**
     * Downloads reviews data.
     */
    private synchronized void downloadReviews() {

        // Delete all existing reviews
        mAppExecutors.diskIO().execute(() -> {
            mAppDatabase.reviewDao().deleteAllReviews();
            Log.d(LOG_TAG, "(PACK) downloadReviews() - Deleted all reviews from database.");
        });

        // Accessing the API
        Log.d(LOG_TAG, "(PACK) downloadReviews() - Starts loading reviews from API.");

        for(MovieEntry movie: mMovieEntries) {
            Call<TMDBReviews> call = mApiService.reviews(movie.getMovieId(), API_KEY_VALUE);

            call.enqueue(new Callback<TMDBReviews>() {

                @Override
                public void onResponse(Call<TMDBReviews> call, Response<TMDBReviews> response) {
                    if (response.body() != null) {
                        List<TMDBReview> results = response.body().getResults();
                        ArrayList<ReviewEntry> reviews = new ArrayList<>();
                        for(TMDBReview result: results) {
                            reviews.add(new ReviewEntry(
                                    result.getId(),
                                    movie.getMovieId(),
                                    result.getAuthor(),
                                    result.getContent(),
                                    result.getUrl()));
                        }

                        mAppExecutors.diskIO().execute(() -> {
                            mAppDatabase.reviewDao().insertReviews(reviews);
                            Log.d(LOG_TAG, "(PACK) downloadReviews() - inserted reviews of the following movie into database: " + movie.getTitle());
                        });
                    }
                }

                @Override
                public void onFailure(Call<TMDBReviews> call, Throwable t) {
                    Log.e(LOG_TAG, "Issue with downloading the movie's reviews");
                    mCallback.OnDownLoadOfDataFailed();
                }
            });
        }

        mIsDownloadOfReviewsFinished = true;
        checkAllDownloadsAreFinished();
    }

    /**
     * Downloads trailers data.
     */
    private synchronized void downloadTrailers() {
        // Delete all existing trailers
        mAppExecutors.diskIO().execute(() -> {
            mAppDatabase.trailerDao().deleteAllTrailers();
            Log.d(LOG_TAG, "(PACK) downloadTrailers() - Deleted all trailers from database.");
        });

        // Accessing the API
        Log.d(LOG_TAG, "(PACK) downloadTrailers() - Starts loading trailers from API.");

        for(MovieEntry movie: mMovieEntries) {
            Call<TMDBTrailers> call = mApiService.trailers(movie.getMovieId(), API_KEY_VALUE);

            call.enqueue(new Callback<TMDBTrailers>() {

                @Override
                public void onResponse(Call<TMDBTrailers> call, Response<TMDBTrailers> response) {
                    if (response.body() != null) {
                        List<TMDBTrailer> results = response.body().getResults();
                        ArrayList<TrailerEntry> trailers = new ArrayList<>();

                        for(TMDBTrailer result: results) {
                            trailers.add(new TrailerEntry(
                                    result.getId(),
                                    movie.getMovieId(),
                                    result.getKey(),
                                    result.getSite(),
                                    result.getType()));
//                            Log.d(LOG_TAG, "(PACK) downloadTrailers() - Trailer's key: " + result.getKey());
                        }

                        mAppExecutors.diskIO().execute(() -> {
                            mAppDatabase.trailerDao().insertTrailers(trailers);
                            Log.d(LOG_TAG, "(PACK) downloadTrailers() - Inserted trailers of the following movie into database: " + movie.getTitle());
                        });
                    }
                }

                @Override
                public void onFailure(Call<TMDBTrailers> call, Throwable t) {
                    Log.e(LOG_TAG, "Issue with downloading the movie's tailers");
                    mCallback.OnDownLoadOfDataFailed();
                }
            });
        }

        mIsDownloadOfTrailersFinished = true;
        checkAllDownloadsAreFinished();
    }
}
