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

import com.packheng.popularmoviesstage2.TMDB.TMDBEndpointInterface;
import com.packheng.popularmoviesstage2.TMDB.TMDBMovie;
import com.packheng.popularmoviesstage2.TMDB.TMDBMovies;
import com.packheng.popularmoviesstage2.databinding.ActivityMainBinding;
import com.packheng.popularmoviesstage2.db.AppDatabase;
import com.packheng.popularmoviesstage2.db.MovieEntry;

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

    static ArrayList<MovieEntry> mMovies;
    private String mSortBy;
    private MovieAdapter mMovieAdapter;

    // For binding with the components of the layout
    ActivityMainBinding mMainBinding;

    private TMDBEndpointInterface mApiService;

    private AppDatabase mDb;

    // Tag used for saving and restoring data into and from SharedPreferences
    private static final String USER_DATA = "user data";
    // Key used for saving and restoring the mIsDownloaded boolean into and from SharedPreferences
    private static final String KEY_IS_DOWNLOADED = "key is downloaded";
    // For tracking whether the movies have already been downloaded.
    private boolean mIsDownloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        mMovies = new ArrayList<MovieEntry>();

        mMainBinding.mainRecyclerView.setVisibility(View.VISIBLE);
        mMainBinding.mainEmptyTextView.setVisibility(View.GONE);

        // Set up the RecyclerView.
        mMainBinding.mainRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter(this, mMovies, this);
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

        mDb = AppDatabase.getsIntance(getApplicationContext());

        // Setup a MainViewModel
        MainViewModelFactory factory = new MainViewModelFactory(mDb);
        final MainViewModel mainViewModel = ViewModelProviders.of(this, factory)
                .get(MainViewModel.class);
        mainViewModel.getMovies().observe(this, movieEntries -> {
            if (movieEntries != null) {
                mMovieAdapter.setMovies(movieEntries);
            }
        });

        if (!mIsDownloaded) {
            downloadData();
        }
    }

    /**
     * Downloads the movies data from the TMDB API into the database.
     */
    private void downloadData() {

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

    private void downloadMovies() {
        // Accessing the API
        Log.d(LOG_TAG, "downloadMovies() - Starts loading movies from API.");

        Call<TMDBMovies> call;
        if (mSortBy.equals(getString(R.string.pref_sort_by_top_rated))) {
            setActionBarTitle(getString(R.string.pref_sort_by_top_rated));
            call = mApiService.topRatedMovies(API_KEY_VALUE);
        } else {
            setActionBarTitle(getString(R.string.pref_sort_by_most_popular));
            call = mApiService.popularMovies(API_KEY_VALUE);
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
                                    Log.d(LOG_TAG, "downloadData() - movie title : " + title + ".");
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
                                            userRating, releaseDate, false);
                                    mMovies.add(movie);
                                }
                            }

                            mDb.movieDao().deleteAll();
                            Log.d(LOG_TAG, "downloadMovies() - deleted all movies in database.");
                            mDb.movieDao().insertAll(mMovies);
                            Log.d(LOG_TAG, "downloadMovies() - inserted downloaded movies in movie database.");
                            mIsDownloaded = true;
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
                mMainBinding.mainEmptyTextView.setText(R.string.issue_with_fetching_data);
            }
        });
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

    // Reloads the movies if the shared preference (sort by) changes
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_by_key))) {
            String sortByPref = sharedPreferences.getString(key, mSortBy);
            if (!mSortBy.equals(sortByPref)) {
                mSortBy = sortByPref;
                downloadData();
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
     * Sets the title of the action bar.
     *
     * @param title of the action bar.
     */
    private void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void onItemClickListener(int movieId) {
        // Launch DetailActivity with the movieId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_MOVIE_ID, movieId);
        startActivity(intent);
    }
}
