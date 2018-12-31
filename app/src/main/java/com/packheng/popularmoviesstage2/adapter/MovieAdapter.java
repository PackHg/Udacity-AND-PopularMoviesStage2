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

package com.packheng.popularmoviesstage2.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.packheng.popularmoviesstage2.R;
import com.packheng.popularmoviesstage2.databinding.MovieItemBinding;
import com.packheng.popularmoviesstage2.data.database.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Provides binding from a data set to views that are displayed within a RecyclerView.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    private final Context mContext;
    private List<Movie> mMovies;
    final private ItemClickListener mItemClickListener;

    public interface ItemClickListener {
        void onItemClickListener(int movieId);
    }

    public MovieAdapter(Context context, List<Movie> movies, ItemClickListener listener) {
        mContext = context;
        mMovies = movies;
        mItemClickListener = listener;
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        MovieItemBinding binding;

        public MovieViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
            if (binding == null) {
                Log.e(LOG_TAG, "Can't data bind with the item view");
            } else {
                binding.movieItemImageView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            int movieId = mMovies.get(getAdapterPosition()).getMovieId();
            mItemClickListener.onItemClickListener(movieId);
        }
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = mMovies.get(position);
        String posterUrl = movie.getPosterUrl();
        String title = movie.getTitle();
        if (!posterUrl.isEmpty()) {
            holder.binding.movieItemEmptyTextView.setVisibility(View.GONE);
            Picasso.with(mContext).load(posterUrl).into(holder.binding.movieItemImageView);
        } else {
            holder.binding.movieItemEmptyTextView.setVisibility(View.VISIBLE);
            holder.binding.movieItemEmptyTextView.setText(title);
        }
    }

    @Override
    public int getItemCount() {
        if (mMovies == null) {
            return 0;
        }
        return mMovies.size();
    }

    public List<Movie> getMovies() {
        return mMovies;
    }

    /**
     * When data changes, this method updates the list of movies
     * and notifies the adapter to use the new values on it
     */
    public void setMovies(List<Movie> mMovies) {
        this.mMovies = mMovies;
        notifyDataSetChanged();
    }
}
