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
import com.packheng.popularmoviesstage2.databinding.ReviewItemBinding;
import com.packheng.popularmoviesstage2.db.ReviewEntry;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private static final String LOG_TAG = ReviewAdapter.class.getSimpleName();

    private final Context mContext;
    private List<ReviewEntry> mReviews;

    public ReviewAdapter(Context context, List<ReviewEntry> reviewEntries) {
        mContext = context;
        mReviews = reviewEntries;
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        ReviewItemBinding binding;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            if (binding == null) {
                Log.e(LOG_TAG, "Can't data dind with the item view");
            }
        }
    }

    @NonNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ReviewViewHolder holder, int position) {
        ReviewEntry review = mReviews.get(position);

        // Review author
        String author = review.getAuthor();
        if (author.isEmpty()) {
            holder.binding.reviewAuthor.setText(mContext.getText(R.string.review_unknown_author));
        } else {
            holder.binding.reviewAuthor.setText(author);
            Log.d(LOG_TAG, "(PACK) onBindViewHolder() - Review Author: " + author);
        }

        // Review content
        String content = review.getContent();
        if (content.isEmpty()) {
            holder.binding.reviewContent.setText(mContext.getText(R.string.review_no_content));
        } else {
            holder.binding.reviewContent.setText(content);
        }
    }

    @Override
    public int getItemCount() {
        if (mReviews == null) {
            return 0;
        }
        return mReviews.size();
    }

    public List<ReviewEntry> getReviews() {
        return mReviews;
    }

    /**
     * When data changes, this method updates the list of reviews
     * and notifies the adapter to use the new values on it
     */
    public void setReviews(List<ReviewEntry> reviews) {
        mReviews = reviews;
        notifyDataSetChanged();
    }
}
