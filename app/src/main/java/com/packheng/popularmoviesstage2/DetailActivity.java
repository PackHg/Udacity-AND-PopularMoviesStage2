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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.packheng.popularmoviesstage2.utils.DateToStringUtils.formatDateToString;
import static com.packheng.popularmoviesstage2.utils.DateToStringUtils.stringToDate;

/**
 * Shows the details of a movie.
 */
public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.detail_activity_poster_iv) ImageView posterImageView;
    @BindView(R.id.detail_activity_poster_empty_tv) TextView emptyPosterTextView;
    @BindView(R.id.detail_activity_title_tv) TextView titleTextView;
    @BindView(R.id.detail_activity_user_rating_tv) TextView userRatingTextView;
    @BindView(R.id.detail_activity_release_date_tv) TextView releaseDatetextView;
    @BindView(R.id.detail_activity_plot_synopsis_tv) TextView overviewTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        final int DEFAULT_POSITION = 0;

        Intent intent = getIntent();
        int position = intent.getIntExtra(MoviesAdapter.MOVIE_POSITION_KEY, DEFAULT_POSITION);

        Movie movie = MainActivity.movies.get(position);

        String posterUrl = movie.getPosterUrl();
        if (!posterUrl.isEmpty()) {
            emptyPosterTextView.setVisibility(View.GONE);
            Picasso.with(this).load(movie.getPosterUrl()).into(posterImageView);
        } else {
            emptyPosterTextView.setVisibility(View.VISIBLE);
        }

        titleTextView.setText(movie.getTitle());
        userRatingTextView.setText(String.format(Locale.getDefault(), "%1.1f", movie.getUserRating()));

        String releaseDate = movie.getReleaseDate();
        if (!releaseDate.isEmpty()) {
            Date date = stringToDate(releaseDate);
            releaseDate = formatDateToString(date);
            releaseDatetextView.setText(releaseDate);
        } else {
            releaseDatetextView.setText(getString(R.string.unknown));
        }

        String overview = movie.getPlotSynopsis();
        if (!overview.isEmpty()) {
            overviewTextView.setText(movie.getPlotSynopsis());
        } else {
            overviewTextView.setText(getString(R.string.no_plot_synopsis_found));
        }

    }
}
