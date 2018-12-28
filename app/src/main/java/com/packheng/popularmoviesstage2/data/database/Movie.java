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

package com.packheng.popularmoviesstage2.data.database;

import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

/**
 * Parent class used to extend {@link MovieEntry} and {@link FavoriteEntry}.
 */
public class Movie {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int movieId;
    private String title;
    private String posterUrl;
    private String plotSynopsis;
    private double userRating;
    private Date releaseDate;

    @Ignore
    public Movie(int movieId, String title, String posterUrl, String plotSynopsis,
                      double userRating, Date releaseDate) {
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.plotSynopsis = plotSynopsis;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }

    public Movie(int id, int movieId, String title, String posterUrl, String plotSynopsis,
                      double userRating, Date releaseDate) {
        this.id = id;
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.plotSynopsis = plotSynopsis;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }

    public int getId() {
        return id;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public double getUserRating() {
        return userRating;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }
}

