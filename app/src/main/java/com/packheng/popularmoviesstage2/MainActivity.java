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

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.packheng.popularmoviesstage2.data.api.TMDBEndpointInterface;
import com.packheng.popularmoviesstage2.data.api.TMDBMovie;
import com.packheng.popularmoviesstage2.data.api.TMDBMovies;
import com.packheng.popularmoviesstage2.data.api.TMDBReview;
import com.packheng.popularmoviesstage2.data.api.TMDBReviews;
import com.packheng.popularmoviesstage2.data.api.TMDBTrailer;
import com.packheng.popularmoviesstage2.data.api.TMDBTrailers;
import com.packheng.popularmoviesstage2.adapter.MovieAdapter;
import com.packheng.popularmoviesstage2.databinding.ActivityMainBinding;
import com.packheng.popularmoviesstage2.data.database.AppDatabase;
import com.packheng.popularmoviesstage2.data.database.FavoriteEntry;
import com.packheng.popularmoviesstage2.data.database.MovieEntry;
import com.packheng.popularmoviesstage2.data.database.ReviewEntry;
import com.packheng.popularmoviesstage2.data.database.TrailerEntry;
import com.packheng.popularmoviesstage2.utils.AppExecutors;
import com.packheng.popularmoviesstage2.viewmodel.MainViewModel;
import com.packheng.popularmoviesstage2.viewmodel.MainViewModelFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.packheng.popularmoviesstage2.utils.DateToStringUtils.stringToDate;
import static com.packheng.popularmoviesstage2.utils.NetworkUtils.isNetworkConnected;

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener, MovieAdapter.ItemClickListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String API_KEY_VALUE = BuildConfig.ApiKey;
    private static final String BASE_URL = "https://image.tmdb.org/t/p";
    private static final String IMAGE_SIZE = "/w185";
    private static final String EMPTY_STRING = "";

    private ArrayList<MovieEntry> mMovies;
    private String mSortBy;
    private MovieAdapter mMovieAdapter;

    private ArrayList<FavoriteEntry> mFavorites;

    // For binding with the components of the layout
    private ActivityMainBinding mMainBinding;

    private TMDBEndpointInterface mApiService;

    private AppDatabase mDatabase;

    // Tag used for saving and restoring data into and from SharedPreferences
    private static final String USER_DATA = "user data";
    // Key used for saving and restoring the mIsDownloaded boolean into and from SharedPreferences
    private static final String KEY_IS_DOWNLOADED = "key is downloaded";
    // For tracking whether the movies have already been downloaded.
    private boolean mIsDownloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "(PACK) onCreate()");

        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        SharedPreferences sp = getSharedPreferences(USER_DATA, 0);
        if (sp != null) {
            mIsDownloaded = sp.getBoolean(KEY_IS_DOWNLOADED, mIsDownloaded);
        }

        // Get the sort by type from SharedPreferences and register the listener
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSortBy = prefs.getString(getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_most_popular));
        prefs.registerOnSharedPreferenceChangeListener(this);

        mMovies = new ArrayList<>();

        mMainBinding.mainRecyclerView.setVisibility(View.VISIBLE);
        mMainBinding.mainEmptyTextView.setVisibility(View.GONE);

        // Set up the RecyclerView.
        mMainBinding.mainRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter(this, new ArrayList<>(mMovies), this);
        mMainBinding.mainRecyclerView.setAdapter(mMovieAdapter);
        int numberOfColumns = calculateBestSpanCount((int) getResources()
                .getDimension(R.dimen.main_movie_poster_width));
        mMainBinding.mainRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        mMainBinding.mainSwipeRefresh.setColorSchemeResources(R.color.colorPrimary, R.color.green, R.color.yellow);
        // Set up a setOnRefreshListener to load the movies when user performs a swipe-to-refresh gesture.
        mMainBinding.mainSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadData();
            }
        });

        // Create the Retrofit instance and constructs a service leveraging TMBDEndpointInterface.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mApiService = retrofit.create(TMDBEndpointInterface.class);

        mDatabase = AppDatabase.getInstance(getApplicationContext());

        if (!mIsDownloaded && !mSortBy.equals(getString(R.string.pref_sort_by_favorites))) {
            downloadData();
        }

        // Setup a MainViewModel
        MainViewModelFactory factory = new MainViewModelFactory(mDatabase);
        final MainViewModel mainViewModel = ViewModelProviders.of(this, factory)
                .get(MainViewModel.class);

        mainViewModel.getObservableFavorites().observe(this, favoriteEntries -> {
            if (favoriteEntries != null) {
                mFavorites = (ArrayList<FavoriteEntry>) favoriteEntries;
                if (mSortBy.equals(getString(R.string.pref_sort_by_favorites))) {
                    updateUI();
                }
            }
        });

        mainViewModel.getObservableMovies().observe(this, movieEntries -> {
            if (movieEntries != null) {
                mMovies = (ArrayList<MovieEntry>) movieEntries;
                if (mSortBy.equals(getString(R.string.pref_sort_by_most_popular)) ||
                        mSortBy.equals(getString(R.string.pref_sort_by_top_rated))) {
                    updateUI();
                }
            }
        });
    }

    private void updateUI() {
        Log.d(LOG_TAG, "(PACK) updateUI() - mSortBy = " + mSortBy);

        setActionBarTitle(mSortBy);
        // Update the state of the option menu item "refresh"
        invalidateOptionsMenu();

        if (mSortBy.equals(getString(R.string.pref_sort_by_favorites))) {
            mMainBinding.mainRecyclerView.setVisibility(View.VISIBLE);
            mMainBinding.mainEmptyTextView.setVisibility(View.GONE);
            mMainBinding.mainSwipeRefresh.setEnabled(false);

            if (mFavorites == null || mFavorites.size() == 0) {
                Log.d(LOG_TAG, "(PACK) updateUI() - mFavorites is null or its size = 0");
                mMainBinding.mainRecyclerView.setVisibility(View.GONE);
                mMainBinding.mainEmptyTextView.setVisibility(View.VISIBLE);
                mMainBinding.mainEmptyTextView.setText(getString(R.string.no_favorites));
                return;
            }

            Log.d(LOG_TAG, String.format("(PACK) updateUI() - mFavorites.size() = %d", mFavorites.size()));

            mMovieAdapter.setMovies(new ArrayList<>(mFavorites));
            return;
        }

        mMainBinding.mainSwipeRefresh.setEnabled(true);
        mMovieAdapter.setMovies(new ArrayList<>(mMovies));
    }

    /**
     * Downloads the movies data from the TMDB API into the database.
     */
    private void downloadData() {
        Log.d(LOG_TAG, "(PACK) downloadData()");

        mMainBinding.mainSwipeRefresh.setRefreshing(true);
        mMainBinding.mainRecyclerView.setVisibility(View.VISIBLE);

        if (isNetworkConnected(this)) {
            mMainBinding.mainEmptyTextView.setVisibility(View.GONE);

            downloadMovies();

        } else {
            mMainBinding.mainSwipeRefresh.setRefreshing(false);
            mMainBinding.mainRecyclerView.setVisibility(View.GONE);
            mMainBinding.mainEmptyTextView.setVisibility(View.VISIBLE);
            mMainBinding.mainEmptyTextView.setText(R.string.no_internet);
        }
    }

    /**
     * Download movies' data
     */
    private void downloadMovies() {
        // Accessing the API
        Log.d(LOG_TAG, "(PACK) downloadMovies() - Starts loading movies from API.");

        Call<TMDBMovies> call;

        if (mSortBy.equals(getString(R.string.pref_sort_by_most_popular))) {
            call = mApiService.popularMovies(API_KEY_VALUE);
        } else if (mSortBy.equals(getString(R.string.pref_sort_by_top_rated))) {
            call = mApiService.topRatedMovies(API_KEY_VALUE);
        } else {
            Log.e(LOG_TAG, "downloadMovies() - mSortBy is unknown: " + mSortBy);
            return;
        }

        call.enqueue(new Callback<TMDBMovies>() {

            @Override
            public void onResponse(Call<TMDBMovies> call, Response<TMDBMovies> response) {
                mMainBinding.mainSwipeRefresh.setRefreshing(false);

                if (response.body() != null) {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {

                        @Override
                        public void run() {
                            List<TMDBMovie> results = response.body().getResults();
                            mMovies.clear();
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
                                    mMovies.add(movie);
                                }
                            }

                            mDatabase.movieDao().deleteAllMovies();
                            Log.d(LOG_TAG, "(PACK) downloadMovies() - deleted all movies in database.");
                            mDatabase.movieDao().insertMovies(mMovies);
                            Log.d(LOG_TAG, "(PACK) downloadMovies() - inserted downloaded movies into database.");
                            mIsDownloaded = true;

                            downloadReviews();
                            downloadTrailers();
                        }
                    });
                } else {
                    mMainBinding.mainRecyclerView.setVisibility(View.GONE);
                    mMainBinding.mainEmptyTextView.setVisibility(View.VISIBLE);
                    mMainBinding.mainEmptyTextView.setText(R.string.no_movies_data_found);
                }
            }

            @Override
            public void onFailure(Call<TMDBMovies> call, Throwable t) {
                mMainBinding.mainSwipeRefresh.setRefreshing(false);
                mMainBinding.mainRecyclerView.setVisibility(View.GONE);
                mMainBinding.mainEmptyTextView.setVisibility(View.VISIBLE);
                mMainBinding.mainEmptyTextView.setText(R.string.issue_with_fetching_movie_data);
            }
        });
    }

    /**
     * Download reviews' data
     */
    private void downloadReviews() {

        // Delete all existing reviews
        AppExecutors.getInstance().diskIO().execute(() -> {
            mDatabase.reviewDao().deleteAllReviews();
            Log.d(LOG_TAG, "(PACK) downloadReviews() - Deleted all reviews from database.");
        });

        // Accessing the API
        Log.d(LOG_TAG, "(PACK) downloadReviews() - Starts loading reviews from API.");

        for(MovieEntry movie: mMovies) {
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

                            AppExecutors.getInstance().diskIO().execute(() -> {
                                mDatabase.reviewDao().insertReviews(reviews);
                                Log.d(LOG_TAG, "(PACK) downloadReviews() - inserted reviews of the following movie into database: " + movie.getTitle());
                            });
                    }
                }

                @Override
                public void onFailure(Call<TMDBReviews> call, Throwable t) {
                    Log.e(LOG_TAG, "Issue with dowloading the movie's reviews");
                }
            });
        }
    }

    /**
     * Download trailers' data
     */
    private void downloadTrailers() {

        // Delete all existing trailers
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.trailerDao().deleteAllTrailers();
                Log.d(LOG_TAG, "(PACK) downloadTrailers() - Deleted all trailers from database.");
            }
        });

        // Accessing the API
        Log.d(LOG_TAG, "(PACK) downloadTrailers() - Starts loading trailers from API.");

        for(MovieEntry movie: mMovies) {
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

                        AppExecutors.getInstance().diskIO().execute(() ->
                                mDatabase.trailerDao().insertTrailers(trailers));
                        Log.d(LOG_TAG, "(PACK) downloadTrailers() - Inserted trailers of the following movie into database: " + movie.getTitle());

                    }
                }

                @Override
                public void onFailure(Call<TMDBTrailers> call, Throwable t) {
                    Log.e(LOG_TAG, "Issue with dowloading the movie's tailers");
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_item_refresh:
                downloadData();
                return true;

            case R.id.menu_item_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.menu_item_refresh);
        if (mSortBy.equals(getString(R.string.pref_sort_by_favorites))) {
            menuItem.setEnabled(false);
        } else {
            menuItem.setEnabled(true);
        }
        return true;
    }

    // Reloads the movies if the shared preference (sort by) changes
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_by_key))) {
            String sortByPref = sharedPreferences.getString(key, mSortBy);

            Log.d(LOG_TAG, "(PACK) onSharedPreferenceChanged() - mSortBy = " + mSortBy);
            Log.d(LOG_TAG, "(PACK) onSharedPreferenceChanged() - sortByPref = " + sortByPref);

            if (sortByPref.equals(getString(R.string.pref_sort_by_favorites))) {
                mSortBy = sortByPref;
                updateUI();
                return;
            }

            if (!mSortBy.equals(sortByPref)) {
                mSortBy = sortByPref;
                downloadData();
                // TODO: update UI after download is completed?
                updateUI();
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = getSharedPreferences(USER_DATA, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_IS_DOWNLOADED, mIsDownloaded);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Calculates best number of columns in the grid view depending of the poster width
     * and the screen width.
     *
     * @param posterWidth Width of the poster in dp.
     * @return number of columns.
     */
    private int calculateBestSpanCount(int posterWidth) {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float screenWidth = outMetrics.widthPixels;
        return Math.round(screenWidth / posterWidth);
    }

    /**
     * Sets the title of the action bar according to the sort by.
     *
     * @param sortBy as chosen by the user.
     */
    private void setActionBarTitle(String sortBy) {
        ActionBar actionBar = getSupportActionBar();
        String title = "";

        if (sortBy.equals(getString(R.string.pref_sort_by_most_popular))) {
            title = getString(R.string.pref_sort_by_most_popular);
        } else if (sortBy.equals(getString(R.string.pref_sort_by_top_rated))) {
            title = getString(R.string.pref_sort_by_top_rated);
        } else if (sortBy.equals(getString(R.string.pref_sort_by_favorites))) {
            title = getString(R.string.pref_sort_by_favorites);
        } else {
            Log.e(LOG_TAG, "setActionBarTitle(String sortBy) - sortBy is unknown: " + sortBy);
            return;
        }

        if (actionBar != null) {
            actionBar.setTitle(title);
        }

    }

    @Override
    public void onItemClickListener(int actualMovieId) {
        // Launch DetailActivity with the movieId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_MOVIE_ID, actualMovieId);
        startActivity(intent);
    }
}
