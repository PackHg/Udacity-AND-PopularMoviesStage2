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
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.packheng.popularmoviesstage2.adapter.MovieAdapter;
import com.packheng.popularmoviesstage2.data.DataRepository;
import com.packheng.popularmoviesstage2.data.api.TMDBEndpointInterface;
import com.packheng.popularmoviesstage2.data.database.AppDatabase;
import com.packheng.popularmoviesstage2.data.database.FavoriteEntry;
import com.packheng.popularmoviesstage2.data.database.MovieEntry;
import com.packheng.popularmoviesstage2.databinding.ActivityMainBinding;
import com.packheng.popularmoviesstage2.utils.AppExecutors;
import com.packheng.popularmoviesstage2.viewmodel.MainViewModel;
import com.packheng.popularmoviesstage2.viewmodel.MainViewModelFactory;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.packheng.popularmoviesstage2.utils.NetworkUtils.isNetworkConnected;

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener, MovieAdapter.ItemClickListener,
        DataRepository.OnDownloadOfDataListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";

    private ArrayList<MovieEntry> mMovies;
    private ArrayList<FavoriteEntry> mFavorites;
    private MovieAdapter mMovieAdapter;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;
    private TextView mEmptyTextView;

    static DataRepository mDataRepository;

    // Tag used for saving and restoring data into and from SharedPreferences
    private static final String USER_DATA = "user data";
    // Key used for saving and restoring the mIsDownloaded boolean into and from SharedPreferences
    private static final String KEY_IS_DOWNLOADED = "key is downloaded";
    // For tracking whether the movies have been downloaded once.
    private boolean mIsDownloaded = false;
    private String mSortBy;
    // Key used for saving and restoring the mLastDownloadIsBy String into and from SharedPreferences
    private static final String KEY_LAST_DOWNLOAD_IS_BY = "key lastDownloadIsBy";
    // For remembering what the sort-by used for the last download is
    private String mLastDownloadIsBy = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mRecyclerView = binding.mainRecyclerView;
        mSwipeRefresh = binding.mainSwipeRefresh;
        mEmptyTextView = binding.mainEmptyTextView;

        SharedPreferences sp = getSharedPreferences(USER_DATA, 0);
        if (sp != null) {
            mIsDownloaded = sp.getBoolean(KEY_IS_DOWNLOADED, mIsDownloaded);
            mLastDownloadIsBy = sp.getString(KEY_LAST_DOWNLOAD_IS_BY, mLastDownloadIsBy);
        }

        // Get the sort-by type from SharedPreferences and register the listener
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSortBy = prefs.getString(getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_most_popular));
        prefs.registerOnSharedPreferenceChangeListener(this);

        mMovies = new ArrayList<>();

        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyTextView.setVisibility(View.GONE);

        // Set up the RecyclerView.
        mRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter(this, new ArrayList<>(mMovies), this);
        mRecyclerView.setAdapter(mMovieAdapter);
        int numberOfColumns = calculateBestSpanCount((int) getResources()
                .getDimension(R.dimen.main_movie_poster_width));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorGreen, R.color.colorYellow);
        // Set up a setOnRefreshListener to download the movies data when user performs a swipe-to-refresh gesture.
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadData();
            }
        });

        // Setup a DataRepository instance
        AppDatabase database = AppDatabase.getInstance(getApplicationContext());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        TMDBEndpointInterface apiService = retrofit.create(TMDBEndpointInterface.class);
        AppExecutors executors = AppExecutors.getInstance();
        mDataRepository = DataRepository.getInstance(database, apiService, executors,this);

        if (!mIsDownloaded && !mSortBy.equals(getString(R.string.pref_sort_by_favorites))) {
            downloadData();
        }

        // Setup a MainViewModel
        MainViewModelFactory factory = new MainViewModelFactory(mDataRepository);
        final MainViewModel mainViewModel = ViewModelProviders.of(this, factory)
                .get(MainViewModel.class);

        mainViewModel.getAllObservableFavorites().observe(this, favoriteEntries -> {
            if (favoriteEntries != null) {
                mFavorites = (ArrayList<FavoriteEntry>) favoriteEntries;
                if (mSortBy.equals(getString(R.string.pref_sort_by_favorites))) {
                    updateUI();
                }
            }
        });

        mainViewModel.getAllObservableMovies().observe(this, movieEntries -> {
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

        setActionBarTitle(mSortBy);
        // Update the state of the option menu item "refresh"
        invalidateOptionsMenu();

        if (mSortBy.equals(getString(R.string.pref_sort_by_favorites))) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyTextView.setVisibility(View.GONE);
            mSwipeRefresh.setEnabled(false);

            if (mFavorites == null || mFavorites.size() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                mEmptyTextView.setVisibility(View.VISIBLE);
                mEmptyTextView.setText(getString(R.string.no_favorites));
                return;
            }

            mMovieAdapter.setMovies(new ArrayList<>(mFavorites));
            return;
        }

        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyTextView.setVisibility(View.GONE);
        mSwipeRefresh.setEnabled(true);
        mSwipeRefresh.setRefreshing(false);
        mMovieAdapter.setMovies(new ArrayList<>(mMovies));
    }

    /**
     * Downloads the movies data.
     */
    private void downloadData() {

        mSwipeRefresh.setRefreshing(true);
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyTextView.setVisibility(View.GONE);

        if (isNetworkConnected(this)) {
            mIsDownloaded = true;
            mLastDownloadIsBy = mSortBy;
            mDataRepository.downloadMovies(mSortBy);

        } else {
            mSwipeRefresh.setRefreshing(false);
            mRecyclerView.setVisibility(View.GONE);
            mEmptyTextView.setVisibility(View.VISIBLE);
            mEmptyTextView.setText(R.string.no_internet);
        }
    }

    @Override
    public void OnDownloadOfDataFinished() {
        mSwipeRefresh.setRefreshing(false);
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyTextView.setVisibility(View.GONE);
    }

    @Override
    public void OnDownLoadOfDataFailed() {
        mSwipeRefresh.setRefreshing(false);
        mRecyclerView.setVisibility(View.GONE);
        mEmptyTextView.setVisibility(View.VISIBLE);
        mEmptyTextView.setText(R.string.issue_with_fetching_movie_data);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_by_key))) {
            String sortByPref = sharedPreferences.getString(key, mSortBy);

            if (sortByPref.equals(getString(R.string.pref_sort_by_favorites)) || sortByPref.equals(mLastDownloadIsBy)) {
                mSortBy = sortByPref;
                updateUI();
                return;
            }

            if (!mSortBy.equals(sortByPref)) {
                mSortBy = sortByPref;
                downloadData();
                updateUI();
                return;
            }

            mSortBy = sortByPref;
            updateUI();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sp = getSharedPreferences(USER_DATA, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_IS_DOWNLOADED, mIsDownloaded);
        editor.putString(KEY_LAST_DOWNLOAD_IS_BY, mLastDownloadIsBy);
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
            Log.w(LOG_TAG, "setActionBarTitle(String sortBy) - sortBy is unknown: " + sortBy);
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
