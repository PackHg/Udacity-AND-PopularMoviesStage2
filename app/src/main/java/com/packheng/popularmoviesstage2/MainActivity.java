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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.packheng.popularmoviesstage2.TMDB.Movie;
import com.packheng.popularmoviesstage2.TMDB.MoviesAdapter;
import com.packheng.popularmoviesstage2.TMDB.TMDBEndpointInterface;
import com.packheng.popularmoviesstage2.TMDB.TMDBResponse;
import com.packheng.popularmoviesstage2.TMDB.TMDBResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.packheng.popularmoviesstage2.utils.NetworkUtils.isNetworkConnected;

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener  {

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";

    static ArrayList<Movie> movies;
    private String sortBy;
    private MoviesAdapter moviesAdapter;

    @BindView(R.id.movies_rv) RecyclerView moviesRecyclerView;
    @BindView(R.id.empty_tv) TextView emptyTextView;
    @BindView(R.id.swipe_refrsh) SwipeRefreshLayout swipeRefreshLayout;

    private TMDBEndpointInterface apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Set number of columns in the RecyclerView.
        int numberOfColumns = calculateBestSpanCount((int) getResources()
                .getDimension(R.dimen.main_movie_poster_width));

        movies = new ArrayList<>();

        moviesRecyclerView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);

        // Set up the RecyclerView.
        moviesRecyclerView.setHasFixedSize(true);
        moviesAdapter = new MoviesAdapter(this, movies);
        moviesRecyclerView.setAdapter(moviesAdapter);
        moviesRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.green, R.color.yellow);
        // Set up a setOnRefreshListener to  when user performs a swipe-to-refresh gesture.
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMoviesData();
            }
        });

        // Create the Retrofit instance and constructs a service leveraging TMBDEndpointInterface.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TMDB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(TMDBEndpointInterface.class);

        // Get the sort by type from SharedPreferences and register the listener
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sortBy = sp.getString(getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_most_popular));
        sp.registerOnSharedPreferenceChangeListener(this);

        loadMoviesData();
    }

    /**
     * Loads movies data.
     */
    private void loadMoviesData() {
        final String API_KEY_VALUE = BuildConfig.ApiKey;
        final String BASE_URL = "https://image.tmdb.org/t/p";
        final String IMAGE_SIZE = "/w185";
        final String EMPTY_STRING = "";

        swipeRefreshLayout.setRefreshing(true);

        if (isNetworkConnected(this)) {
            moviesRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.GONE);

            // Accessing the API
            Call<TMDBResponse> call;
            if (sortBy.equals(getString(R.string.pref_sort_by_top_rated))) {
                setActionBarTitle(getString(R.string.pref_sort_by_top_rated));
                call = apiService.topRatedMovies(API_KEY_VALUE);
            } else {
                setActionBarTitle(getString(R.string.pref_sort_by_most_popular));
                call = apiService.popularMovies(API_KEY_VALUE);
            }

            call.enqueue(new Callback<TMDBResponse>() {

                @Override
                public void onResponse(Call<TMDBResponse> call, Response<TMDBResponse> response) {
                    swipeRefreshLayout.setRefreshing(false);
                    emptyTextView.setVisibility(View.GONE);
                    moviesRecyclerView.setVisibility(View.VISIBLE);

                    if (response.body() != null) {
                        List<TMDBResult> results = response.body().getResults();
                        movies.clear();
                        for (TMDBResult result: results) {
                            Movie movie = new Movie();
                            if (result != null) {
                                movie.setTitle(result.getOriginalTitle());
                                if (!result.getPosterPath().isEmpty()) {
                                    movie.setPosterUrl(BASE_URL + IMAGE_SIZE + result.getPosterPath());
                                } else {
                                    movie.setPosterUrl(EMPTY_STRING);
                                }
                                movie.setPlotSynopsis(result.getOverview());
                                movie.setUserRating(result.getVoteAverage());
                                movie.setReleaseDate(result.getReleaseDate());
                            }
                            movies.add(movie);
                        }
                        moviesAdapter.notifyDataSetChanged();
                    } else {
                        moviesRecyclerView.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                        emptyTextView.setText(R.string.no_movies_data_found);
                    }

                }

                @Override
                public void onFailure(Call<TMDBResponse> call, Throwable t) {
                    swipeRefreshLayout.setRefreshing(false);
                    moviesRecyclerView.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                    emptyTextView.setText(R.string.issue_with_fetching_data);
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
            moviesRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.no_internet);
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
                loadMoviesData();
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
            String sortByPref = sharedPreferences.getString(key, sortBy);
            if (!sortBy.equals(sortByPref)) {
                sortBy = sortByPref;
                loadMoviesData();
            }
        }
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
}
