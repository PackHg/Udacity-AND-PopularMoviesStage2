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

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.packheng.popularmoviesstage2.databinding.TrailerItemBinding;
import com.packheng.popularmoviesstage2.db.TrailerEntry;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import static com.packheng.popularmoviesstage2.utils.NetworkUtils.isNetworkConnected;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private static final String LOG_TAG = TrailerAdapter.class.getSimpleName();

    private final Context mContext;
    private List<TrailerEntry> mTrailers;
    private final ItemClickListener mItemClickListener;

    public interface ItemClickListener {
        void onItemClickListener(String youtubeKey);
    }

    public TrailerAdapter(Context context, List<TrailerEntry> trailers, ItemClickListener listener) {
        mContext = context;
        mTrailers = trailers;
        mItemClickListener = listener;
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

        TrailerItemBinding binding;

        public TrailerViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
            if (binding == null) {
                Log.e(LOG_TAG, "Can't data dind with the item view");
            } else {
                binding.detailTrailerThumbnail.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            String youtubeKey = mTrailers.get(getAdapterPosition()).getYoutubeKey();
            Log.d(LOG_TAG, "(PACK) Click on trailer " + getAdapterPosition() + "and key " + youtubeKey);
            mItemClickListener.onItemClickListener(youtubeKey);
        }
    }

    @NonNull
    @Override
    public TrailerAdapter.TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.trailer_item, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerAdapter.TrailerViewHolder holder, int position) {

        final String BASE = "http://img.youtube.com/vi/";
        final String SIZE = "/0.jpg";

        TrailerEntry trailer = mTrailers.get(position);

        if(!isNetworkConnected(mContext)) {
            holder.binding.detailTrailerThumbnail.setVisibility(View.GONE);
            holder.binding.detailTrailerEmptyView.setVisibility(View.VISIBLE);

            holder.binding.detailTrailerEmptyView.setText(
                    String.format(Locale.getDefault() ,"%s %d", R.string.trailer, position));
        } else {
            holder.binding.detailTrailerThumbnail.setVisibility(View.VISIBLE);
            holder.binding.detailTrailerEmptyView.setVisibility(View.GONE);

            String youtubeKey = trailer.getYoutubeKey();
            Picasso.with(mContext).load(BASE + youtubeKey + SIZE)
                    .into(holder.binding.detailTrailerThumbnail);
        }
    }

    @Override
    public int getItemCount() {
        if (mTrailers == null) {
            return 0;
        }
        return mTrailers.size();
    }

    public List<TrailerEntry> getTrailers() {
        return mTrailers;
    }

    /**
     * When data changes, this method updates the list of trailers
     * and notifies the adapter to use the new values on it
     */
    public void setTrailers(List<TrailerEntry> trailers) {
        mTrailers = trailers;
        notifyDataSetChanged();
    }
}
