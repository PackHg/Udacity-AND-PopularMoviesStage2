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
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.packheng.popularmoviesstage2.databinding.ActivityDetailBinding;
import com.packheng.popularmoviesstage2.db.AppDatabase;
import com.packheng.popularmoviesstage2.db.MovieEntry;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Locale;

import static com.packheng.popularmoviesstage2.utils.DateToStringUtils.formatDateToString;

/**
 * Shows the details of a movie.
 */
public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    // Extra for the task ID to be received in the intent
    public static final String EXTRA_MOVIE_ID = "extra movie id";
    // Extra for the task ID to be received after rotation
    public static final String INSTANCE_MOVIE_ID = "instance movie id";
    public static final int DEFAULT_MOVIE_ID = -1;

    private int mMovieId = DEFAULT_MOVIE_ID;

    private AppDatabase mDb;

    ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mDb = AppDatabase.getsIntance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_MOVIE_ID)) {
            mMovieId = savedInstanceState.getInt(INSTANCE_MOVIE_ID, DEFAULT_MOVIE_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_MOVIE_ID)) {
            mMovieId = intent.getIntExtra(EXTRA_MOVIE_ID, mMovieId);
        }

        // Setup a DetailViewModel
        DetailViewModelFactory factory = new DetailViewModelFactory(mDb, mMovieId);
        final DetailViewModel detailViewModel = ViewModelProviders.of(this, factory)
                .get(DetailViewModel.class);
        detailViewModel.getMovie().observe(this, movieEntry -> {
            if (movieEntry != null) {
                bindMovieToUI(movieEntry);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_MOVIE_ID, mMovieId);
        super.onSaveInstanceState(outState);
    }

    private void bindMovieToUI(MovieEntry movie) {

        // Movie poster
        String posterUrl = movie.getPosterUrl();
        if (!posterUrl.isEmpty()) {
            mDetailBinding.detailPosterEmptyTextView.setVisibility(View.GONE);
            Picasso.with(this).load(movie.getPosterUrl()).into(mDetailBinding.detailPosterImageView);
        } else {
            mDetailBinding.detailPosterEmptyTextView.setVisibility(View.VISIBLE);
        }

        // Movie title
        mDetailBinding.detailTitleTextView.setText(movie.getTitle());
        mDetailBinding.detailUserRatingTextView.setText(String.format(Locale.getDefault(), "%1.1f", movie.getUserRating()));

        // Movie release date
        Date releaseDate = movie.getReleaseDate();
        if (releaseDate != null) {
            mDetailBinding.detailReleaseDateTextView.setText(formatDateToString(releaseDate));
        } else {
            mDetailBinding.detailTitleTextView.setText(getString(R.string.unknown));
        }

        // Movie plot synopsis
        String overview = movie.getPlotSynopsis();
        if (!overview.isEmpty()) {
            mDetailBinding.detailPlotSynopsisTextView.setText(movie.getPlotSynopsis());
        } else {
            mDetailBinding.detailPlotSynopsisTextView.setText(getString(R.string.no_plot_synopsis_found));
        }
    }
}
