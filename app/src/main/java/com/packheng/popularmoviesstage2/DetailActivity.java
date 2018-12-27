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

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.packheng.popularmoviesstage2.adapter.ReviewAdapter;
import com.packheng.popularmoviesstage2.adapter.TrailerAdapter;
import com.packheng.popularmoviesstage2.databinding.ActivityDetailBinding;
import com.packheng.popularmoviesstage2.db.AppDatabase;
import com.packheng.popularmoviesstage2.db.FavoriteEntry;
import com.packheng.popularmoviesstage2.db.FavoriteReviewEntry;
import com.packheng.popularmoviesstage2.db.FavoriteTrailerEntry;
import com.packheng.popularmoviesstage2.db.Movie;
import com.packheng.popularmoviesstage2.db.MovieEntry;
import com.packheng.popularmoviesstage2.db.Review;
import com.packheng.popularmoviesstage2.db.ReviewEntry;
import com.packheng.popularmoviesstage2.db.Trailer;
import com.packheng.popularmoviesstage2.db.TrailerEntry;
import com.packheng.popularmoviesstage2.utils.AppExecutors;
import com.packheng.popularmoviesstage2.viewmodel.DetailViewModel;
import com.packheng.popularmoviesstage2.viewmodel.DetailViewModelFactory;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.packheng.popularmoviesstage2.utils.DateToStringUtils.formatDateToString;
import static com.packheng.popularmoviesstage2.utils.Utils.launchYoutubeVideo;

/**
 * Shows the details of a movie.
 */
public class DetailActivity extends AppCompatActivity implements TrailerAdapter.ItemClickListener{

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    // Extra for the task ID to be received in the intent
    public static final String EXTRA_MOVIE_ID = "extra movie id";
    // Key for the task ID to be received after rotation
    private static final String INSTANCE_MOVIE_ID = "instance movie id";
    private static final int DEFAULT_MOVIE_ID = -1;

    private int mMovieId = DEFAULT_MOVIE_ID;
    private MovieEntry mMovie;
    private ArrayList<ReviewEntry> mReviews;
    private ArrayList<TrailerEntry> mTrailers;

    private ActivityDetailBinding mDetailBinding;

    private ReviewAdapter mReviewAdapter;
    private TrailerAdapter mTrailerAdapter;

    private AppDatabase mDatabase;

