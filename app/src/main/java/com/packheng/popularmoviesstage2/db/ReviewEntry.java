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

package com.packheng.popularmoviesstage2.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "reviews")
public class ReviewEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String reviewId;
    private int movieId;
    private String author;
    private String content;
    private String url;

    @Ignore
    public ReviewEntry(String reviewId, int movieId, String author, String content, String url) {
        this.reviewId = reviewId;
        this.movieId = movieId;
        this.author = author;
        this.content = content;
        this.url = url;
    }

    public ReviewEntry(int id, String reviewId, int movieId, String author, String content, String url) {
        this.id = id;
        this.reviewId = reviewId;
        this.movieId = movieId;
        this.author = author;
        this.content = content;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public String getReviewId() {
        return reviewId;
    }
    public int getMovieId() {
        return movieId;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }
}