    private boolean mIsFavorite = false;
    private FavoriteEntry mFavorite;
    private ArrayList<FavoriteReviewEntry> mFavoriteReviews;
    private ArrayList<FavoriteTrailerEntry> mFavoriteTrailers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mDatabase = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_MOVIE_ID)) {
            mMovieId = savedInstanceState.getInt(INSTANCE_MOVIE_ID, DEFAULT_MOVIE_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_MOVIE_ID)) {
            mMovieId = intent.getIntExtra(EXTRA_MOVIE_ID, mMovieId);
        }

        Log.d(LOG_TAG, "(PACK) onCreate() - mMovieId = " + mMovieId);

        /*
         * Set up the RecyclerView for the movie's reviews
         */
        mReviewAdapter = new ReviewAdapter(this, new ArrayList<Review>());
        mDetailBinding.detailReviewRecyclerView.setAdapter(mReviewAdapter);
        mDetailBinding.detailReviewRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mDetailBinding.detailReviewRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        /*
         * Set up the RecyclerView for the movie's trailers
         */
        mTrailerAdapter = new TrailerAdapter(this, new ArrayList<Trailer>(), this);
        mDetailBinding.detailTrailerRecyclerView.setAdapter(mTrailerAdapter);
        mDetailBinding.detailTrailerRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        /*
         * Setup a DetailViewModel
         */
        DetailViewModelFactory factory = new DetailViewModelFactory(mDatabase, mMovieId);
        final DetailViewModel detailViewModel = ViewModelProviders.of(this, factory)
                .get(DetailViewModel.class);

        // Observe favorite data
        detailViewModel.getObservableFavorite().observe(this, favoriteEntry -> {

            if (favoriteEntry != null) {
                mIsFavorite = true;
                setStarCheckboxChecked(true);
                mFavorite = favoriteEntry;
                // TODO: if the movie has been a favorite then update the UI with data from the favorites database.

                bindDataToUI(mFavorite);

                // Observe favorite review data
                detailViewModel.getObservableFavoriteReviews().observe(this, favoriteReviewEntries -> {
                    if (favoriteReviewEntries != null) {
                        mFavoriteReviews = (ArrayList<FavoriteReviewEntry>) favoriteReviewEntries;
                        // Display the number of reviews on UI
                        mDetailBinding.detailNumberOfReviews.setText(String.format(Locale.getDefault(),
                                "%d %s", mFavoriteReviews.size(), getString(R.string.reviews)));
                        mReviewAdapter.setReviews(new ArrayList<>(mFavoriteReviews));
                    }
                });

                // Observe favorite trailer data
                detailViewModel.getObservableFavoriteTrailers().observe(this, favoriteTrailerEntries -> {
                    if (favoriteTrailerEntries != null) {
                        mFavoriteTrailers = (ArrayList<FavoriteTrailerEntry>) favoriteTrailerEntries;
                        mDetailBinding.detailNumberOfTrailers.setText(
                                String.format(Locale.getDefault(), "%d %s", mFavoriteTrailers.size(), getString(R.string.trailers)));
                        mTrailerAdapter.setTrailers(new ArrayList<>(mFavoriteTrailers));
                    }
                });

                return;
            }

            // Observe movie data
            detailViewModel.getObservableMovie().observe(this, movieEntry -> {
                if (movieEntry != null) {
                    mMovie = movieEntry;

                    bindDataToUI(mMovie);
                    // Observe review data
                    detailViewModel.getObservableReviews().observe(this, reviewEntries -> {
                        if (reviewEntries != null) {
                            mReviews = (ArrayList<ReviewEntry>) reviewEntries;
                            // Display the number of reviews on UI
                            mDetailBinding.detailNumberOfReviews.setText(String.format(Locale.getDefault(),
                                    "%d %s", mReviews.size(), getString(R.string.reviews)));
                            mReviewAdapter.setReviews(new ArrayList<>(mReviews));
                        }
                    });

                    // Observe trailer data
                    detailViewModel.getObservableTrailers().observe(this, trailerEntries -> {
                        if (trailerEntries != null){
                            mTrailers = (ArrayList<TrailerEntry>) trailerEntries;
                            mDetailBinding.detailNumberOfTrailers.setText(
                                    String.format(Locale.getDefault(), "%d %s", trailerEntries.size(), getString(R.string.trailers)));
                            mTrailerAdapter.setTrailers(new ArrayList<>(mTrailers));
                        }
                    });
                }
            });
        });

        // Observe the star checkbox state
        mDetailBinding.detailFavoriteCheckbox.setOnClickListener(v -> {
            mIsFavorite = mDetailBinding.detailFavoriteCheckbox.isChecked();
            if (mIsFavorite) {
                Toast.makeText(DetailActivity.this, getString(R.string.marked_as_favorite),
                        Toast.LENGTH_SHORT).show();

                if (mFavorite == null) {
                    // Add movie data into the favorite tables

                    mFavorite = copyMovieEntryToFavoriteEntry(mMovie);
                    mFavoriteReviews = copyReviewEntriesToFavoriteReviewEntries(mReviews);
                    mFavoriteTrailers = copyTrailerEntriesToFavoriteTrailerEntries(mTrailers);

                    AppExecutors.getInstance().diskIO().execute(() -> {
                        mDatabase.favoriteDao().insertFavorite(mFavorite);
                        Log.d(LOG_TAG, String.format("(PACK) - Movie %d is added into favorites database", mMovieId));
                        if (mFavoriteReviews != null) {
                            mDatabase.favoriteReviewDao().insertFavoriteReviews(mFavoriteReviews);
                            Log.d(LOG_TAG, String.format("(PACK) - Reviews of movie %d are added into favorites database", mMovieId));
                        }
                        if(mFavoriteTrailers != null) {
                            mDatabase.favoriteTrailerDao().insertFavoriteTrailers(mFavoriteTrailers);
                            Log.d(LOG_TAG, String.format("(PACK) - Trailers of movie %d are added into favorites database", mMovieId));
                        }
                    });
                }
            } else {
                Toast.makeText(DetailActivity.this, getString(R.string.unmarked_as_favorite),
                        Toast.LENGTH_SHORT).show();
                if (mFavorite != null) {
                    // Remove movie data from the favorite tables

                    AppExecutors.getInstance().diskIO().execute(() -> {
                        mDatabase.favoriteDao().deleteFavoriteWithMovieId(mMovieId);
                        Log.d(LOG_TAG, String.format("(PACK) - Movie %d is removed from favorites database", mMovieId));
                        mDatabase.favoriteReviewDao().deleteAllFavoriteReviewsWithMovieId(mMovieId);
                        Log.d(LOG_TAG, String.format("(PACK) - Removed reviews of movie %d from favorites database", mMovieId));
                        mDatabase.favoriteTrailerDao().deleteAllFavoriteTrailersWithMovieId(mMovieId);
                        Log.d(LOG_TAG, String.format("(PACK) - Removed trailers of movie %d from favorites database", mMovieId));
                    });
                    mFavorite = null;
                    mFavoriteReviews = null;
                    mFavoriteTrailers = null;
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_MOVIE_ID, mMovieId);
        super.onSaveInstanceState(outState);
    }

//    @SuppressLint("DefaultLocale")
//    private void bindDataToUI(MovieEntry movie) {
    private void bindDataToUI(Movie movie) {

        Log.d(LOG_TAG, "(PACK) bindDataToUI() - Movie title = " + movie.getTitle());

        /*
         * Movie poster
         */
        String posterUrl = movie.getPosterUrl();
        if (!posterUrl.isEmpty()) {
            mDetailBinding.detailPosterEmptyTextView.setVisibility(View.GONE);
            Picasso.with(this).load(movie.getPosterUrl()).into(mDetailBinding.detailPosterImageView);
        } else {
            mDetailBinding.detailPosterEmptyTextView.setVisibility(View.VISIBLE);
        }

        /*
         * Movie title
         */
        mDetailBinding.detailTitleTextView.setText(movie.getTitle());
        mDetailBinding.detailUserRatingTextView.setText(String.format(Locale.getDefault(), "%1.1f", movie.getUserRating()));

        /*
         * Movie release date
         */
        Date releaseDate = movie.getReleaseDate();
        if (releaseDate != null) {
            mDetailBinding.detailReleaseDateTextView.setText(formatDateToString(releaseDate));
        } else {
            mDetailBinding.detailTitleTextView.setText(getString(R.string.unknown));
        }

        /*
         * Movie plot synopsis
         */
        String overview = movie.getPlotSynopsis();
        if (!overview.isEmpty()) {
            mDetailBinding.detailPlotSynopsisTextView.setText(movie.getPlotSynopsis());
        } else {
            mDetailBinding.detailPlotSynopsisTextView.setText(getString(R.string.no_plot_synopsis_found));
        }
    }

    @Override
    public void onItemClickListener(String youtubeKey) {
        Log.d(LOG_TAG, "(PACK) Launching the Youtube video with key " + youtubeKey);
        launchYoutubeVideo(this, youtubeKey);
    }

    /**
     * Changes the checked state of the star checkbox.
     *
     * @param checked A boolean
     */
    private void setStarCheckboxChecked(boolean checked) {
        mDetailBinding.detailFavoriteCheckbox.setChecked(checked);
    }

    /**
     * Copies a {@link MovieEntry} to {@link FavoriteEntry}.
     * Returns null if the argument is null.
     *
     * @param movieEntry a {@link MovieEntry}.
     * @return a {@link FavoriteEntry} or null.
     */
    FavoriteEntry copyMovieEntryToFavoriteEntry(MovieEntry movieEntry) {
        if (movieEntry == null) {
            return null;
        }
        return new FavoriteEntry(
                movieEntry.getMovieId(),
                movieEntry.getTitle(),
                movieEntry.getPosterUrl(),
                movieEntry.getPlotSynopsis(),
                movieEntry.getUserRating(),
                movieEntry.getReleaseDate());
    }

    /**
     * Copies an {@link ArrayList<ReviewEntry>} to an {@link ArrayList<FavoriteReviewEntry>}.
     * Returns null if the argument is null.
     *
     * @param reviewEntries an {@link ArrayList<ReviewEntry>}.
     * @return an {@link ArrayList<FavoriteReviewEntry>} or null.
     */
    ArrayList<FavoriteReviewEntry> copyReviewEntriesToFavoriteReviewEntries(ArrayList<ReviewEntry> reviewEntries) {
        if (reviewEntries == null) {
            return null;
        }

        ArrayList<FavoriteReviewEntry> favoriteReviews = new ArrayList<>();
        for (ReviewEntry review: reviewEntries) {
            favoriteReviews.add( new FavoriteReviewEntry(
                    review.getReviewId(),
                    review.getMovieId(),
                    review.getAuthor(),
                    review.getContent(),
                    review.getUrl()));
        }
        return favoriteReviews;
    }

    /**
     * Copies an {@link ArrayList<TrailerEntry>} to an {@link ArrayList<FavoriteTrailerEntry>}.
     * Returns null if the argument is null.
     *
     * @param trailerEntries an {@link ArrayList<TrailerEntry>}.
     * @return an {@link ArrayList<FavoriteTrailerEntry>} or null.
     */
    ArrayList<FavoriteTrailerEntry> copyTrailerEntriesToFavoriteTrailerEntries(ArrayList<TrailerEntry> trailerEntries) {
        if (trailerEntries == null) {
            return null;
        }
        ArrayList<FavoriteTrailerEntry> favoriteTrailers = new ArrayList<>();
        for (TrailerEntry trailer: trailerEntries) {
            favoriteTrailers.add(new FavoriteTrailerEntry(
                    trailer.getTrailerId(),
                    trailer.getMovieId(),
                    trailer.getYoutubeKey(),
                    trailer.getSite(),
                    trailer.getType()));
        }
        return favoriteTrailers;
        }
}
